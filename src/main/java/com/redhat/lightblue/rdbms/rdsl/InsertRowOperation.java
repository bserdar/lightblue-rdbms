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

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

/**
 * Inserts a row to a table
 * <pre>
 *   { insert-row: { table: $tables.table } }
 * </pre>
 */
public class InsertRowOperation implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(InsertRowOperation.class);

    public static final String NAME="insert-row";
    public static final String NAMES[]={NAME};

    public static final ScriptOperationFactory FACTORY=new ScriptOperationFactory() {
        
            @Override
            public String[] operationNames() {
                return NAMES;
            }
            
            @Override
            public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
                ObjectNode argNode=(ObjectNode)node.get(NAME);
                JsonNode x=argNode.get("table");
                if(x==null)
                    throw Error.get(ScriptErrors.ERR_MISSING_ARG,"table");
                Path table=new Path(x.asText());
                return new InsertRowOperation(table);
            }
        };

    private Path tableName;
    
    public InsertRowOperation() {}

    public InsertRowOperation(Path table) {
        this.tableName=table;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Path getTableName() {
        return tableName;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("insert {} begin",tableName);
        Value tablev=ctx.getVarValue(tableName);
        if(tablev.getType()!=ValueType.table) 
            throw Error.get(ScriptErrors.ERR_NOT_A_TABLE,tableName.toString());

        Table table=tablev.getTableValue();
        Column[] nonNullCols=table.getNonNullColumns();
        LOGGER.debug("There are {} non-null columns",nonNullCols.length);
        if(nonNullCols.length==0)
            throw Error.get(ScriptErrors.ERR_ALL_COLS_NULL,tableName.toString());
        
        String insertStmt=ctx.getDialect().getInsertRowStmt(table,nonNullCols);
        LOGGER.debug("insert stmt: {}",insertStmt);
        try {
            PreparedStatement stmt=ctx.getConnection().prepareStatement(insertStmt);
            LOGGER.debug("Setting parameters");
            int index=1;
            for(Column c:nonNullCols) {
                ctx.getDialect().setParameter(stmt,index++,c.getJdbcType(),c.getValue());
            }
            LOGGER.debug("Executing insert statement");
            int result=stmt.executeUpdate();
            LOGGER.debug("Result: {}",result);
            return new Value(result);
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            LOGGER.error("Error during insertOperation", x);
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x.toString());
        }
    }
}
