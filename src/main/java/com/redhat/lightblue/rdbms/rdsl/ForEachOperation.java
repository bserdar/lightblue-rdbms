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

public class ForEachOperation implements ScriptOperation, ScriptOperationFactory {

    private static final Logger LOGGER=LoggerFactory.getLogger(ForEachOperation.class);

    public static final String NAME="$foreach";
    public static final String NAMES[]={NAME};

    private Path var;
    private Path elemVar;
    private Script doit;

    public ForEachOperation() {}

    public ForEachOperation(Path var,
                            Path elemVar,
                            Script doit) {
        this.var=var;
        this.elemVar=elemVar;
        this.doit=doit;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("$foreach {}",var);
        Value ret=Value.NULL_VALUE;
        Value listValue=ctx.getVarValue(var);
        if(listValue.getType()==ValueType.list) {
            ListValue lv=listValue.getListValue();
            if(!lv.isEmpty()) {
                LOGGER.debug("Starting iteration of {}",var);
                int elemIndex=0;
                for(Iterator<Value> itr=lv.getValues();itr.hasNext();elemIndex++) {
                    Value elementValue=itr.next();
                    LOGGER.debug("{} : Element value {}={}",elemIndex,elemVar,elementValue);
                    if(elemVar!=null) {
                        ctx.setVarValue(elemVar,elementValue);
                    }
                    if(doit!=null) {
                        LOGGER.debug("{} : Calling script",elemIndex);
                        ScriptExecutionContext newCtx=ctx.newContext();
                        ret=doit.execute(newCtx);
                        LOGGER.debug("{} : Script returned",elemIndex);
                    }
                }
            }
        } else {
            throw Error.get(ScriptErrors.ERR_LIST_REQUIRED,var.toString());
        }
        return ret;
    }

    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        ForEachOperation newOp=new ForEachOperation();
        ObjectNode args=(ObjectNode)node.get(NAME);
        JsonNode x=args.get("var");
        if(x!=null) {
            newOp.var=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"var");
        }
        x=args.get("elem");
        if(x!=null) {
            newOp.elemVar=new Path(x.asText());
        } 
        x=args.get("do");
        if(x!=null) {
            newOp.doit=Script.parse(reg,x);
        }
        return newOp;
    }
}
