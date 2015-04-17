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

import com.redhat.lightblue.util.Error;

/**
 * A value binding. This can only be an IN-binding.
 */
public class ValueBinding extends Binding {

    private final Value value;

    public ValueBinding(Value v) {
        this(v,null);
    }

    public ValueBinding(Value v,Integer jdbcType) {
        super(Binding.Dir.IN,jdbcType);
        this.value=v;
    }

    public Value getValue() {
        return value;
    }

    public String toString() {
        return getDir().toString()+"("+value+")";
    }
    
    @Override
    public Value getValue(ScriptExecutionContext ctx) {
        return value;
    }

    @Override
    public void setValue(ScriptExecutionContext ctx,Value v) {
        throw Error.get(ScriptErrors.ERR_CANNOT_SET_VALUE_OF_VALUE_BINDING);
    }

}
