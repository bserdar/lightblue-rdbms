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

import java.lang.reflect.Array;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;

/**
 * Checks if a value is empty. Empty is defined as follows:
 * <ul>
 * <li>String: empty string or null</li>
 * <li>Primitive value: null</li>
 * <li>List: empty list or null</li>
 * <li>map: Empty map or null</li>
 * <li> Anything else is false</li>
 * </ul>
 */
public class IsEmptyTest extends ConditionalTest {

    private final ScriptOperation script;

    public static final String NAME="isEmpty";

    public IsEmptyTest(ScriptOperation s) {
        super(NAME);
        this.script=s;
    }
    
    public IsEmptyTest(Value v) {
        this(new ValueOperation(v));
    }

    public IsEmptyTest(Path var) {
        this(new VariableAccessOperation(var));
    }
        
    @Override
    protected boolean executeTest(ScriptExecutionContext ctx) {
        Value v=script.execute(ctx.newContext());
        if(v.getValue()==null)
            return true;

        switch(v.getType()) {
        case primitive:
            return v.getValue() instanceof String && ((String)v.getValue()).length()==0;
        case list:
            return v.getListValue().isEmpty();
        case map:
            return v.getMapValue().isEmpty();
        case lob:
            return v.getValue().getClass().isArray()&&Array.getLength(v.getValue())==0;
        }
        return false;
    }

    @Override
    public ScriptOperation getOperation(OperationRegistry reg,ObjectNode node) {
        return new IsEmptyTest(ConditionalTest.parseVarOrScript(reg,node,NAME));
    }
}
