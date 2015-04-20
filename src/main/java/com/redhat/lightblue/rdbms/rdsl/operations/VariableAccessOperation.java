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

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.ScriptExecutionContext;

/**
 * This is an operation of the foem
 * <pre>
 *   { var: varName }
 * </pre>
 * It returns the value of the variable 'varName'
 *
 * The main purpose of this is to expose the value of a variable as a
 * script. When the grammar requires a script, but passing a variable
 * value is required, use this.
 */
public class VariableAccessOperation implements ScriptOperation {

    public static final String NAME="var";
    
    private Path var;

    public VariableAccessOperation() {}

    public VariableAccessOperation(Path var) {
        this.var=var;
    }

    public Path getVar() {
        return var;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        return ctx.getVarValue(var);
    }    
}
