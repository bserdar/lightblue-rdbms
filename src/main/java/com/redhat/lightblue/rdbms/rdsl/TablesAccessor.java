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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;

/**
 * Provides access to the map of tables and their columns as variables
 */
public class TablesAccessor implements VariableAccessor {

    private static final Logger LOGGER=LoggerFactory.getLogger(TablesAccessor.class);
    
    private final Map<String,Table> tables;

    /**
     * Construct accessor for the given table map
     */
    public TablesAccessor(Map<String,Table> tables) {
        this.tables=tables;
    }

    @Override
    public Value getVarValue(Path var) {
        LOGGER.debug("get {}",var);
        Object ref=resolve(var);
        LOGGER.debug("resolved: {}",ref);
        if(ref!=null) {
            if(ref instanceof Column) {
                return new Value(ValueType.primitive,((Column)ref).getValue());
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ScriptErrors.ERR_NO_COLUMN,var.toString());
    }

    @Override
    public ValueType getVarType(Path var) {
        LOGGER.debug("get {}",var);
        Object ref=resolve(var);
        LOGGER.debug("resolved: {}",ref);
        if(ref!=null) {
            if(ref instanceof Column) {
                return ValueType.primitive;
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ScriptErrors.ERR_NO_COLUMN,var.toString());
    }

    @Override
    public void setVarValue(Path var,Value value) {
        LOGGER.debug("set {}",var);
        Object ref=resolve(var);
        LOGGER.debug("resolved: {}",ref);
        if(ref!=null) {
            if(ref instanceof Column) {
                if(value.getType() == ValueType.lob) {
                    ((Column)ref).setValue(value.getValue());
                } else if(value.getType() == ValueType.primitive) {
                    ((Column)ref).setValue(value.getValue());
                } else
                    throw Error.get(ScriptErrors.ERR_INVALID_ASSIGNMENT,var.toString());
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_REFERENCE_TO_TABLE,var.toString());
        } else
            throw Error.get(ScriptErrors.ERR_NO_COLUMN,var.toString());
    }

    /**
     * Returns a Table, Column, or null if the variable cannot be resolved
     */
    private Object resolve(Path var) {
        LOGGER.debug("resolving {}",var);
        // If var has one segment, it can only be pointing to a table
        Object ret=null;
        if(var.numSegments()==1) {
            ret=tables.get(var.head(0).toUpperCase());
        } else if(var.numSegments()>1) {
            // var can be pointing to a table, or the n-1 prefix is a table, and the last
            // segment is a column
            LOGGER.debug("Getting table {}",var);
            Table t=tables.get(var.toString().toUpperCase());
            if(t!=null)
                ret=t;
            else {
                Path pfix=var.prefix(-1);
                LOGGER.debug("Getting table {}",pfix);
                t=tables.get(pfix.toString().toUpperCase());
                if(t!=null) {
                    LOGGER.debug("Getting column {}",var.tail(0));
                    ret=t.getColumn(var.tail(0));
                    if(ret==null)
                        throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,var.toString());
                }
            }
        } else
            throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,var.toString());
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(Map.Entry<String,Table> entry:tables.entrySet()) {
            bld.append(entry.getValue().toString()).append('\n');
        }
        return bld.toString();
    }
}
