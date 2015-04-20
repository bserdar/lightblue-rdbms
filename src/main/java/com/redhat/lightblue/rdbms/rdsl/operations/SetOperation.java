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
package com.redhat.lightblue.rdbms.rdsl.operations;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.ScriptExecutionContext;
import com.redhat.lightblue.rdbms.rdsl.Script;

/**
 * Set the value of lvar using current value of rvar, or the result of the script, or the value
 * <pre>
 *    { $set: { dest: l-variable, var: r-variable } }
 *    { $set: { dest: l-variable, value: value } }
 *    { $set: { dest: l-variable, valueof: script } }
 * </pre>
 */
public class SetOperation implements ScriptOperation {

    public static final String NAME="$set";
    
    private Path lVariable;

    // Only one of these will be non-null, depending on the rvalue
    private ScriptOperation rScript;
    private Path rVariable;
    private Value rValue;

    /**
     * Default Ctor
     */
    public SetOperation() {}

    /**
     * Constructs a set operation that sets the variable from the result of the script
     */
    public SetOperation(Path lvalue,
                        ScriptOperation rvalue) {
        this.lVariable=lvalue;
        this.rScript=rvalue;
    }

    /**
     * Constructs a set operation that sets the lvalue from the value of rvalue
     */
    public SetOperation(Path lvalue,
                        Path rvalue) {
        this.lVariable=lvalue;
        this.rVariable=rvalue;
    }

    /**
     * Constructs a set operation that set the lvalue from rvalue
     */
    public SetOperation(Path lvalue,
                        Value rvalue) {
        this.lVariable=lvalue;
        this.rValue=rvalue;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * l-value (the destination variable)
     */
    public Path getLVariable() {
        return lVariable;
    }

    /**
     * l-value (the destination variable)
     */
    public void setLVariable(Path p) {
        lVariable=p;
    }

    /**
     * r-script ( dest: lvalue, valueOf: script })
     */
    public ScriptOperation getRScript() {
        return rScript;
    }

    /**
     * r-script ( dest: lvalue, valueOf: script })
     */
    public void setRScript(Script s) {
        rScript=s;
    }

    /**
     * r-variable { dest: lvalue, var: rvariable }
     */
    public Path getRVariable() {
        return rVariable;
    }

    /**
     * r-variable { dest: lvalue, var: rvariable }
     */
    public void setRVariable(Path p) {
        rVariable=p;
    }

    /**
     * r-value ( dest: lvalue, value: rvalue }
     */
    public Value getRValue() {
        return rValue;
    }

    /**
     * r-value ( dest: lvalue, value: rvalue }
     */
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
    
}
