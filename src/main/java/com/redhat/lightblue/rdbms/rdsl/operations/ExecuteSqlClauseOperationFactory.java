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

import com.redhat.lightblue.rdbms.rdsl.Bindings;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperationFactory;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;

public class ExecuteSqlClauseOperationFactory implements ScriptOperationFactory<ExecuteSqlClauseOperation> {

    public static final String NAMES[]={ExecuteSqlClauseOperation.NAMEQ,
                                        ExecuteSqlClauseOperation.NAMECALL};
    
    @Override
    public String[] operationNames() {
        return NAMES;
    }
    
    @Override
    public ExecuteSqlClauseOperation getOperation(OperationRegistry reg,ObjectNode node) {
        String name;
        String clause;
        Bindings bindings;

        ObjectNode argNode=(ObjectNode)node.get(ExecuteSqlClauseOperation.NAMEQ);
        if(argNode==null) {
            argNode=(ObjectNode)node.get(ExecuteSqlClauseOperation.NAMECALL);
            name=ExecuteSqlClauseOperation.NAMECALL;
        } else {
            name=ExecuteSqlClauseOperation.NAMEQ;
        }
        
        JsonNode x=argNode.get("clause");
        if(x==null)
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"clause");
        clause=x.asText();
        
        x=argNode.get("bindings");
        if(x instanceof ArrayNode) {
            bindings=Bindings.parseBindings( (ArrayNode)x); 
        } else if(x!=null)
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,x.toString());
        else
            bindings=null;
        return new ExecuteSqlClauseOperation(name,clause,bindings);
    }
}
