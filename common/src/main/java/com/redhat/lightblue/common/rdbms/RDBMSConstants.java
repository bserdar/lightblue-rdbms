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
package com.redhat.lightblue.common.rdbms;

/**
 *  This class have the static error code used in different parts of the RDBMS module to describe something unexpected
 *
 * @author lcestari
 */
public final class RDBMSConstants {

    /* The constants below represent the error code  */
    public static final String ERR_DATASOURCE_NOT_FOUND = "rdbms:DatasourceNotFound";
    public static final String ERR_GET_CONNECTION_FAILED = "rdbms-util:GetConnectionFailed";
    public static final String ERR_GET_STATEMENT_FAILED = "rdbms:GetStatementFailed";
    public static final String ERR_EXECUTE_QUERY_FAILED = "rdbms:ExecuteQueryFailed";
    public static final String ERR_EXECUTE_UPDATE_FAILED = "rdbms:ExecuteUpdateFailed";
    public static final String ERR_BUILD_RESULT_FAILED = "rdbms:BuildResultFailed";
    public static final String ERR_ILL_FORMED_METADATA = "rdbms:IllFormedMetadata";
    public static final String ERR_NO_ACCESS = "rdbms:NoAccess";
    public static final String ERR_NO_OPERATOR = "rdbms:NoSupportedOperator";
    public static final String ERR_NO_PROJECTION = "rdbms:NoProjection";
    public static final String ERR_NO_GROUPBY = "rdbms:GroupByNotSupported";
    public static final String ERR_SUP_OPERATOR = "rdbms:NoSupportedProjection";
    public static final String NO_FIELD ="rdbms:NotMappedField";
    public static final String INV_FIELD ="rdbms:InvalidField";
    public static final String ERR_SUP_QUERY = "rdbms:NoSupportedQuery";

    /* This class just hold the constants together, it doesn't need to be instantiated  */
    private RDBMSConstants() {
    }
}
