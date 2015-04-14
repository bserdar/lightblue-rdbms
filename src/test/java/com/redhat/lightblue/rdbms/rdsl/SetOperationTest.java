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

import java.util.Map;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;
import com.redhat.lightblue.rdbms.tables.Column;

public class SetOperationTest {

    @Test
    public void modifyColumn() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Assert.assertNull(ctx.getVarValue(new Path("$tables.schema.table.col1")).getValue());
        Script s=new Script(new SetOperation(new Path("$tables.schema.table.col1"),
                                             new Path("$document.field1")));
        s.execute(ctx);
        Assert.assertEquals("value1",ctx.getVarValue(new Path("$tables.schema.table.col1")).getValue());
    }

    @Test
    public void modifyDocField() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        ctx.setVarValue(new Path("$tables.schema.table.col1"),new Value(1));
        Assert.assertEquals("value1",ctx.getVarValue(new Path("$document.field1")).getValue());
        Script s=new Script(new SetOperation(new Path("$document.field1"),
                                             new Path("$tables.schema.table.col1")));
        s.execute(ctx);
        Assert.assertEquals("1",ctx.getVarValue(new Path("$document.field1")).getValue());
    }

    @Test
    public void createSimpleTempVar() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        
        Script s=new Script(new SetOperation(new Path("test"),
                                             new Path("$document.field1")));
        s.execute(ctx);
        Assert.assertEquals("value1",ctx.getVarValue(new Path("test")).getValue());
    }

    @Test
    public void createObjectTempVar() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        
        Script s=new Script(new SetOperation(new Path("test"),
                                             new Path("$document.field6")));
        s.execute(ctx);
        Assert.assertEquals("nvalue1",ctx.getVarValue(new Path("test.nf1")).getValue());
        Assert.assertEquals("nvalue2",ctx.getVarValue(new Path("test.nf2")).getValue());
        System.out.println(ctx.getVarValue(new Path("test.nf7")).getValue());
        Assert.assertEquals("nnvalue1",ctx.getVarValue(new Path("test.nf7.nnf1")).getValue());
    }
}
