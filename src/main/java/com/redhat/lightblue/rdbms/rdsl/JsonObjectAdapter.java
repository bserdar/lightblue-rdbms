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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.FieldTreeNode;

/**
 * MapValue adapter for Json ObjectNode nodes
 */
public class JsonObjectAdapter implements MapValue {
    private final ObjectNode node;
    private final ObjectField nodeMd;
    
    /**
     * Constructs a MapValue for the object node, and the field
     * metadata. The metadata belongs to the object field. Metadata is
     * optional, but should be non-null for all Json objects obtained
     * from documents.
     */    
    public JsonObjectAdapter(ObjectNode node,ObjectField fieldMd) {
        this.node=node;
        this.nodeMd=fieldMd;
    }

    @Override
    public boolean isEmpty() {
        return node.size()==0;
    }

    @Override
    public Iterator<String> getNames() {
        return node.fieldNames();
    }

    @Override
    public Value getValue(String name) {
        FieldTreeNode childMd;
        if(nodeMd!=null) {
            childMd=nodeMd.getFields().getField(name);
        } else {
            childMd=null;
        }
        JsonNode childNode=node.get(name);
        return DocumentFieldAccessor.getValueForField(childMd,childNode);
    }
}
