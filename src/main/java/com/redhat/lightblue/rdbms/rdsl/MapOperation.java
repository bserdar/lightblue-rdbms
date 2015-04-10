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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Field;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

import com.redhat.lightblue.rdbms.metadata.FieldRDBMSInfo;

public class MapOperation implements ScriptOperation, ScriptOperationFactory {

    private static final Logger LOGGER=LoggerFactory.getLogger(MapOperation.class);

    public static final String NAME="$map";
    public static final String NAMES[]={NAME};

    private Path source;
    private Path dest;

    private static class Mapping {
        final FieldTreeNode md;
        final Path relativeName;
        final Column column;

        public Mapping(FieldTreeNode md,
                       Path relativeName,
                       Column column) {
            this.md=md;
            this.relativeName=relativeName;
            this.column=column;
        }
    }
    
    public MapOperation() {}

    public MapOperation(Path source,Path dest) {
        this.source=source;
        this.dest=dest;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
        // Either the source or the destination must be a table, and the other one must be a map
        Value sourceValue=ctx.getVarValue(source);
        Value destValue=ctx.getVarValue(dest);

        if(sourceValue.getType()==ValueType.table &&
           destValue.getType()==ValueType.map)
            mapTable2Doc(sourceValue.getTableValue(),destValue.getMapValue());
        else if(destValue.getType()==ValueType.table&&
                sourceValue.getType()==ValueType.map)
            mapDoc2Table(sourceValue.getMapValue(),destValue.getTableValue());
        else
            throw Error.get(ScriptErrors.ERR_INVALID_MAP_CALL);
        return Value.NULL_VALUE;
    }
    
    static public void mapTable2Doc(Table sourceTable,MapValue destDocument) {
        if(destDocument instanceof JsonObjectAdapter)
            mapTable2Doc(sourceTable,(JsonObjectAdapter)destDocument);
        else
            throw Error.get(ScriptErrors.ERR_NEED_DOCUMENT_FOR_MAP);
    }

    static public void mapDoc2Table(MapValue sourceDocument,Table destTable) {
        if(sourceDocument instanceof JsonObjectAdapter)
            mapDoc2Table((JsonObjectAdapter)sourceDocument,destTable);
        else
            throw Error.get(ScriptErrors.ERR_NEED_DOCUMENT_FOR_MAP);
    }

    static public void mapTable2Doc(Table sourceTable,JsonObjectAdapter destDocument) {
        GetTableColumnsCb cb=new GetTableColumnsCb(sourceTable);
        iterateMd(destDocument.getMd(),cb);
        for(Mapping mapping:cb.map) {
            Object columnValue=mapping.column.getValue();
            // There can't be any arrays on the path to the field
            if(mapping.relativeName.nAnys()>0)
                throw Error.get(ScriptErrors.ERR_COLUMN_MAPPED_TO_ARRAY,mapping.relativeName.toString());
            LOGGER.debug("Setting {} from {}",mapping.relativeName,mapping.column);
            JsonDoc.modify(destDocument.getNode(),mapping.relativeName,
                           mapping.md.getType().toJson(JsonNodeFactory.instance,columnValue),true);
        }
    }

    static public void mapDoc2Table(JsonObjectAdapter sourceDocument,Table destTable) {
        GetTableColumnsCb cb=new GetTableColumnsCb(destTable);
        iterateMd(sourceDocument.getMd(),cb);
        for(Mapping mapping:cb.map) {
            JsonNode docValue=JsonDoc.get(sourceDocument.getNode(),mapping.relativeName);
            LOGGER.debug("Setting {} from {}",mapping.column,mapping.relativeName);
            Object value=mapping.md.getType().fromJson(docValue);
            mapping.column.setValue(value);
        }
    }

    private interface Callback {
        void callback(Path relativePath,FieldTreeNode node,FieldRDBMSInfo finfo);
    }

    private static class GetTableColumnsCb implements Callback {
        private final Table table;
        private final List<Mapping> map=new ArrayList<>();

        public GetTableColumnsCb(Table t) {
            table=t;
        }

        @Override
        public void callback(Path relativePath,FieldTreeNode field,FieldRDBMSInfo finfo) {
            if(Table.clean(finfo.getTableName()).equals(table.getName())) {
                Column c=table.getColumn(finfo.getColumnName());
                if(c==null)
                    throw Error.get(ScriptErrors.ERR_MAP_UNKNOWN_COLUMN,finfo.getColumnName());
                map.add(new Mapping(field,relativePath,c));
            }
        }   

    }
    
    static private void iterateMd(FieldTreeNode root,Callback cb) {
        iterateMd(new MutablePath(),root,cb);
    }
        
    static private void iterateMd(MutablePath p,FieldTreeNode root,Callback cb) {
        if(root instanceof Field) {
            FieldRDBMSInfo finfo=(FieldRDBMSInfo)((Field)root).getProperties().get("rdbms");
            if(finfo!=null)
                cb.callback(p.immutableCopy(),root,finfo);
        }
        if(root.hasChildren()) {
            for(Iterator<? extends FieldTreeNode> itr=root.getChildren();itr.hasNext();) {
                FieldTreeNode child=itr.next();
                p.push(child.getName());
                iterateMd(p,child,cb);
                p.pop();
            }
        }
    }

    @Override
    public String[] operationNames() {
        return NAMES;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        MapOperation newOp=new MapOperation();
        ObjectNode args=(ObjectNode)node.get(NAME);
        JsonNode x=args.get("dest");
        if(x!=null) {
            newOp.dest=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"dest");
        }
        x=args.get("source");
        if(x!=null) {
            newOp.source=new Path(x.asText());
        } else {
            throw Error.get(ScriptErrors.ERR_MISSING_ARG,"source");
        }
        return newOp;
    }
}
