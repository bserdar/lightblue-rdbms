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

import java.util.Iterator;

/**
 * This is the interface used to access anything that can be
 * represented as a map. The underlying object is most likely a Json
 * ObjectNode, i.e. an object with fields in it, and each field can be
 * a simple value, a list, or another map.
 */
public interface MapValue {
    /**
     * Check if the map is empty
     */
    boolean isEmpty();

    /**
     * Returns an iterator over the names of objects contained in the map
     */
    Iterator<String> getNames();

    /**
     * Returns the object with the given name. Returns null if no such object exists
     */
    Value getValue(String name);
}
