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

public class ExecuteSqlClauseTest {

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
                                       "id int not null,"+
                                       "col1 varchar(20),"+
                                       "col2 varchar(20),"+
                                       "col3 date,"+
                                       "col4 int)");
    }
    
    private void dropTestTable() throws Exception {
        conn.createStatement().execute("drop table testtable");
    }

    // @Test
    // public void insertTest() throws Exception {
    //     EntityMetadata md=TestUtil.getMd("testMetadata.json");
    //     JsonDoc doc=TestUtil.getDoc("sample1.json");
    //     ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
    //     ctx.setConnection(conn);
    //     ctx.setDialect(new OracleDialect());
        
    //     Script s=new Script(new ExecuteSqlClauseOperation(ExecuteSqlClauseOperation.NAMEQ,"insert into testtable values (?,?,?,?,?)",
    //                                                       new Bindings(new VariableBinding(new Path("id")),
    //                                                                    new VariableBinding(new Path("$document.field1")),
    //                                                                    new VariableBinding(new Path("blah")),
    //                                                                    new ValueBinding(new Value(ValueType.primitive,new Date())),
    //                                                                    new VariableBinding(new Path("x")))));

    //     for(int i=0;i<100;i++) {
    //         ctx.setVarValue(new Path("id"),new Value(i));
    //         ctx.setVarValue(new Path("x"),new Value(2*i));
    //         ctx.setVarValue(new Path("blah"),new Value("blah"+i));
    //         s.execute(ctx);
    //     }

    //     PreparedStatement stmt=conn.prepareStatement("select count(*) from testtable");
    //     ResultSet rs=stmt.executeQuery();
    //     rs.next();
    //     Assert.assertEquals(100,rs.getInt(1));

    //     stmt=conn.prepareStatement("select id,col1,col2,col3,col4 from testtable order by id asc");
    //     rs=stmt.executeQuery();
    //     int i=0;
    //     while(rs.next()) {
    //         Assert.assertEquals(i,rs.getInt(1));
    //         Assert.assertEquals("value1",rs.getString(2));
    //         Assert.assertEquals("blah"+i,rs.getString(3));
    //         Assert.assertEquals(2*i,rs.getInt(5));
    //         i++;
    //     } 
    // }

    @Test
    public void queryTest() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        ctx.setConnection(conn);
        ctx.setDialect(new OracleDialect());

        PreparedStatement stmt=conn.prepareStatement("insert into testtable values (?,?,?,?,?)");
        for(int i=0;i<100;i++) {
            stmt.setInt(1,i);
            stmt.setString(2,"col1"+i);
            stmt.setString(3,"col2"+i);
            stmt.setDate(4,new java.sql.Date(System.currentTimeMillis()));
            stmt.setInt(5,2*i);
            stmt.executeUpdate();
        }

        Script s=new Script(new SetOperation(new Path("resultset"),
                                             new ExecuteSqlClauseOperation(ExecuteSqlClauseOperation.NAMEQ,
                                                                           "select id,col1,col2,col4 from testtable where id>? order by id asc",
                                                                           new Bindings(new VariableBinding(new Path("$document.field3"))))));
        
        s.execute(ctx);
        Value v=ctx.getVarValue(new Path("resultset"));
        Assert.assertEquals(ValueType.list,v.getType());
        ListValue l=v.getListValue();
        Assert.assertFalse(l.isEmpty());
        int n=0;
        for(Iterator<Value> itr=l.getValues();itr.hasNext();) {
            Value row=itr.next();
            Assert.assertEquals(ValueType.list,row.getType());
            int colix=0;
            for(Iterator<Value> colitr=row.getListValue().getValues();colitr.hasNext();) {
                Value col=colitr.next();
                switch(colix){
                case 0: // id
                    Assert.assertEquals(n+4,((Number)col.getValue()).intValue());
                    break;
                case 1: // col1
                    Assert.assertEquals("col1"+(n+4),col.getValue().toString());
                    break;
                case 2: // col2
                    Assert.assertEquals("col2"+(n+4),col.getValue().toString());
                    break;
                case 3: // col4
                    Assert.assertEquals((n+4)*2,((Number)col.getValue()).intValue());
                    break;
                }
                colix++;
            }
            n++;
        }
        Assert.assertEquals(96,n);
    }
}
