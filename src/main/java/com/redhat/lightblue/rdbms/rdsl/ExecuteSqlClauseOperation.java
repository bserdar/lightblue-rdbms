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

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;

public class ExecuteSqlClauseOperation implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(ExecuteSqlClauseOperation.class);

    public static final String NAMEQ="sql";
    public static final String NAMECALL="sqlcall";
    public static final String NAMES[]={NAMEQ,NAMECALL};

    public static final ScriptOperationFactory FACTORY=new ScriptOperationFactory() {     
            @Override
            public String[] operationNames() {
                return NAMES;
            }
            
            @Override
            public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
                ExecuteSqlClauseOperation op;
                ObjectNode argNode=(ObjectNode)node.get(NAMEQ);
                if(argNode==null) {
                    argNode=(ObjectNode)node.get(NAMECALL);
                    op=new ExecuteSqlClauseOperation(NAMECALL);
                } else {
                    op=new ExecuteSqlClauseOperation(NAMEQ);
                }
                
                JsonNode x=argNode.get("clause");
                if(x==null)
                    throw Error.get(ScriptErrors.ERR_MISSING_ARG,"clause");
                op.clause=x.asText();
                
                x=argNode.get("bindings");
                if(x instanceof ArrayNode) {
                    op.bindings=Bindings.parseBindings( (ArrayNode)x); 
                } else if(x!=null)
                    throw Error.get(ScriptErrors.ERR_MALFORMED_OPERATION,x.toString());
                return op;
            }
        };

    private final String name;
    private String clause;
    private Bindings bindings;
    
    public ExecuteSqlClauseOperation(String name) {
        this.name=name;
    }

    public ExecuteSqlClauseOperation(String name,String clause,Bindings bindings) {
        this(name);
        this.clause=clause;
        this.bindings=bindings;
    }

    public String getClause() {
        return clause;
    }

    public Bindings getBindings() {
        return bindings;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        if(name.equals(NAMEQ)) {
            return executeQ(ctx);
        } else {
            executeCall(ctx);
            return Value.NULL_VALUE;
        }
    }

    public Value executeQ(ScriptExecutionContext ctx) {
        try {
            LOGGER.debug("Preparing query: {}",clause);
            PreparedStatement stmt=ctx.getConnection().prepareStatement(clause);
            if(bindings!=null) {
                LOGGER.debug("Setting parameters");
                bindings.setParameters(ctx,stmt);
            }
            LOGGER.debug("Executing query: {}",clause);
            if(stmt.execute()) {
                LOGGER.debug("Getting result set");
                ResultSet rs=stmt.getResultSet();
                LOGGER.debug("Returning result set");
                return new Value(new ResultSetAdapter(rs,ctx.getDialect()));
            } else {
                LOGGER.debug("Getting update count");
                int updateCount=stmt.getUpdateCount();
                LOGGER.debug("Update count:{}",updateCount);
                return new Value(updateCount);
            }
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            LOGGER.error("Error while executing "+clause,x);
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x.toString());
        }
    }

    public void executeCall(ScriptExecutionContext ctx) {
        try {
            LOGGER.debug("Preparing call: {}",clause);
            CallableStatement stmt=ctx.getConnection().prepareCall(clause);
            if(bindings!=null) {
                LOGGER.debug("Setting parameters");
                bindings.setParameters(ctx,stmt);
                LOGGER.debug("Registering out parameters");
                bindings.registerOutParameters(ctx,stmt);
            }
            LOGGER.debug("Executing call: {}",clause);
            stmt.execute();
            if(bindings!=null) {
                LOGGER.debug("Retrieving out parameter values");
                bindings.retrieveOutValues(ctx,stmt);
            }
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            LOGGER.error("Error while executing "+clause,x);
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x.toString());
        }
    }
}
