/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rdbms.rdsl;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.dialect.AbstractDialect;

/**
 * This class deals with binding values/variables to ad-hoc query and
 * update statements used in scripts.
 */
public class Bindings {

    private final List<Binding> bindings=new ArrayList<>();

    public Bindings() {}

    public Bindings(Binding...b) {
        add(b);
    }

    /**
     * Number of bindings
     */
    public int size() {
        return bindings.size();
    }

    /**
     * Returns the binding at the given index. Index starts from 0
     */
    public Binding get(int i) {
        return bindings.get(i);
    }

    /**
     * Adds new bindings
     */
    public void add(Binding... b) {
        for(Binding x:b)
            bindings.add(x);
    }

    /**
     * Sets all the IN and INOUT binding values to the prepared statement
     */
    public void setParameters(ScriptExecutionContext ctx,PreparedStatement stmt) {
        setParameters(1,ctx,stmt);
    }

    /**
     * Sets the IN and INOUT bindings of the prepared statement, starting from the given parameter index
     * 
     * @param firstIndex the first parameter index to bind, starting with 1
     * @param ctx The execution context
     * @param stmt The statement
     */
    public void setParameters(int firstIndex,ScriptExecutionContext ctx,PreparedStatement stmt) {
        // Do all the IN bindings first
        int index=firstIndex;
        for(Binding b:bindings) {
            if(b.getDir()==Binding.Dir.IN||
               b.getDir()==Binding.Dir.INOUT) {
                Value v=b.getValue(ctx);
                ctx.getDialect().setParameter(stmt,index,b.getJdbcType(),v==null?null:v.getValue());
            }
            index++;
        }
    }

    /**
     * Registers all OUT parameters for the statement
     */
    public void registerOutParameters(ScriptExecutionContext ctx,CallableStatement stmt) {
        // Do all the out bindings
        int index=1;
        for(Binding b:bindings) {
            if(b.getDir()==Binding.Dir.OUT||
               b.getDir()==Binding.Dir.INOUT) {
                if(b.getJdbcType()==null)
                    throw Error.get(ScriptErrors.ERR_OUT_PARAMETER_NEEDS_TYPE,b.toString());
                if(b instanceof VariableBinding)
                    ctx.getDialect().registerOutParameter(stmt,index,b.getJdbcType());
                else
                    throw Error.get(ScriptErrors.ERR_OUT_PARAMETER_NEEDS_VARIABLE_BINDING,b.toString());
            }
            index++;
        }
    }

    public void retrieveOutValues(ScriptExecutionContext ctx,CallableStatement stmt) {
        int index=1;
        for(Binding b:bindings) {
            if(b.getDir()==Binding.Dir.OUT||
               b.getDir()==Binding.Dir.INOUT) {
                if(b instanceof VariableBinding) {
                    Value v=ctx.getDialect().getOutValue(stmt,index,b.getJdbcType());
                    ctx.setVarValue( ((VariableBinding)b).getVar(), v);
                }
            }
            index++;
        }
    }

    public static Bindings parseBindings(ArrayNode bindingsNode) {
        Bindings ret=new Bindings();
        for(Iterator<JsonNode> itr=bindingsNode.elements();itr.hasNext();) {
            JsonNode bindingNode=itr.next();
            if(bindingNode instanceof ObjectNode) {
                ObjectNode on=(ObjectNode)bindingNode;
                JsonNode x=on.get("type");
                Integer jdbcType;
                if(x!=null) {
                    jdbcType=AbstractDialect.parseJDBCType(x.asText());
                } else
                    jdbcType=null;
                x=on.get("var");
                if(x!=null) {
                    // variable binding
                    Path var=new Path(x.asText());
                    Binding.Dir dir;
                    x=on.get("dir");
                    if(x!=null) {
                        String dt=x.asText();
                        if("IN".equalsIgnoreCase(dt))
                            dir=Binding.Dir.IN;
                        else if("OUT".equalsIgnoreCase(dt))
                            dir=Binding.Dir.OUT;
                        else if("INOUT".equalsIgnoreCase(dt))
                            dir=Binding.Dir.INOUT;
                        else
                            throw Error.get(ScriptErrors.ERR_INVALID_VALUE,dt);
                    } else
                        dir=Binding.Dir.IN;
                    ret.bindings.add(new VariableBinding(var,dir,jdbcType));
                } else {
                    x=on.get("value");
                    if(x==null)
                        throw Error.get(ScriptErrors.ERR_INVALID_VALUE,on.toString());
                    // value binding, can only by IN
                    ret.bindings.add(new ValueBinding(Value.toValue(x),jdbcType));
                }
            } else {
                // Assume it is a variable IN binding 
                ret.bindings.add(new VariableBinding(new Path(bindingNode.asText()),Binding.Dir.IN));
            }
        }
        return ret;
    }
}
