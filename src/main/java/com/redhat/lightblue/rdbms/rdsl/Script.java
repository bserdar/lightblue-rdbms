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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;

/**
 * A script is a list of operations, and it is an operation as well.
 */
public class Script implements ScriptOperation {
    private List<ScriptOperation> operations;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        Value last=Value.NULL_VALUE;
        for(ScriptOperation op:operations) {
            last=op.execute(ctx);
            ctx.setLastExecutionResult(last);
        }
        ctx.setLastExecutionResult(last);
        return last;
    }

    public List<ScriptOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<ScriptOperation> s) {
        this.operations=s;
    }

    public static Script parse(OperationRegistry reg,JsonNode node) {
        List<ScriptOperation> list=new ArrayList<>();
        if(node instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=node.elements();itr.hasNext();) {
                JsonNode el=itr.next();
                if(el instanceof ArrayNode) {
                    list.add(parse(reg,el));
                } else if(el instanceof ObjectNode) {
                    list.add(reg.get( (ObjectNode)el ));
                } else {
                    throw Error.get(ScriptErrors.ERR_MALFORMED_SCRIPT,el.toString());
                }
            }
        } else if(node instanceof ObjectNode) {
            list.add(reg.get((ObjectNode)node));
        } else
            throw Error.get(ScriptErrors.ERR_MALFORMED_SCRIPT,node.toString());
        Script ret=new Script();
        ret.setOperations(list);
        return ret;
    }
}
