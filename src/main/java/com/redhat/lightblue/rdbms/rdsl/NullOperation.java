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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.tables.Table;

/**
 * Sets a value or a group of values to null
 * <pre>
 *    { $null : { dest: var } }
 * </pre>
 *
 * If 'dest' is a variable, sets it to null. If 'dest' is a table,
 * sets all column values of that table to null.
 */
public class NullOperation implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(NullOperation.class);

    public static final String NAME="$null";
    public static final String NAMES[]={NAME};

    public static final ScriptOperationFactory FACTORY=new ScriptOperationFactory() {
            @Override
            public String[] operationNames() {
                return NAMES;
            }
            
            @Override
            public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
                NullOperation newOp=new NullOperation();
                ObjectNode args=(ObjectNode)node.get(NAME);
                JsonNode x=args.get("dest");
                if(x!=null) {
                    newOp.dest=new Path(x.asText());
                } else {
                    throw Error.get(ScriptErrors.ERR_MISSING_ARG,"dest");
                }
                return newOp;
            }
        };
    
    private Path dest;

    public NullOperation() {}

    public NullOperation(Path dest) {
        this.dest=dest;
    }

    public Path getDest() {
        return dest;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        ValueType type=ctx.getVarType(dest);
        switch(type) {
        case primitive:
        case lob:
        case list:
        case map:
            ctx.setVarValue(dest,null);
            break;

        case table:
            ((Table)ctx.getVarValue(dest).getValue()).resetColumnValues();
            break;
        }
        return Value.NULL_VALUE;
    }

}
