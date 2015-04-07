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

import java.util.List;
import java.util.ArrayList;

public class PrimaryKey {
    private final ArrayList<String> columns=new ArrayList<>();

    public PrimaryKey(List<String> cols) {
        if(cols!=null)
            for(String x:cols)
                columns.add(x.toUpperCase());
    }

    public PrimaryKey(String... cols) {
        for(String x:cols)
            columns.add(x.toUpperCase());
    }

    public int size() {
        return columns.size();
    }

    public String get(int i) {
        return columns.get(i);
    }

    public List<String> get() {
        return (List<String>)columns.clone();
    }

    public String toString() {
        StringBuilder str=new StringBuilder();
        str.append('[');
        boolean first=true;
        for(String x:columns) {
            if(first)
                first=false;
            else
                str.append(',');
            str.append(x);
        }
        str.append(']');
        return str.toString();
    }
}
