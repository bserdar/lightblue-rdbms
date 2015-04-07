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
 * Interface that contols a domain of variables. The variables under
 * this accessor can be tables, documents, temp variables, or the
 * top-level variable context that provides access to all those.
 */
public interface VariableAccessor {
    
    /**
     * Retrieves the value of a variable. Throws exception if variable
     * does not exist.
     */
    Value getVarValue(Path var);

    /**
     * Retrieves the type of a variable. Throws exception if variable
     * does not exist
     */
    ValueType getVarType(Path var);

    /**
     * Sets var value. If var does not exist and it is single level
     * (no '.'s), defines a temporary var. If var does not exist and
     * it is a multi-level var, throws exception
     */
    void setVarValue(Path var,Value value);
}
