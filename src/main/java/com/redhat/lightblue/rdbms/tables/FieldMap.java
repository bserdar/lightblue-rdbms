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
package com.redhat.lightblue.rdbms.tables;

import com.redhat.lightblue.util.Path;

public class FieldMap {
    private final Path field;
    private final Column column;
    private final String writeFilter;
    private final String readFilter;

    public FieldMap(Path field,
                    Column column,
                    String writeFilter,
                    String readFilter) {
        this.field=field;
        this.column=column;
        this.writeFilter=writeFilter==null?"?":writeFilter;
        this.readFilter=readFilter==null?"?":readFilter;
    }

    public Path getField() {
        return field;
    }

    public Column getColumn() {
        return column;
    }

    public String getWriteFilter() {
        return writeFilter;
    }

    public String getReadFilter() {
        return readFilter;
    }

    public String toString() {
        return field.toString()+"->"+column.toString();
    }
}
