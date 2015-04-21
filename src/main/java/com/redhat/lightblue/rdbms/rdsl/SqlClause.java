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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;

/**
 * Represents a SQL clause with optional bindings
 * <pre>
 *   { clause: sqlClause, bindings:[binding1,...]}
 * </pre>
 */
public class SqlClause {

    private String clause;
    private Bindings bindings;

    public SqlClause() {
    }

    public SqlClause(String clause,Bindings bindings) {
        this.clause=clause;
        this.bindings=bindings;
    }

    public String getClause() {
        return clause;
    }

    public void setClause(String c) {
        this.clause=c;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public void setBindings(Bindings b) {
        bindings=b;
    }

    @Override
    public String toString() {
        if(bindings==null)
            return clause;
        else
            return clause+" "+bindings;
    }

    public static SqlClause parseClause(ObjectNode clauseNode) {
        JsonNode x=clauseNode.get("clause");
        if(x==null)
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"clause");
        String clause=x.asText();
        x=clauseNode.get("bindings");
        Bindings bindings;
        if(x instanceof ArrayNode)
            bindings=Bindings.parseBindings((ArrayNode)x);
        else if(x!=null)
            throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,clauseNode.asText());
        else
            bindings=null;
        return new SqlClause(clause,bindings);
    }

}
