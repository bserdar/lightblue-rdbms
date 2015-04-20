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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperationFactory;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.Bindings;

public class ApplyBindingOperationFactory implements ScriptOperationFactory<ApplyBindingOperation> {

    public static final String NAMES[]={ApplyBindingOperation.NAME};

    @Override
    public String[] operationNames() {
        return NAMES;
    }
            
    @Override
    public ApplyBindingOperation getOperation(OperationRegistry reg,ObjectNode node) {
        ObjectNode argNode=(ObjectNode)node.get(ApplyBindingOperation.NAME);
        Path row;
        JsonNode x=argNode.get("row");
        if(x==null)
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"row");
        row=new Path(x.asText());
        
        Bindings bindings;
        x=argNode.get("bindings");
        if(x instanceof ArrayNode) {
            bindings=Bindings.parseBindings( (ArrayNode)x); 
        } else 
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,x.toString());
        return new ApplyBindingOperation(row,bindings);
    }            
}
