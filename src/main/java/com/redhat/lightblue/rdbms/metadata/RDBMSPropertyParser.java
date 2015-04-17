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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.parser.PropertyParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;
import com.redhat.lightblue.rdbms.tables.ForeignKey;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;

import com.redhat.lightblue.rdbms.dialect.AbstractDialect;

/**
 * RDBMS property parser. Parses a property named "rdbms" in schema,
 * or within a field definition. Since it doesn't know the context it
 * is called in, if the "rdbms" object contains "tables" element, it
 * parses the table definitions from it, otherwise it assumes this is
 * a field definition, and parses FieldRDBMSInfo.
 *
 * Once parsing is done, the tables are accessible in a
 * Map<String,Table> map at schema level, and each field RDBMS info is
 * accessible as a FieldRDBMSInfo object at field level.
 * 
 * <pre>
 *    Map<String,Table> tables=(Map<String,Table>)entityMetadata.getSchema().getProperties().get("rdbms");
 *    ...
 *    FieldRDBMSInfo fieldInfo=(FieldRDMSInfo)field.getProperties().get("rdbms");
 * </pre>
 */
public class RDBMSPropertyParser<T> extends PropertyParser<T> {

    private static final Logger LOGGER=LoggerFactory.getLogger(RDBMSPropertyParser.class);
    
    public static final String ERR_INVALID_TABLE_REFERENCE="rdbms:metadata:parser:invalid-table-reference";
    public static final String ERR_INVALID_PRIMARY_KEY_REFERENCE="rdbms:metadata:parser:invalid-primary-key-reference";
    public static final String ERR_NO_COLUMNS="rdbms:metadata:parser:no-columns";
    public static final String ERR_INVALID_COLUMN="rdbms:metadata:parser:invalid-column";
    public static final String ERR_INVALID_JDBC_TYPE="rdbms:metadata:parser:invalid-jdbc-type";

    private static final class FKey {
        Table sourceTable;
        String foreignTable;
        List<String> sourceColumns;
        boolean notNull;
    }

    @Override
    public Object parse(String name, MetadataParser<T> p, T node) {
        LOGGER.debug("parsing rdbms metadata");
        List<T> tables = p.getObjectList(node,"tables");
        if(tables!=null) {
            LOGGER.debug("parsing tables");
            List<FKey> foreignKeyList=new ArrayList<>();
            Map<String,Table> tableMap=new HashMap<>();
            for(T table:tables) {
                Table t=parseTable(p,table,foreignKeyList);
                LOGGER.debug("parsed {}",t.getName());
                tableMap.put(t.getName(),t);
            }
            linkForeignKeys(tableMap,foreignKeyList);
            return tableMap;
        } else {
            FieldRDBMSInfo fieldInfo=new FieldRDBMSInfo();
            fieldInfo.setTableName(p.getStringProperty(node,"table"));
            fieldInfo.setColumnName(p.getStringProperty(node,"column"));
            fieldInfo.setReadFilter(p.getStringProperty(node,"readFilter"));
            fieldInfo.setWriteFilter(p.getStringProperty(node,"writeFilter"));
            LOGGER.debug("Parsed field mapping for {}.{}",fieldInfo.getTableName(),fieldInfo.getColumnName());
            return fieldInfo;
        }
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, Object object) {
        if(object instanceof FieldRDBMSInfo) {
            FieldRDBMSInfo r=(FieldRDBMSInfo)object;
            if(r.getTableName()!=null)
                p.putString(emptyNode,"table",r.getTableName());
            if(r.getColumnName()!=null)
                p.putString(emptyNode,"column",r.getColumnName());
            if(r.getReadFilter()!=null)
                p.putString(emptyNode,"readFilter",r.getReadFilter());
            if(r.getWriteFilter()!=null)
                p.putString(emptyNode,"writeFilter",r.getWriteFilter());
        } else if(object instanceof Map) {
            Object array=p.newArrayField(emptyNode,"tables");
            for(Table table: ((Map<String,Table>)object).values() ) {
                p.addObjectToArray(array,convertTable(p,emptyNode,table));
            }
        }
    }

    // After all the tables are parsed, create ForeignKey objects
    // linking source tables to foreign tables
    private void linkForeignKeys(Map<String,Table> tables,
                                 List<FKey> foreignKeys) {
        for(FKey fkey:foreignKeys) {
            // Find the foreign table
            Table foreignTable=tables.get(fkey.foreignTable);
            if(foreignTable==null)
                throw Error.get(ERR_INVALID_TABLE_REFERENCE,fkey.foreignTable);
            // Make sure foreign primary key has the same number of columns
            if(foreignTable.getPrimaryKey().size()!=fkey.sourceColumns.size())
                throw Error.get(ERR_INVALID_PRIMARY_KEY_REFERENCE,fkey.foreignTable);
            ForeignKey foreignKey=new ForeignKey(fkey.sourceTable,foreignTable,fkey.sourceColumns,fkey.notNull);
            fkey.sourceTable.getForeignKeysList().add(foreignKey);
        }
    }

    private Table parseTable(MetadataParser<T> p,T tableNode,List<FKey> foreignKeyList) {
        String tableName=p.getRequiredStringProperty(tableNode,"table");
        List<T> columnList=p.getObjectList(tableNode,"columns");
        if(columnList==null||columnList.isEmpty())
            throw Error.get(ERR_NO_COLUMNS,tableName);
        List<String> primaryKeyCols=p.getStringList(tableNode,"primaryKey");
        PrimaryKey primaryKey=primaryKeyCols==null||primaryKeyCols.isEmpty()?null:new PrimaryKey(primaryKeyCols);
        Table table=new Table(tableName,primaryKey);
        for(T col:columnList) {
            parseColumn(p,table,col);
        }
        // Make sure primary key is valid
        if(primaryKey!=null) {
            for(String col:primaryKey.get())
                if(table.getColumn(col)==null)
                    throw Error.get(ERR_INVALID_COLUMN,col);
        }

        List<T> fkeyList=p.getObjectList(tableNode,"foreignKeys");
        if(fkeyList!=null) {
            // Collect all foreign keys in a temp list. Once all
            // tables are processed, link the foreign keys to their
            // table and validate them
            for(T fkey:fkeyList) {
                FKey fk=new FKey();
                fk.sourceTable=table;
                fk.foreignTable=p.getRequiredStringProperty(fkey,"table").toUpperCase();
                Boolean b=(Boolean)p.getValueProperty(fkey,"notNull");
                fk.notNull=b!=null?b:false;
                fk.sourceColumns=p.getStringList(fkey,"columns");
                foreignKeyList.add(fk);
            }
        }
        return table;
    }

    private void parseColumn(MetadataParser<T> p,Table table,T colNode) {
        String name=p.getRequiredStringProperty(colNode,"column");
        Column c=new Column(table,name);
        String x=p.getStringProperty(colNode,"nativeType");
        if(x!=null)
            c.setNativeType(x);
        x=p.getStringProperty(colNode,"jdbcType");
        if(x!=null) {
            Integer t=AbstractDialect.parseJDBCType(x);
            if(t==null)
                throw Error.get(ERR_INVALID_JDBC_TYPE,x);
            c.setJdbcType(t);
        }
    }

    private T convertTable(MetadataParser<T> p,T emptyNode,Table table) {
        T tableNode=p.newNode();

        p.putString(tableNode,"table",table.getName());
        PrimaryKey primaryKey=table.getPrimaryKey();
        if(primaryKey!=null) {
            Object pkey=p.newArrayField(emptyNode,"primaryKey");
            int n=primaryKey.size();
            for(int i=0;i<n;i++) {
                p.addStringToArray(pkey,primaryKey.get(i));
            }
        }

        if(!table.getForeignKeysList().isEmpty()) {
            Object fkeys=p.newArrayField(emptyNode,"foreignKeys");
            for(ForeignKey f:table.getForeignKeysList()) {
                T fkey=p.newNode();
                p.putString(fkey,"table",f.getForeignTable().getName());
                p.putValue(fkey,"notNull",f.isNotNull()?Boolean.TRUE:Boolean.FALSE);
                Object cols=p.newArrayField(fkey,"columns");
                for(String x:f.getSourceColumns())
                    p.addStringToArray(cols,x);
            }
        }

        return tableNode;
    }
}
