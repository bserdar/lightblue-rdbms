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
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.ArrayField;

/**
 * ListValue adapter for Json ArrayNode nodes. The underlying object is read-only
 */
public class JsonArrayAdapter implements ListValue {
    private final ArrayNode node;
    private final ArrayField nodeMd;

    private class _Iterator implements Iterator<Value> {

        private final Iterator<JsonNode> itr;

        public _Iterator(Iterator<JsonNode> itr) {
            this.itr=itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public void remove() {
        }

        @Override
        public Value next() {
            JsonNode elementNode=itr.next();
            return DocumentFieldAccessor.getValueForField(nodeMd==null?null:nodeMd.getElement(),elementNode);
        }
    }

    /**
     * Constructs a ListValue for the array node, and the field
     * metadata. The metadata belongs to the array field. Metadata is
     * optional, but should be non-null for all Json arrays obtained
     * from documents.
     */    
    public JsonArrayAdapter(ArrayNode node,ArrayField fieldMd) {
        this.node=node;
        this.nodeMd=fieldMd;
    }

    @Override
    public boolean isEmpty() {
        return node.size()==0;
    }

    @Override
    public Iterator<Value> getValues() {
        return new _Iterator(node.elements());
    }
  
}
