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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * An accessor that controls access to all temp variables
 */
public class TempVariableAccessor implements VariableAccessor {

    private Value value;

    public TempVariableAccessor() {
    }

    public TempVariableAccessor(Value v) {
        this.value=v;
    }

    @Override
    public Value getVarValue(Path var) {
        if(var.isEmpty()) {
            return value;
        } else {
            if(value.getValue() instanceof TempVarResolver) {
                return ((TempVarResolver)value.getValue()).get(var);
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE,var.toString());
        }
    }

    @Override
    public ValueType getVarType(Path var) {
        Value v=getVarValue(var);
        if(v==null)
            throw Error.get(ScriptErrors.ERR_INVALID_VARIABLE,var.toString());
        else
            return v.getType();
    }
    
    @Override
    public void setVarValue(Path var,Value newValue) {
        if(var.isEmpty()) {
            value=newValue;
        } else {
            if(value.getValue() instanceof TempVarResolver) {
                ((TempVarResolver)value.getValue()).set(var,newValue);
            } else
                throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE,var.toString());
        }
    }
}