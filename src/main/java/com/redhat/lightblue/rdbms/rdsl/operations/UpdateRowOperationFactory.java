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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperationFactory;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.SqlClause;

public class UpdateRowOperationFactory implements ScriptOperationFactory<UpdateRowOperation> {

    public static final String NAMES[]={UpdateRowOperation.NAME};

    @Override
    public String[] operationNames() {
        return NAMES;
    }
    
    @Override
    public UpdateRowOperation getOperation(OperationRegistry reg,ObjectNode node) {
        Path table;
        List<String> columns;
        SqlClause whereClause;

        ObjectNode argNode=(ObjectNode)node.get(UpdateRowOperation.NAME);
        JsonNode x=argNode.get("table");
        if(x==null)
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"table");
        table=new Path(x.asText());
        x=argNode.get("columns");
        if(x instanceof ArrayNode) {
            ArrayNode arr=(ArrayNode)x;
            columns=new ArrayList<>(arr.size());
            for(Iterator<JsonNode> itr=arr.elements();itr.hasNext();) {
                JsonNode col=itr.next();
                columns.add(col.asText());
            }
        } else if(x!=null)
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,node.toString());
        else
            columns=null;
        x=argNode.get("where");
        if(x instanceof ObjectNode)
            whereClause=SqlClause.parseClause((ObjectNode)x);
        else if(x!=null)
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,node.toString());
        else
            whereClause=null;
        return new UpdateRowOperation(table,columns,whereClause);
    }
}
