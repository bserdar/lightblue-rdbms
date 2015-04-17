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

/**
 * A variable binding, can be an IN- or OUT-binding
 */
public class VariableBinding extends Binding {

    private final Path var;

    public VariableBinding(Path var,Dir dir,Integer jdbcType) {
        super(dir,jdbcType);
        this.var=var;;        
    }

    public VariableBinding(Path var,Dir dir) {
        this(var,dir,null);
    }

    /**
     * Creates an IN binding
     */
    public VariableBinding(Path var) {
        this(var,Dir.IN,null);
    }

    public Path getVar() {
        return var;
    }

    public String toString () {
        return getDir().toString()+"("+var+")";
    }

    @Override
    public Value getValue(ScriptExecutionContext ctx) {
        return ctx.getVarValue(var);
    }

    @Override
    public void setValue(ScriptExecutionContext ctx,Value v) {
        ctx.setVarValue(var,v);
    }
}
