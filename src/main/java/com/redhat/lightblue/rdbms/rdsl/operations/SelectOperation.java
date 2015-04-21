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
package com.redhat.lightblue.rdbms.rdsl.operations;

import java.util.List;
import java.util.ArrayList;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.ScriptOperation;
import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.ValueType;
import com.redhat.lightblue.rdbms.rdsl.ScriptErrors;
import com.redhat.lightblue.rdbms.rdsl.ScriptExecutionContext;
import com.redhat.lightblue.rdbms.rdsl.SqlClause;
import com.redhat.lightblue.rdbms.rdsl.Bindings;
import com.redhat.lightblue.rdbms.rdsl.Binding;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.Column;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;

import com.redhat.lightblue.rdbms.dialect.Dialect;

/**
 * <pre>
 * { select : { project: [ var, {"clause": clause, bindings: [ ... ] }, ... ],
 *              distinct: true|false,
 *              join: { tables: [ { "alias": alias, "table": table, outer: false }, ... ], 
 *                     on: { clause: "criteria", bindings:[...] } } |
 *              table: "table",
 *              where: { clause: "criteria", bindings: [...] },
 *              sort: [ { column: col, ascending: true}, ... ] }
 * </pre>
 */
public class SelectOperation implements ScriptOperation {

    private static final Logger LOGGER=LoggerFactory.getLogger(SelectOperation.class);

    public static final String NAME="select";

    private ProjectionExpression projection;
    private boolean distinct;
    private JoinExpression join;
    private SqlClause whereClause;
    private SortExpression sort;
    
    public SelectOperation() {}

    public SelectOperation(ProjectionExpression projection,
                           boolean distinct,
                           JoinExpression join,
                           SqlClause whereClause,
                           SortExpression sort) {
        this.projection=projection;
        this.distincy=distinct;
        this.join=join;
        this.whereClause=whereClause;
        this.sort=sort;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public ProjectionExpression getProjection() {
        return projection;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public JoinExpression getJoin() {
        return join;
    }

    public SqlClause getWhereClause() {
        return whereClause;
    }

    public SortExpression getSort() {
        return sort;
    }

    @Override
    public Value execute(ScriptExecutionContext ctx) {
    }
}
