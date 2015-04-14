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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Set the value of lvar using current value of rvar, or the result of the script, or the value
 */
public class SetOperation implements ScriptOperation, ScriptOperationFactory {

    public static final String NAME="$set";
    public static final String NAMES[]={NAME};
    
    private Path lVariable;

    // Only one of these will be non-null, depending on the rvalue
    private ScriptOperation rScript;
    private Path rVariable;
    private Value rValue;


    public SetOperation() {}

    public SetOperation(Path lvalue,
                        ScriptOperation rvalue) {
        this.lVariable=lvalue;
        this.rScript=rvalue;
    }

    public SetOperation(Path lvalue,
                        Path rvalue) {
        this.lVariable=lvalue;
        this.rVariable=rvalue;
    }

    public SetOperation(Path lvalue,
                        Value rvalue) {
        this.lVariable=lvalue;
        this.rValue=rvalue;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Path getLVariable() {
        return lVariable;
    }

    public void setLVariable(Path p) {
        lVariable=p;
    }

    public ScriptOperation getRScript() {
        return rScript;
    }

    public void setRScript(Script s) {
        rScript=s;
    }

    public Path getRVariable() {
        return rVariable;
    }

    public void setRVariable(Path p) {
        rVariable=p;
    }

    public Value getRValue() {
        return rValue;
    }

    public void setRValue(Value v) {
        rValue=v;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        Value result=null;
        // Depending on which r-value is non-null, execute
        if(rScript!=null) {
            ScriptExecutionContext newCtx=ctx.newContext();
            result=rScript.execute(newCtx);
        } else if(rVariable!=null) {
            result=ctx.getVarValue(rVariable);
        } else if(rValue!=null) {
            result=rValue;
        }
        ctx.setVarValue(lVariable,result);
        return result;
    }
    
    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        SetOperation newOp=new SetOperation();
        ObjectNode configNode=(ObjectNode)node.get(NAME);
        JsonNode x=configNode.get("dest");
        if(x!=null)
            newOp.setLVariable(new Path(x.asText()));
        else
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"dest");
        x=configNode.get("var");
        if(x!=null) {
            newOp.setRVariable(new Path(x.asText()));
        } else {
            x=configNode.get("value");
            if(x!=null) {
                newOp.setRValue(Value.toValue(x));
            } else {
                x=configNode.get("valueOf");
                if(x!=null) {
                    rScript=Script.parse(reg,x);
                } else {
                    throw Error.get(ScriptErrors.ERR_MISSING_ARG,"var/value/valueOf");
                }
            }
        }
        return newOp;
    }
}
