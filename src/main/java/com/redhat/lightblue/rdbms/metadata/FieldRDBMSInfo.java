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
package com.redhat.lightblue.rdbms.metadata;

import java.io.Serializable;

/**
 * This structure represents the RDBMS mappings for a field in
 * metadata. This is how you get the RDBMS info for a field:
 *
 * <pre>
 *    FieldRDBMSInfo fieldInfo=(FieldRDMSInfo)field.getProperties().get("rdbms");
 * </pre>
 */
public class FieldRDBMSInfo implements Serializable {

    private static final long serialVersionUID = 1l;

    private String tableName;
    private String columnName;
    private String writeFilter;
    private String readFilter;

    public FieldRDBMSInfo() {
    }

    public FieldRDBMSInfo(String tableName,
                          String columnName,
                          String writeFilter,
                          String readFilter) {
        this.tableName=tableName;
        this.columnName=columnName;
        this.writeFilter=writeFilter;
        this.readFilter=readFilter;
    }

    public  String getTableName() {
        return tableName;
    }

    public void setTableName(String s) {
        tableName=s;
    }

    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String s) {
        columnName=s;
    }

    public String getWriteFilter() {
        return writeFilter;
    }

    public void setWriteFilter(String s) {
        writeFilter=s;
    }

    public String getReadFilter() {
        return readFilter;
    }

    public void setReadFilter(String s) {
        readFilter=s;
    }
}