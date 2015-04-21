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

public class SortColumn {

    public enum SortDir {asc,desc};

    private String column;
    private SortDir dir;

    public SortColumn() {
    }

    public SortColumn(String col,SortDir dir) {
        this.column=col;
        this.dir=dir;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String s) {
        column=s;
    }

    public SortDir getDir() {
        return dir;
    }

    public void setDir(SortDir d) {
        dir=d;
    }

    public String toString() {
        return column+":"+dir;
    }
}
