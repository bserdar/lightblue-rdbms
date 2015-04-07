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

import com.redhat.lightblue.util.Path;

/**
 * Set the value of lvar using current value of rvar, or the result of the script, or the value
 */
public class SetOperation implements ScriptOperation {

    public static final String NAME="$set";
    
    private Path lVariable;

    // Only one of these will be non-null, depending on the rvalue
    private Script rScript;
    private Path rVariable;
    private Value rValue;


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

    public Script getRScript() {
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
    public void execute(ScriptExecutionContext ctx) {
        Value result=null;
        // Depending on which r-value is non-null, execute
        if(rScript!=null) {
            ScriptExecutionContext newCtx=ctx.newContext();
            rScript.execute(newCtx);
            result=newCtx.getLastExecutionResult();
        } else if(rVariable!=null) {
            result=ctx.getVarValue(rVariable);
        } else if(rValue!=null) {
            result=rValue;
        }
        ctx.setVarValue(lVariable,result);
    }
    
}
