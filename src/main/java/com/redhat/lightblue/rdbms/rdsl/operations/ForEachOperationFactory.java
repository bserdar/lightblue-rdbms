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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperationFactory;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.Script;

public class ForEachOperationFactory implements ScriptOperationFactory<ForEachOperation> {

    public static final String NAMES[]={ForEachOperation.NAME};
    
    @Override
    public String[] operationNames() {
        return NAMES;
    }
    
    @Override
    public ForEachOperation getOperation(OperationRegistry reg,ObjectNode node) {
        Path var,elemVar;
        ScriptOperation doit;

        ObjectNode args=(ObjectNode)node.get(ForEachOperation.NAME);
        JsonNode x=args.get("var");
        if(x!=null) {
            var=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"var");
        }
        x=args.get("elem");
        if(x!=null) {
            elemVar=new Path(x.asText());
        } else
            elemVar=null;
        x=args.get("do");
        if(x!=null) {
            doit=Script.parse(reg,x);
        } else
            doit=null;
        return new ForEachOperation(var,elemVar,doit);
    }
}
