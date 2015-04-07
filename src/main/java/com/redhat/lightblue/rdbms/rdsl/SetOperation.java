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

/**
 * Set the value of lvar using current value of rvar
 */
public class SetOperation implements ScriptOperation {

    public static final String NAME="$set";
    
    private Path lVariable;

    // Only one of these will be non-null, depending on the rvalue
    private Script rScript;
    private Path rVariable;
    private Value rValue;


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

    public vois setRValue(Value v) {
        rValue=v;
    }

    @Override
    public void execute(ScriptOperationContext ctx) {
        Value result=null;
        // Depending on which r-value is non-null, execute
        if(rscript!=null) {
            ScriptOperationContext newCtx=ctx.newContext();
            result=newCtx.execute(script);
        } else if(rVariable!=null) {
            result=ctx.getVarValue(rVariable);
        } else if(rValue!=null) {
            result=rValue;
        }
        ctx.setVarValue(lVariable,result);
    }
    
}
