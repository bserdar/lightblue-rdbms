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

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Date;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.dialect.OracleDialect;

public class InsertOperationTest {

    private Connection conn;

    @Before
    public void setup() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        conn=DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "");
        createTestTable();
   }

    @After
    public void close() throws Exception {
        if(conn!=null) {
            dropTestTable();
            conn.close();
        }
        conn=null;
    }

    private void createTestTable() throws Exception {
        conn.createStatement().execute("create table testtable ("+
                                       "id1 int not null,"+
                                       "col1 varchar(20),"+
                                       "col2 varchar(20),"+
                                       "col3 date,"+
                                       "col4 int)");
    }
    
    private void dropTestTable() throws Exception {
        conn.createStatement().execute("drop table testtable");
    }

    @Test
    public void insertTest() throws Exception {
        EntityMetadata md=TestUtil.getMd("testTableMd.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        ctx.setConnection(conn);
        ctx.setDialect(new OracleDialect());
        
        Script s=new Script(new InsertRowOperation(new Path("$tables.testtable")));

        for(int i=0;i<100;i++) {
            ctx.setVarValue(new Path("$tables.testtable.id1"),new Value(i));
            ctx.setVarValue(new Path("$tables.testtable.col1"),new Value(2*i));
            ctx.setVarValue(new Path("$tables.testtable.col2"),new Value("blah"+i));
            s.execute(ctx);
        }

        PreparedStatement stmt=conn.prepareStatement("select count(*) from testtable");
        ResultSet rs=stmt.executeQuery();
        rs.next();
        Assert.assertEquals(100,rs.getInt(1));

        stmt=conn.prepareStatement("select id1,col1,col2,col3,col4 from testtable order by id1 asc");
        rs=stmt.executeQuery();
        int i=0;
        while(rs.next()) {
            Assert.assertEquals(i,rs.getInt(1));
            Assert.assertEquals(2*i,rs.getInt(2));
            Assert.assertEquals("blah"+i,rs.getString(3));
            i++;
        } 
    }

    @Test
    public void parseTest() throws Exception {
        InsertRowOperation i=(InsertRowOperation)InsertRowOperation.FACTORY.
            getOperation(null,TestUtil.json("{'insert-row' : { 'table':'$tables.testtable' } }"));
        Assert.assertEquals(new Path("$tables.testtable"),i.getTableName());
    }
}
