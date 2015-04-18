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
package com.redhat.lightblue.rdbms.dialect;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;

import com.redhat.lightblue.rdbms.rdsl.Value;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

/**
 * The interface that defines how SQL translations are done based on
 * the actual database backend
 */
public interface Dialect {

    /**
     * Sets a parameter in a prepared statement
     *
     * @param stmt The prepared statement
     * @param index JDBC parameter index
     * @param sqlType Type sql type of the column, if known. It can be
     * null if not specified by metadata
     * @param obj The value to set
     */
    void setParameter(PreparedStatement stmt,int index,Integer sqlType,Object obj);

    /**
     * Parse a type name and return the matching JDBC sql type for it (java.sql.Types) 
     */
    Integer parseType(String typeName);

    /**
     * Registers an OUT parameter
     */
    void registerOutParameter(CallableStatement stmt,int index,int sqlType);
    
    /**
     * Retrieves an out parameter value from a callable statement after the statement executed
     */
    Value getOutValue(CallableStatement stmt,int index,int sqlType);

    /**
     * Retrieves a value from a result set
     */
    Value getResultSetValue(ResultSet rs,int index);

    /**
     * Writes an INSERT statement for the given table and columns
     */
    String getInsertRowStmt(Table table,Column[] columns);
}

