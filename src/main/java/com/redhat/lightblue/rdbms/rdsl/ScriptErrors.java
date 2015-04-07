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

public final class ScriptErrors {

    public static final String ERR_VAR_NOT_WRITABLE="rdbms:script:variable-not-writable";
    public static final String ERR_VAR_NOT_DOCUMENT_PART="rdbms:script:variable-not-document-part";
    public static final String ERR_INVALID_ARRAY_INDEX="rdbms:script:invalid-array-index";
    public static final String ERR_INVALID_VARIABLE="rdbms:script:invalid-variable";
    public static final String ERR_MULTILEVEL_VARIABLE_NOT_ALLOWED="rdbms:script:multilevel-variable-not-allowed";
    public static final String ERR_NO_COLUMN="rdbms:script:no-column";
    public static final String ERR_CANNOT_ASSIGN_TABLE_VALUE="rdbms:script:cannot-assign-table-value";
    public static final String ERR_CANNOT_INTERPRET_AS_VALUE="rdbms:script:cannot-interpret-variable-as-value";


    private ScriptErrors() {}
}
