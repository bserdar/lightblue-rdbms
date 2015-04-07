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
 * Contains all the variables and database information required to
 * execute scripts
 */
public class ScriptExecutionContext implements VariableAccessor {

    private final VariableAccessor scope;
    private final ScriptExecutionContext parent;

    /**
     * Constructs the top-level execution context
     */
    public ScriptExecutionContext() {
        this(null);
    }

    /**
     * Constructs a nested scope
     */
    public ScriptExecutionContext(ScriptExecutionContext parentScope) {
        scope=new DelegatingVariableAccessor();
        this.parent=parentScope;       
    }

    /**
     * Lookup variable in the current scope. Start with this scope, move up
     */
    @Override
    public LValue getValue(Path p) {
        try {
            return scope.getValue(p);
        } catch (Error x) {
            return parent.getValue(p);
        }
    }

    /**
     * If the variable is single-=level, defines a temp variable at this scope. Otherwise,
     * resolves and assigns the variable.
     */
    @Override
    public boolean setValue(Path p,Value value) {
        boolean ret;
        if(p.numSegments()==1) {
            ret=scope.setValue(p,value);
        } else {
            if(parent!=null)
                ret=parent.setValue(p,value);
            else
                ret=scope.setValue(p,value);
        }
        return ret;
    }
}
