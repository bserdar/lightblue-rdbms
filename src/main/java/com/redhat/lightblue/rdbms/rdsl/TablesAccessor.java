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

import java.util.Map;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

/**
 * Provides access to the map of tables and their columns as variables
 */
public class TablesAccessor implements VariableAccessor {
    
    private final Map<String,Table> tables;

    /**
     * Construct accessor for the given table map
     */
    public TablesAccessor(Map<String,Table> tables) {
        this.tables=tables;
    }

    @Override
    public Value getVarValue(Path var) {
        Object ref=resolve(var);
        if(ref!=null) {
            if(ref instanceof Column) {
                return new Value(ValueType.primitive,((Column)ref).getValue());
            } else
                throw Error.get(ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ERR_UNKNOWN_COLUMN,var.toString());
    }

    @Override
    public ValueType getVarType(Path var) {
        Object ref=resolve(var);
        if(ref!=null) {
            if(ref instanceof Column) {
                return ValueType.primitive;
            } else
                throw Error.get(ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ERR_UNKNOWN_COLUMN,var.toString());
    }

    @Override
    public void setVarValue(Path var,Value value) {
        Object ref=resolve(var);
        if(ref!=null) {
            if(ref instanceof Column) {
                if(value.getType() == ValueType.lob) {
                    ((Column)ref).setValue(value.getLobValue());
                } else if(value.getType() == ValueType.primitive) {
                    ((Column)ref).setValue(value.getPrimitiveValue());
                } else
                    throw Error.get(ERR_INVALID_ASSIGNMENT,var.toString());
            } else
                throw Error.get(ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ERR_UNKNOWN_COLUMN,var.toString());
    }

    /**
     * Returns a Table, Column, or null if the variable cannot be resolved
     */
    private Object resolve(Path var) {
        // If var has one segment, it can only be pointing to a table
        Object ret=null;
        if(var.numSegments()==1) {
            ret=tables.get(var.head(0));
        } else if(var.numSegments()>1) {
            // var can be pointing to a table, or the n-1 prefix is a table, and the last
            // segment is a column
            Table t=tables.get(var.toString());
            if(t!=null)
                ret=t;
            else {
                t=tables.get(var.prefix(-1).toString());
                if(t!=null) {
                    ret=t.getColumn(var.tail(0));
                    if(ret==null)
                        throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,var.toString());
                }
            }
        }
        return ret;
    }
}
