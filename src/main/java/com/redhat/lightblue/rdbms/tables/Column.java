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
package com.redhat.lightblue.rdbms.tables;

/**
 * Represents a column of a table, and its current value
 */
public class Column {

    private final String name;
    private Object value;
    private Table table;
    private Integer jdbcType;
    private String nativeType;

    public Column(Table table,String name) {
        this.table=table;
        this.name=name.toUpperCase();
        table.columns.put(this.name,this);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value=value;
    }

    public Table getTable() {
        return table;
    }

    /**
     * The SQL type of the column (java.sql.Types). Can be null, meaning unspecified
     */
    public Integer getJdcType() {
        return jdbcType;
    }

    /**
     * The SQL type of the column (java.sql.Types). Can be null, meaning unspecified
     */
    public void setJdbcType(Integer type) {
        this.jdbcType=type;
    }

    /**
     * The native type of the column. Optional information. If specified, this is passed to the dialect
     */
    public String getNativeType() {
        return nativeType;
    }

    /**
     * The native type of the column. Optional information. If specified, this is passed to the dialect
     */
    public void setNativeType(String s) {
        nativeType=s;
    }

    @Override
    public String toString() {
        return table.getName()+"."+name;
    }
}

