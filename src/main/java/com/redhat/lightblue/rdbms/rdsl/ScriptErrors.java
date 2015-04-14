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
    public static final String ERR_INVALID_REFERENCE_TO_TABLE="rdbms:script:invalid-reference-to-table";
    public static final String ERR_INVALID_ASSIGNMENT="rdbms:script:invalid-assignment";
    public static final String ERR_INCOMPATIBLE_ARRAY_ELEMENT="rdbms:script:incompatible-array-element";
    public static final String ERR_INCOMPATIBLE_ASSIGNMENT="rdbms:script:incompatible-assignment";
    public static final String ERR_INCONSISTENT_DATA="rdbms:script:inconsistent-data";
    public static final String ERR_UNKNOWN_OPERATION="rdbms:script:unknown-operation";
    public static final String ERR_MALFORMED_OPERATION="rdbms:script:malformed-operation";
    public static final String ERR_MALFORMED_SCRIPT="rdbms:script:malformed-script";
    public static final String ERR_MISSING_ARG="rdbms:script:missing-arg";
    public static final String ERR_INVALID_DEREFERENCE="rdbms:script:invalid-dereference";

    /**
     * Map operation must be called with a table and document object
     */
    public static final String ERR_INVALID_MAP_CALL="rdbms:script:map:invalid-map-call";

    /**
     * Map operation is called with an object that does not know its metadata (it is not part of the document)
     */
    public static final String ERR_NEED_DOCUMENT_FOR_MAP="rdbms:script:map:need-document-for-map";
    
    /**
     * Column not found in column mapping
     */
    public static final String ERR_MAP_UNKNOWN_COLUMN="rdbms:script:map:unknown-column";

    /**
     * A column is mapped to an array value
     */
    public static final String ERR_COLUMN_MAPPED_TO_ARRAY="rdbms:script:map:column-mapped-to-array";

    /**
     * A list is required in foreach
     */
    public static final String ERR_LIST_REQUIRED="rdbms:script:foreach:list-required";

    /**
     * test for conditional is invalid
     */
    public static final String ERR_INVALID_TEST="rdbms:script:conditional:invalid-test";

    /**
     * Boolean value required for conditional test
     */
    public static final String ERR_BOOLEAN_REQUIRED="rdbms:script:conditional:boolean-required";

    private ScriptErrors() {}
}
