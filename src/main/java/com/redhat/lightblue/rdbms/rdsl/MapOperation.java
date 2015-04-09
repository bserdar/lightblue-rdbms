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

public class MapOperation implements ScriptOperation, ScriptOperationFactory {

    public static final String NAME="$map";
    public static final String NAMES[]={NAME};

    private Path source;
    private Path dest;
    
    public MapOperation() {}

    public MapOperation(Path source,Path dest) {
        this.source=source;
        this.dest=dest;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        Value ret=Value.NULL_VALUE;
        return ret;
    }
    
    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        MapOperation newOp=new MapOperation();
        ObjectNode args=(ObjectNode)node.get(NAME);
        JsonNode x=args.get("dest");
        if(x!=null) {
            newOp.dest=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"dest");
        }
        x=args.get("source");
        if(x!=null) {
            newOp.source=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"source");
        }
        return newOp;
    }
}
