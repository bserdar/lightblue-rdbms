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

import com.redhat.lightblue.rdbms.rdsl.SqlClause;

public class JoinExpression {

    private List<JoinTable> joinTables;
    private SqlClause on;

    public JoinExpression() {
    }

    public JoinExpression(List<JoinTable> tables,SqlClause on) {
        this.joinTables=tables;
        this.on=on;
    }

    public List<JoinTable> getJoinTables() {
        return joinTables;
    }

    public void setJoinTables(List<JoinTable> l) {
        this.joinTables=l;
    }

    public SqlClause getOn() {
        return on;
    }

    public void setOn(SqlClause c) {
        on=c;
    }
}
