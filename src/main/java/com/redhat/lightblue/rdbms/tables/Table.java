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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class Table {

    private final String name;
    final Map<String,Column> columns=new HashMap<>();
    final List<ForeignKey> foreignKeys=new ArrayList<>();
    final PrimaryKey primaryKey;

    public Table(String name) {
        this.name=name.toUpperCase();
        this.primaryKey=null;
    }

    public Table(String name,PrimaryKey pkey) {
        this.name=name.toUpperCase();
        this.primaryKey=pkey;
    }
    
    /**
     * Returns the table name. If table needs to be accessed using a
     * schema.table, then this name includes the schema as well, and
     * any reference to the table should include the schema name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a column with the given name
     */
    public Column getColumn(String name) {
        return columns.get(name.toUpperCase());
    }

    /**
     * Sets a column
     */
    public void setColumn(Column col) {
        columns.put(col.getName(),col);
    }

    /**
     * Returns column names
     */
    public String[] getColumnNames() {
        Collection<String> s=columns.keySet();
        return s.toArray(new String[s.size()]);
    }

    /**
     * Returns all the columns
     */
    public Column[] getColumns() {
        Collection<Column> cols=columns.values();
        return cols.toArray(new Column[cols.size()]);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Returns an instance to the foreign keys list of this table
     */
    public List<ForeignKey> getForeignKeysList() {
        return foreignKeys;
    }

    /**
     * Sets all column values to null
     */
    public void resetColumnValues() {
        for(Column c:columns.values())
            c.setValue(null);
    }

    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        bld.append(name).append("(");
        boolean first=true;
        for(Column col:columns.values()) {
            if(first)
                first=false;
            else
                bld.append(',');
            bld.append(col.toString());
        }
        bld.append(")");
        if(primaryKey!=null) 
            bld.append(" PK= ").append(primaryKey.toString());
        if(!foreignKeys.isEmpty())
            bld.append(" FK= ").append(foreignKeys.toString());
        return bld.toString();
    }
}
