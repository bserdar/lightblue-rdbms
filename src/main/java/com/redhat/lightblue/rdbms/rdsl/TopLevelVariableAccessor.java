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
import java.util.HashMap;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * If the first segment of the variable maps to a VariableAccessor,
 * the variable resolution is delegated to that accessor. Otherwise,
 * if the variable has only one segment, it is assumed to be a
 * temporary variable and access is provided by this instance.
 */
public class TopLevelVariableAccessor implements VariableAccessor {

    private final Map<String,Object> children=new HashMap<>();

    /**
     * Retrieves variable value using the first segment of the
     * path. If the first segment points to VariableAccessor,
     * delegates the resolution of the remainder of the name to that
     * accessor. Otherwise, returns that value.
     */
    @Override
    public Value getVarValue(Path variable) {
        Value ret=null;
        if(!variable.isEmpty()) {
            String name=variable.head(0);
            Object head=children.get(name);
            if(head instanceof VariableAccessor) {
                ret=((VariableAccessor)head).getVarValue(variable.suffix(-1));
            } else if(head instanceof Value) {
                ret=(Value)head;
            } else {
                throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,name);
            }
        } else
            throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,"<empty>");
        return ret;
    }

    @Override
    public ValueType getVarType(Path variable) {
        ValueType ret=null;
        if(!variable.isEmpty()) {
            String name=variable.head(0);
            Object head=children.get(name);
            if(head instanceof VariableAccessor) {
                ret=((VariableAccessor)head).getVarType(variable.suffix(-1));
            } else if(head instanceof Value) {
                ret=((Value)head).getType();
            } else {
                throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,name);
            }
        } else
            throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,"<empty>");
        return ret;
    }


    @Override
    public void setVarValue(Path variable,Value value) {
        if(!variable.isEmpty()) {
            String name=variable.head(0);
            Object child=children.get(name);
            if(child instanceof VariableAccessor) {
                ((VariableAccessor)child).setVarValue(variable.suffix(-1),value);
            } else {
                // New temp variable. At initial creation, variable must have only 1 segment
                if(variable.numSegments()>1)
                    throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE,variable.toString());
                // For list and map values, we create a deep copy
                if(value.getType()==ValueType.map)
                    value=new Value(new TempVarMapValueAdapter(value.getMapValue()));
                else if(value.getType()==ValueType.list)
                    value=new Value(new TempVarListValueAdapter(value.getListValue()));
                children.put(name,new TempVariableAccessor(value));
            } 
        } else
            throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,"<empty>");
    }

    /**
     * Registers a new variable accessor with the given name
     */
    public void set(String name,VariableAccessor accessor) {
        children.put(name,accessor);
    }

    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(Map.Entry<String,Object> entry:children.entrySet()) {
            bld.append(entry.getKey()).append(':').append(entry.getValue().toString()).append('\n');
        }
        return bld.toString();
    }
}
