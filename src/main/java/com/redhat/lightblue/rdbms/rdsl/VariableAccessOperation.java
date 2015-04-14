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

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;

/**
 * This is an operation of the foem
 * <pre>
 *   { var: varName }
 * </pre>
 * It returns the value of the variable 'varName'
 */
public class VariableAccessOperation implements ScriptOperation, ScriptOperationFactory {

    public static final String NAME="var";
    public static final String NAMES[]={NAME};
    
    private Path var;

    public VariableAccessOperation() {}

    public VariableAccessOperation(Path var) {
        this.var=var;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        return ctx.getVarValue(var);
    }
    
    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        return new VariableAccessOperation(new Path(node.get(NAME).asText()));
    }
}
