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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class ConditionalOperation implements ScriptOperation, ScriptOperationFactory {

    private static final Logger LOGGER=LoggerFactory.getLogger(ConditionalOperation.class);

    public static final String NAME="$conditional";
    public static final String NAMES[]={NAME};

    private ScriptOperation test;
    private ScriptOperation trueScript;
    private ScriptOperation falseScript;

    public ConditionalOperation() {}

    public ConditionalOperation(ScriptOperation test,
                                ScriptOperation trueScript,
                                ScriptOperation falseScript) {
        this.test=test;
        this.trueScript=trueScript;
        this.falseScript=falseScript;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("conditional");
        ScriptExecutionContext newCtx=ctx.newContext();
        Value val=test.execute(newCtx);
        LOGGER.debug("test:{}",val);
        if(val.getType()==ValueType.primitive) {
            if((val.getValue() instanceof Boolean&&(Boolean)val.getValue()) ||
               (val.getValue() instanceof Number&& (Number)val.getValue()!=0) ) {
                LOGGER.debug("running true script");
                newCtx=ctx.newContext();
                val=trueScript.execute(newCtx);
            } else {
                LOGGER.debug("running false script");
                newCtx=ctx.newContext();
                val=falseScript.execute(newCtx);
            }
        } else
            throw Error.get(ScriptErrors.ERR_BOOLEAN_REQUIRED,val.toString());
        LOGGER.debug("conditional:{}",val);
        return val;
    }

    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        ConditionalOperation newOp=new ConditionalOperation();
        ObjectNode args=(ObjectNode)node.get(NAME);
        JsonNode x=args.get("test");
        if(x!=null) {
            if(x instanceof ObjectNode) {
                test=reg.get( (ObjectNode)x );
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_TEST,x.asText());
            newOp.test=Script.parse(reg,x);
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"test");
        }
        x=args.get("then");
        if(x!=null) {
            newOp.trueScript=Script.parse(reg,x);
        } 
        x=args.get("else");
        if(x!=null) {
            newOp.falseScript=Script.parse(reg,x);
        }
        return newOp;
    }
}
