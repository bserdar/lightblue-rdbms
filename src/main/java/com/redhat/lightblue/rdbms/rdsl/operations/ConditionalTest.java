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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.OperationRegistry;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.Script;
import com.redhat.lightblue.rdbms.rdsl.ScriptExecutionContext;

/**
 * Abstract base class for conditonal tests. Override the class, and
 * implement executeTest.
 */
public abstract class ConditionalTest implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(ConditionalTest.class);

    private final String name;

    protected ConditionalTest(String name) {
        this.name=name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("Executing test {}",name);
        boolean v=executeTest(ctx);
        LOGGER.debug("{}:{}",name,v);
        return new Value(v);
    }

    public static ScriptOperation parseVarOrScript(OperationRegistry reg,ObjectNode operationNode,String opName) {
        JsonNode argNode=operationNode.get(opName);
        if(argNode!=null) {
            if(argNode instanceof ObjectNode) {
                return reg.get( (ObjectNode) argNode);
            } else {
                return new Script(new VariableAccessOperation(new Path(argNode.asText())));
            }
        } else
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,opName);
    }

    /**
     * Execute the test and return the result
     */
    protected abstract boolean executeTest(ScriptExecutionContext ctx);
}
