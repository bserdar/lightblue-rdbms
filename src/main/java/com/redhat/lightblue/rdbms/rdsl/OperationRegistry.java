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

import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;

public class OperationRegistry {

    private final Map<String,ScriptOperationFactory> map=new HashMap<>();


    /**
     * Constructs the registry with all known operations
     */
    public OperationRegistry() {
        add(ExecuteSqlClauseOperation.FACTORY);
        add(ForEachOperation.FACTORY);
        add(InsertRowOperation.FACTORY);
        add(MapOperation.FACTORY);
        add(NullOperation.FACTORY);
        add(SetOperation.FACTORY);
    }

    /**
     * Registers the given script operation factory
     */
    public void add(ScriptOperationFactory f) {
        for(String name:f.operationNames())
            map.put(name,f);
    }

    /**
     * Configures and returns a script operation using the Json node
     */
    public ScriptOperation get(ObjectNode node) {
        if(node.size()==1) {
            String name=node.fieldNames().next();
            ScriptOperationFactory f=map.get(name);
            if(f!=null) {
                return f.getOperation(this,node);
            } else {
                throw Error.get(ScriptErrors.ERR_UNKNOWN_OPERATION,name);
            } 
        } else {
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,node.toString());
        }
    }
}
