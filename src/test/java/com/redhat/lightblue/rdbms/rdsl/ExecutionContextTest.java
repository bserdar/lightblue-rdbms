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
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;
import com.redhat.lightblue.rdbms.tables.Column;

public class ExecutionContextTest {

    @Test
    public void insertSanityCheck() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);
        System.out.println(ctx);

        Value v=ctx.getVarValue(new Path("$document"));
        Assert.assertEquals(ValueType.map,v.getType());
        Assert.assertTrue(v.getValue() instanceof MapValue);
                                
        try {
            v=ctx.getVarValue(new Path("$tables"));
            Assert.fail();
        } catch (Exception e) {}

        try {
            v=ctx.getVarValue(new Path("$tables.schema"));
            Assert.fail();
        } catch (Exception e) {}
        v=ctx.getVarValue(new Path("$tables.schema.table"));
        Assert.assertTrue(v.getValue() instanceof Table);

        v=ctx.getVarValue(new Path("$tables.schema.table.col1"));
        Assert.assertNotNull(v);
        Assert.assertNull(v.getValue());
    }
    
    @Test
    public void updateSanityCheck() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForUpdate(TestUtil.getTables(md),doc,doc,md,null);
        System.out.println(ctx);

        Value v=ctx.getVarValue(new Path("$document"));
        Assert.assertEquals(ValueType.map,v.getType());
        Assert.assertTrue(v.getValue() instanceof MapValue);
        v=ctx.getVarValue(new Path("$olddocument"));
        Assert.assertEquals(ValueType.map,v.getType());
        Assert.assertTrue(v.getValue() instanceof MapValue);
                                
        try {
            v=ctx.getVarValue(new Path("$tables"));
            Assert.fail();
        } catch (Exception e) {}

        try {
            v=ctx.getVarValue(new Path("$tables.schema"));
            Assert.fail();
        } catch (Exception e) {}
        v=ctx.getVarValue(new Path("$tables.schema.table"));
        Assert.assertTrue(v.getValue() instanceof Table);

        v=ctx.getVarValue(new Path("$tables.schema.table.col1"));
        Assert.assertNotNull(v);
        Assert.assertNull(v.getValue());
    }

    @Test
    public void deleteSanityCheck() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForDeletion(TestUtil.getTables(md),JsonNodeFactory.instance.textNode("id"),null);
        System.out.println(ctx);
        Value v=ctx.getVarValue(new Path("$docId"));
        Assert.assertEquals(ValueType.primitive,v.getType());
        Assert.assertEquals("id",v.getValue().toString());

        ObjectNode node=JsonNodeFactory.instance.objectNode();
        node.set("id1",JsonNodeFactory.instance.textNode("id1"));
        node.set("id2",JsonNodeFactory.instance.numberNode(1));

        ctx=ScriptExecutionContext.getInstanceForDeletion(TestUtil.getTables(md),node,null);
        System.out.println(ctx);
        v=ctx.getVarValue(new Path("$docId"));
        Assert.assertEquals(ValueType.map,v.getType());
        
        v=ctx.getVarValue(new Path("$docId.id1"));
        Assert.assertEquals("id1",v.getValue().toString());
        v=ctx.getVarValue(new Path("$docId.id2"));
        Assert.assertEquals("1",v.getValue().toString());
    }

     @Test
    public void modifyFirstLevelDocumentField() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Value v=ctx.getVarValue(new Path("$document.field1"));
        Assert.assertEquals("value1",v.getValue().toString());
        
        ctx.setVarValue(new Path("$document.field1"),new Value("test"));
        Assert.assertEquals("test",ctx.getVarValue(new Path("$document.field1")).getValue().toString());
    }

    @Test
    public void modifySecondLevelDocumentField() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Value v=ctx.getVarValue(new Path("$document.field6.nf1"));
        Assert.assertEquals("nvalue1",v.getValue().toString());
        
        ctx.setVarValue(new Path("$document.field6.nf1"),new Value("test"));
        Assert.assertEquals("test",ctx.getVarValue(new Path("$document.field6.nf1")).getValue().toString());
    }

    @Test
    public void modifyArrayDocumentField() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Value v=ctx.getVarValue(new Path("$document.field6.nf5.1"));
        Assert.assertEquals("10",v.getValue().toString());
        
        ctx.setVarValue(new Path("$document.field6.nf5.1"),new Value(11));
        Assert.assertEquals("11",ctx.getVarValue(new Path("$document.field6.nf5.1")).getValue().toString());
    }

    @Test
    public void modifyObjectArrayDocumentField() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Value v=ctx.getVarValue(new Path("$document.field7.1.elemf1"));
        Assert.assertEquals("elvalue1_1",v.getValue().toString());
        
        ctx.setVarValue(new Path("$document.field7.1.elemf1"),new Value("test"));
        Assert.assertEquals("test",ctx.getVarValue(new Path("$document.field7.1.elemf1")).getValue().toString());
    }

    @Test
    public void modifyColumn() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Value v=ctx.getVarValue(new Path("$tables.schema.table.col1"));
        Assert.assertNull(v.getValue());
        
        ctx.setVarValue(new Path("$tables.schema.table.col1"),new Value(1));
        Assert.assertEquals("1",ctx.getVarValue(new Path("$tables.schema.table.col1")).getValue().toString());
    }

}
