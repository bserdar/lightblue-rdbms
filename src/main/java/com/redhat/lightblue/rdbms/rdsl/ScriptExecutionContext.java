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

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.rdbms.tables.Table;

import com.redhat.lightblue.rdbms.dialect.Dialect;

/**
 * Contains all the variables and database information required to
 * execute scripts
 */
public class ScriptExecutionContext implements VariableAccessor {

    private static final Logger LOGGER=LoggerFactory.getLogger(ScriptExecutionContext.class);

    private final VariableAccessor scope;
    private final ScriptExecutionContext parent;
    private Value lastExecutionResult=null;
    private final ScriptOperationFactory opFactory;
    private Dialect dialect;
    private Connection connection;

    /**
     * Constructs the top-level execution context
     */
    public ScriptExecutionContext(ScriptOperationFactory factory) {
        this(null,factory);
    }

    /**
     * Constructs a nested scope
     */
    public ScriptExecutionContext(ScriptExecutionContext parentScope,
                                  ScriptOperationFactory factory) {
        scope=new TopLevelVariableAccessor();
        this.parent=parentScope;       
        this.opFactory=factory;
        if(parentScope!=null) {
            this.dialect=parentScope.dialect;
            this.connection=parentScope.connection;
        }
    }

    public ScriptOperationFactory getOperationFactory() {
        return opFactory;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect d) {
        dialect=d;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection c) {
        connection=c;
    }

    /**
     * Lookup variable in the current scope. Start with this scope, move up
     */
    @Override
    public Value getVarValue(Path var) {
        Error.push(var.toString());
        LOGGER.debug("get {} enter",var);
        Value ret;
        try {
            try {
                ret=scope.getVarValue(var);
            } catch (Error x) {
                if(parent!=null)
                    ret=parent.getVarValue(var);
                else
                    throw x;
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("get {}:{}",var,ret);
        return ret;
    }

    /**
     * Lookup variable in the current scope. Start with this scope, move up
     */
    @Override
    public ValueType getVarType(Path var) {
        Error.push(var.toString());
        LOGGER.debug("getType {} enter",var);
        ValueType ret;
        try {
            try {
                ret=scope.getVarType(var);
            } catch (Error x) {
                if(parent!=null)
                    ret=parent.getVarType(var);
                else
                    throw x;
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("getType {}:{}",var,ret);
        return ret;
    }


    /**
     * defines a new variable, or modifies an existing one
     */
    @Override
    public void setVarValue(Path p,Value value) {
        Error.push(p.toString());
        LOGGER.debug("set {}:{} enter",p,value);
        try {
            if(p.numSegments()==1) {
                scope.setVarValue(p,value);
            } else {
                if(parent!=null)
                    parent.setVarValue(p,value);
                else
                    scope.setVarValue(p,value);
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("set {} return",p);
    }

    /**
     * Creates a nested execution context for scripts contained within other scripts
     */
    public ScriptExecutionContext newContext() {
        return new ScriptExecutionContext(this,opFactory);
    }

    /**
     * Value of the last execution
     */
    public Value getLastExecutionResult() {
        return lastExecutionResult;
    }

    /**
     * Value of the last execution
     */
    public void setLastExecutionResult(Value result) {
        lastExecutionResult=result;
    }

    @Override
    public String toString() {
        StringBuilder b=new StringBuilder();
        if(parent!=null)
            b.append(parent.toString());
        b.append("\n----\n");
        b.append(scope.toString());
        return b.toString();
    }


    /**
     * Creates an execution context for insertion. Defines $tables and $document variables.
     */
    public static ScriptExecutionContext getInstanceForInsertion(Map<String,Table> tables,
                                                                 JsonDoc document,
                                                                 EntityMetadata md,
                                                                 ScriptOperationFactory opFactory) {
        ScriptExecutionContext ctx=new ScriptExecutionContext(opFactory);
        ((TopLevelVariableAccessor)ctx.scope).set("$tables",new TablesAccessor(tables));
        ((TopLevelVariableAccessor)ctx.scope).set("$document",new DocumentFieldAccessor(md,document));
        return ctx;
    }

    /**
     * Creates an execution context for update. Defines $tables, $document, and $olddocument variables.
     */
    public static ScriptExecutionContext getInstanceForUpdate(Map<String,Table> tables,
                                                              JsonDoc oldDoc,
                                                              JsonDoc newDoc,
                                                              EntityMetadata md,
                                                              ScriptOperationFactory opFactory) {
        ScriptExecutionContext ctx=new ScriptExecutionContext(opFactory);
        ((TopLevelVariableAccessor)ctx.scope).set("$tables",new TablesAccessor(tables));
        ((TopLevelVariableAccessor)ctx.scope).set("$document",new DocumentFieldAccessor(md,newDoc));
        ((TopLevelVariableAccessor)ctx.scope).set("$olddocument",new DocumentFieldAccessor(md,oldDoc));
        return ctx;
    }

    /**
     * Creates an execution context for deletion. Defines $tables and $docId variables.
     */
    public static ScriptExecutionContext getInstanceForDeletion(Map<String,Table> tables,
                                                                JsonNode docId,
                                                                ScriptOperationFactory opFactory) {
        ScriptExecutionContext ctx=new ScriptExecutionContext(opFactory);
        ((TopLevelVariableAccessor)ctx.scope).set("$tables",new TablesAccessor(tables));
        if(docId instanceof ObjectNode) {
            ((TopLevelVariableAccessor)ctx.scope).set("$docId",new TempVariableAccessor(new Value(new TempVarMapValueAdapter(new JsonObjectAdapter( (ObjectNode)docId,null )))));
        } else {
            ((TopLevelVariableAccessor)ctx.scope).set("$docId",new TempVariableAccessor(Value.toValue(docId)));
        }
        return ctx;
    }                                                         
}
