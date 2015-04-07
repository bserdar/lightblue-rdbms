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
package com.redhat.lightblue.rdbms.metadata;

import java.io.Serializable;
import java.util.Objects;

import com.redhat.lightblue.metadata.DataStore;

/**
 * Represents the RDBMS data store in metadata. Keeps the datasource and dialect.
 */
public class RDBMSDataStore implements DataStore, Serializable {

    private static final long serialVersionUID = 1l;

    public static final String BACKEND = "rdbms";

    private String datasource;
    private String dialect;

    public RDBMSDataStore() {
    }

    public RDBMSDataStore(String datasource,
                          String dialect) {
        this.datasource = datasource;
        this.dialect = dialect;
    }

    @Override
    public String getBackend() {
        return BACKEND;
    }

    /**
     * Datasource name
     */
    public String getDatasource() {
        return this.datasource;
    }

    /**
     * Datasource name
     */
    public void setDatasource(String argDatasourceName) {
        this.datasource = argDatasourceName;
    }

    /**
     * DB Dialect
     */
    public String getDialect() {
        return this.dialect;
    }

    /**
     * DB Dialect
     */
    public void setDialect(String x) {
        this.dialect = x;
    }


    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder(64);
        if (datasource != null) {
            bld.append(" datasource:").append(datasource);
        }
        if (dialect != null) {
            bld.append(" dialect:").append(dialect);
        }
        return bld.toString();
    }

    @Override
    public boolean equals(Object x) {
        try {
            if (x instanceof RDBMSDataStore) {
                RDBMSDataStore ds = (RDBMSDataStore) x;
                try {
                    return Objects.equals(datasource, ds.datasource)
                        && Objects.equals(dialect, dialect) ;
                } catch (ClassCastException e) {
            }
        }
        } catch (Exception e) {
        }
        return false;
    }
 }
