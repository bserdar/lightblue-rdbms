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

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.*;

import com.redhat.lightblue.rdbms.dialect.OracleDialect;

public class UpdateOperationTest {

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
    public void updateTest() throws Exception {
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

        ctx.setVarValue(new Path("$tables.testtable.id1"),new Value(1));
        ctx.setVarValue(new Path("$tables.testtable.col1"),new Value("updated"));
        s=new Script(new UpdateRowOperation(new Path("$tables.testtable"),null,null));
        s.execute(ctx);
        ResultSet rs=conn.prepareStatement("select col1 from testtable where id1=1").executeQuery();
        rs.next();
        Assert.assertEquals("updated",rs.getString(1));


        ctx.setVarValue(new Path("$tables.testtable.id1"),new Value(2));
        ctx.setVarValue(new Path("$tables.testtable.col1"),new Value("updated"));
        List<String> updateCols=new ArrayList<>();
        updateCols.add("col1");
        s=new Script(new UpdateRowOperation(new Path("$tables.testtable"),updateCols,null));
        s.execute(ctx);
        rs=conn.prepareStatement("select col1 from testtable where id1=2").executeQuery();
        rs.next();
        Assert.assertEquals("updated",rs.getString(1));

        ctx.setVarValue(new Path("$tables.testtable.id1"),new Value(3));
        ctx.setVarValue(new Path("$tables.testtable.col1"),new Value("updated"));
        updateCols=new ArrayList<>();
        updateCols.add("col1");
        s=new Script(new UpdateRowOperation(new Path("$tables.testtable"),updateCols,new SqlClause("id1=?",new Bindings(new ValueBinding(new Value(3))))));
        s.execute(ctx);
        rs=conn.prepareStatement("select col1 from testtable where id1=3").executeQuery();
        rs.next();
        Assert.assertEquals("updated",rs.getString(1));
    }

    @Test
    public void parseTest() throws Exception {
        UpdateRowOperation i=(UpdateRowOperation)new OperationRegistry().
            get(TestUtil.json("{'update-row' : { 'table':'$tables.testtable' } }"));
        Assert.assertEquals(new Path("$tables.testtable"),i.getTableName());
        i=(UpdateRowOperation)new OperationRegistry().
            get(TestUtil.json("{'update-row' : { 'table':'$tables.testtable', 'columns':['col1'] } }"));
        Assert.assertEquals(new Path("$tables.testtable"),i.getTableName());
        Assert.assertEquals("col1",i.getColumns().get(0));
        i=(UpdateRowOperation)new OperationRegistry().
            get(TestUtil.json("{'update-row' : { 'table':'$tables.testtable', 'columns':['col1'],'where':{'clause':'id=?','bindings':[1]} } }"));
        Assert.assertEquals(new Path("$tables.testtable"),i.getTableName());
        Assert.assertEquals("col1",i.getColumns().get(0));
        Assert.assertEquals(1,i.getWhereClause().getBindings().size());        
    }
}
