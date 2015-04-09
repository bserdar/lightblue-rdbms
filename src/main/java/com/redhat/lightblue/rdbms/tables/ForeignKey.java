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

import java.util.List;
import java.util.ArrayList;

public class ForeignKey {
    private final Table sourceTable;
    private final Table foreignTable;
    private final ArrayList<String> sourceColumns=new ArrayList<>();
    private final boolean notNull;

    public ForeignKey(Table from,
                      Table to,
                      List<String> cols,
                      boolean notNull) {
        this.sourceTable=from;
        this.foreignTable=to;
        for(String x:cols)
            this.sourceColumns.add(x.toUpperCase());
        this.notNull=notNull;
    }

    /**
     * Returns the table declaring the foreign key
     */
    public Table getSourceTable() {
        return sourceTable;
    }

    /**
     * Returns the foreign table
     */
    public Table getForeignTable() {
        return foreignTable;
    }

    /**
     * Returns the columns in the source table that should be equal
     * to the primary key of the foreign table
     */
    public List<String> getSourceColumns() {
        return (List<String>)sourceColumns.clone();
    }

    /**
     * Returns if the foreign relationship is optional or not
     */
    public boolean isNotNull() {
        return notNull;
    }

    @Override
    public String toString() {
        return "("+foreignTable.getName()+":"+sourceColumns+" notNull:"+notNull+")";
    }
}
