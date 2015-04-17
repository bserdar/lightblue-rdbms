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
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.dialect.Dialect;

/**
 * This adapter shows a SQL result set as a list value. Since at any
 * given time multiple iterators can be operating on the result set,
 * this adapter keeps the rows retrieved from the result set in a
 * list, and returns iterators that iterate over that internal
 * list. Elements of the internal list is fetched lazily from the
 * result set.
 */
public class ResultSetAdapter implements ListValue {

    private static final Logger LOGGER=LoggerFactory.getLogger(ResultSetAdapter.class);

    private final ResultSet rs;
    private final ResultSetMetaData rsmd;
    private final Dialect dialect;
    private boolean terminated=false;
    private final List<Value> fetchedRows=new ArrayList<>();

    public ResultSetAdapter(ResultSet rs,Dialect d) {
        this.rs=rs;
        try {
            this.rsmd=rs.getMetaData();
        } catch (Exception e) {
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,e.toString());
        }
        this.dialect=d;
    }
    
    @Override
    public boolean isEmpty() {
        LOGGER.debug("isEmpty: fetchedRows.size={} terminated={}",fetchedRows.size(),terminated);
        if(fetchedRows.isEmpty()) {
            if(!terminated)
                retrieveNextRow();
            return terminated;
        } else
            return false;
    }
    
    @Override
    public Iterator<Value> getValues() {
        return new Iterator<Value>() {
            int index=-1;

            @Override
            public boolean hasNext() {
                if(index+1<fetchedRows.size())
                    return true;
                if(terminated)
                    return false;
                else {
                    retrieveNextRow();
                    return !terminated;
                }
            }

            @Override
            public Value next() {
                index++;
                if(index<fetchedRows.size())
                    return fetchedRows.get(index);
                else {
                    if(!terminated) {
                        retrieveNextRow();
                        if(!terminated)
                            return fetchedRows.get(index);
                    }
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Retrieves the next row from the result set
     */
    private boolean retrieveNextRow() {
        LOGGER.debug("retrieveNextRow begin: fetchedRows.size={} terminated={}",fetchedRows.size(),terminated);
        try {
            if(terminated)
                return false;
            else {
                if(fetchedRows.isEmpty()) {
                    if(rs.next()) {
                        retrieveRow();
                    } else {
                        terminated=true;
                    }
                } else {
                    if(rs.next()) {
                        retrieveRow();
                    } else {
                        terminated=true;
                    }
                } 
            }
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x);
        }
        LOGGER.debug("retrieveNextRow end: fetchedRows.size={} terminated={}",fetchedRows.size(),terminated);
        return terminated;
    }

    /**
     * Retrieves the current row values and puts them in the fetchedRows list
     */
    private void retrieveRow() {
        try {
            int ncols=rsmd.getColumnCount();
            List<Value> row=new ArrayList<>(ncols);
            for(int i=1;i<=ncols;i++) {
                Value columnValue=dialect.getResultSetValue(rs,i);
                row.add(columnValue);
            }
            fetchedRows.add(new Value(new TempVarListValueAdapter(row)));
            LOGGER.debug("retrieveRow end: fetchedRows.size={} terminated={}",fetchedRows.size(),terminated);
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            throw Error.get(ScriptErrors.ERR_JDBC_ERROR,x);
        }
    }
}
