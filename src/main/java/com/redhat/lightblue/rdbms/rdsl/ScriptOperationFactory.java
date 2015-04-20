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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Interface that generates script operation instances from Json
 * nodes. A script operation factory can generate multiple operations.
 */
public interface ScriptOperationFactory<T extends ScriptOperation> {

    /**
     * Returns the names of the operations this factory can generate
     */
    String[] operationNames();

    /**
     * Returns a script operation instance using the given Json node
     * operation configuration. The operationNode is the object node
     * of the form:
     * <pre>
     *   { "operationName" : { args } }
     * </pre>
     * or
     * <pre>
     *   { "operationName" : arg }
     * </pre>
     */
    T getOperation(OperationRegistry reg,ObjectNode operationNode);
}
