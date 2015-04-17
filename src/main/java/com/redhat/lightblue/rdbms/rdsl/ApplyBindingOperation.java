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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

public class ApplyBindingOperation implements ScriptOperation, ScriptOperationFactory {

    private static final Logger LOGGER=LoggerFactory.getLogger(ApplyBindingOperation.class);

    public static final String NAME="apply-binding";
    public static final String NAMES[]={NAME};

    private Path row;
    private Bindings bindings;
    
    public ApplyBindingOperation(Path row,Bindings bindings) {
        this.row=row;
        this.bindings=bindings;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("begin processing {}",row);
        Value rowv=ctx.getVarValue(row);
        if(rowv.getType()==ValueType.list) {
            ListValue columns=rowv.getListValue();
            if(!columns.isEmpty()) {
                LOGGER.debug("row list is not empty");
                int index=0;
                for(Iterator<Value> itr=columns.getValues();itr.hasNext();) {
                    Value col=itr.next();
                    LOGGER.debug("processing column {}",index);
                    Binding binding=bindings.get(index);
                    binding.setValue(ctx,col);
                    index++;
                }
            } else
                LOGGER.debug("empty list");
        } else
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,row.toString());
        LOGGER.debug("end processing {}",row);
        return Value.NULL_VALUE;
    }
        
    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        ObjectNode argNode=(ObjectNode)node.get(NAME);
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
