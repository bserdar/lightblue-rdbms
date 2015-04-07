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

/**
 * A script is a list of operations, and it is an operation as well.
 */
public class Script implements ScriptOperation {
    private List<ScriptOperation> operations;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void execute(ScriptExecutionContext ctx) {
        ScriptExecutionContext newCtx=ctx.newContext();
        for(ScriptOperation op:operations) {
            op.execute(newCtx);
        }
        ctx.setLastExecutionResult(newCtx.getLastExecutionResult());
    }

    public List<ScriptOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<ScriptOperation> s) {
        this.operations=s;
    }
}
