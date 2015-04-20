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

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.ScriptOperationFactory;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.Script;

public class ConditionalOperationFactory implements ScriptOperationFactory<ConditionalOperation> {
    public static final String NAMES[]={ConditionalOperation.NAME};

    @Override
    public String[] operationNames() {
        return NAMES;
    }
    
    @Override
    public ConditionalOperation getOperation(OperationRegistry reg,ObjectNode node) {
        ScriptOperation test,trueScript,falseScript;

        ObjectNode args=(ObjectNode)node.get(ConditionalOperation.NAME);
        JsonNode x=args.get("test");
        if(x!=null) {
            if(x instanceof ObjectNode) {
                test=Script.parse(reg,x);
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_TEST,x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"test");
        }
        x=args.get("then");
        if(x!=null) {
            trueScript=Script.parse(reg,x);
        } else
            trueScript=null;
        x=args.get("else");
        if(x!=null) {
            falseScript=Script.parse(reg,x);
        } else
            falseScript=null;
        return new ConditionalOperation(test,trueScript,falseScript);
    }
}
