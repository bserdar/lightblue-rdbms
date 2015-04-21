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

import java.util.Map;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.Field;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

public class MetadataUtils {
    
    /**
     * Returns the field name for the given column
     *
     * @param md The metadata
     * @param col The column
     *
     * @return The metadata field name, or null if no mapping is found
     */
    public static Path findFieldForColumn(EntityMetadata md,Column col) {
        return findFieldForColumn(md.getEntitySchema(),col);
    }

    /**
     * Returns the field name for the given column
     *
     * @param sch The schema
     * @param col The column
     *
     * @return The metadata field name, or null if no mapping is found
     */
    public static Path findFieldForColumn(EntitySchema sch,Column col) {
        Map<String,Table> tables=(Map<String,Table>)sch.getProperties().get("rdbms");
        String theTable;
        if(tables.size()==1)
            theTable=tables.keySet().iterator().next();
        else
            theTable=null;
        FieldCursor cursor=sch.getFieldCursor();
        while(cursor.next()) {
            FieldTreeNode field=cursor.getCurrentNode();
            Map<String,Object> properties;
            if(field instanceof Field) {
                properties=((Field)field).getProperties();
            } else if(field instanceof ArrayElement) {
                properties=((ArrayElement)field).getProperties();
            } else
                properties=null;
            if(properties!=null) {
                FieldRDBMSInfo finfo=(FieldRDBMSInfo)properties.get("rdbms");
                if(finfo!=null) {
                    String tableName=finfo.getTableName();
                    if(tableName==null)
                        tableName=theTable;
                    if(tableName!=null) {
                        if(tableName.equals(col.getTable().getName())&&
                           col.getName().equals(finfo.getColumnName()))
                            return field.getFullPath();
                    }
                }
            }
        }
        return null;
    }
}

