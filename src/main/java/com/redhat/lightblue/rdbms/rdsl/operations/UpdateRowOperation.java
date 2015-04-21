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

import java.util.List;
import java.util.ArrayList;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.ValueType;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.ScriptExecutionContext;
import com.redhat.lightblue.rdbms.rdsl.SqlClause;
import com.redhat.lightblue.rdbms.rdsl.Bindings;
import com.redhat.lightblue.rdbms.rdsl.Binding;
import com.redhat.lightblue.rdbms.rdsl.ValueBinding;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;

import com.redhat.lightblue.rdbms.dialect.Dialect;

/**
 * Updates a row in a table
 * <pre>
 *   { update-row: { table: $tables.table, columns: [col1,...], where: whereClause } }
 * </pre>
 */
public class UpdateRowOperation implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(UpdateRowOperation.class);

    public static final String NAME="update-row";

    private Path tableName;
    private List<String> columns;
    private SqlClause whereClause;
    
    public UpdateRowOperation() {}

    public UpdateRowOperation(Path table,
                              List<String> columns,
                              SqlClause whereClause) {
        this.tableName=table;
        this.columns=columns;
        this.whereClause=whereClause;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Path getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public SqlClause getWhereClause() {
        return whereClause;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        LOGGER.debug("update {} begin",tableName);
        Value tablev=ctx.getVarValue(tableName);
        if(tablev.getType()!=ValueType.table) 
            throw Error.get(ScriptErrors.ERR_NOT_A_TABLE,tableName.toString());
        Table table=tablev.getTableValue();

        LOGGER.debug("preparing update columns");
        Column[] tableColumns=table.getColumns();
        Column[] updateColumns;
        if(columns!=null&&!columns.isEmpty()) {
            List<Column> cols=new ArrayList<>(tableColumns.length);
            for(String colName:columns) {
                Column col=table.getColumn(colName);
                if(col==null)
                    throw Error.get(ScriptErrors.ERR_NO_COLUMN,colName);
                cols.add(col);
            }
            updateColumns=cols.toArray(new Column[cols.size()]);
        } else
            updateColumns=tableColumns;
        LOGGER.debug("updating {} columns", updateColumns.length);

        SqlClause where;
        if(whereClause==null) {
            LOGGER.debug("writing a where clause using primary key");
            PrimaryKey pkey=table.getPrimaryKey();
            if(pkey==null)
                throw Error.get(ScriptErrors.ERR_TABLE_HAS_NO_PKEY,table.getName());
            int n=pkey.size();
            Bindings bindings=new Bindings();
            StringBuilder bld=new StringBuilder();
            for(int i=0;i<n;i++) {
                if(i>0)
                    bld.append(" and ");
                String col=pkey.get(i);
                Column column=table.getColumn(col);
                bld.append(column.getName()).append("=?");
                bindings.add(new ValueBinding(new Value(ValueType.primitive,column.getValue())));
            }
            where=new SqlClause(bld.toString(),bindings);
        } else
            where=whereClause;
        LOGGER.debug("where clause:{}",where);

        Dialect dialect=ctx.getDialect();
        try {
            String stmtString=dialect.getUpdateRowStmt(table,updateColumns,where==null?null:where.getClause());
            LOGGER.debug("update statement:{}",stmtString);
            PreparedStatement stmt=ctx.getConnection().prepareStatement(stmtString);
            
            LOGGER.debug("binding values");
            int paramIndex=1;
            for(Column updateCol:updateColumns) {
                LOGGER.debug("Setting parameter {} to {}",paramIndex,updateCol.getValue());
                dialect.setParameter(stmt,paramIndex,updateCol.getJdbcType(),updateCol.getValue());
                paramIndex++;
            }
            if(where!=null) {
                LOGGER.debug("binding where clause values");
                where.getBindings().setParameters(paramIndex,ctx,stmt);
            }
            LOGGER.debug("executing update");
            int result=stmt.executeUpdate();
            LOGGER.debug("returning {}",result);
            stmt.close();
            return new Value(result);
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            LOGGER.error("error during update",x);
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x.toString());
        }
    }
}
