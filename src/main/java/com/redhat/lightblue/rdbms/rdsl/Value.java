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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Wrapper for values within a script. Contains a valueType, and the
 * actual value with that type
 */
public class Value {

    public static final Value NULL_VALUE=new Value(ValueType.primitive,null);

    private final Object value;
    private final ValueType type;

    /**
     * Constructs a Value with the given type and value
     */
    public Value(ValueType type,Object value) {
        this.type=type;
        this.value=value;
    }

    /**
     * Constructs a List value
     */
    public Value(ListValue lv) {
        this(ValueType.list,lv);
    }

    /**
     * Constructs a map value
     */
    public Value(MapValue mv) {
        this(ValueType.map,mv);
    }

    /**
     * Returns value type
     */
    public ValueType getType() {
        return type;
    }

    /**
     * Returns the value, whatever its type
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the value as a ListValue. It the value is not a
     * ListValue, a TypeCastException will be thrown
     */
    public ListValue getListValue() {
        return (ListValue)value;
    }

    /**
     * Returns the value as a MapValue. If the value is not a
     * MapValue, a TypeCastException will be thrown
     */
    public MapValue getMapValue() {
        return (MapValue)value;
    }

    @Override
    public String toString() {
        return type+"("+value+")";
    }

    /**
     * Returns a value object representing the json node
     */
    public static Value toValue(JsonNode node) {
        if(node instanceof ObjectNode) {
            return new Value(new JsonObjectAdapter( (ObjectNode)node, null ));
        } else if(node instanceof ArrayNode) {
            return new Value(new JsonArrayAdapter( (ArrayNode)node, null ) );
        } else {
            if(node.isNumber()) {
                return new Value(ValueType.primitive,node.numberValue());
            } else if(node.isBoolean()) {
                return new Value(ValueType.primitive,node.booleanValue());
            } else {
                return new Value(ValueType.primitive,node.asText());
            }
        }
    }
}
