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

import java.util.Date;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.*;

import com.redhat.lightblue.rdbms.dialect.OracleDialect;

public class ConditionalOperationTest {

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

    @Test
    public void emptyTest() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        ctx.setConnection(conn);
        ctx.setDialect(new OracleDialect());
        
        Script s=new Script(new ConditionalOperation(new IsEmptyTest(new Path("$document.field7")),
                                                     new Script(new SetOperation(new Path("$document.field1"),
                                                                                 new Value("true"))),
                                                     new Script(new SetOperation(new Path("$document.field1"),
                                                                                 new Value("false")))));
        s.execute(ctx);
        Assert.assertEquals("false",ctx.getVarValue(new Path("$document.field1")).getValue());
        s=new Script(new ConditionalOperation(new IsEmptyTest(new Path("$document.field6.nf11")),
                                              new Script(new SetOperation(new Path("$document.field1"),
                                                                          new Value("true"))),
                                              new Script(new SetOperation(new Path("$document.field1"),
                                                                          new Value("false")))));
        s.execute(ctx);
        Assert.assertEquals("true",ctx.getVarValue(new Path("$document.field1")).getValue());
    }

    @Test
    public void parseTest() throws Exception {
        ConditionalOperation e=(ConditionalOperation)new OperationRegistry().
            get(TestUtil.json("{'$conditional': { 'test': { 'isEmpty': 'var' }, 'then': { '$set':{'dest':'var','value':'val'} }, 'else':{'$set':{'dest':'var','var':'var2'}}}}"));
        
        Assert.assertTrue(((Script)e.getTest()).getOperations().get(0) instanceof IsEmptyTest);
        Assert.assertTrue(((Script)e.getTrueScript()).getOperations().get(0) instanceof SetOperation);
        Assert.assertTrue(((Script)e.getFalseScript()).getOperations().get(0) instanceof SetOperation);
    }

}
