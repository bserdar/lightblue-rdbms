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
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.rdbms.metadata.RDBMSDataStoreParser;
import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;
import com.redhat.lightblue.rdbms.tables.Column;

public class ExecutionContextTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    public static JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    public static EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("rdbms", new RDBMSDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    public static Map<String,Table> getTables() throws Exception {
        Table t=new Table("schema.table",new PrimaryKey("id1","id2"));
        new Column(t,"id1");
        new Column(t,"id2");
        new Column(t,"col1");
        new Column(t,"col2");
        Map<String,Table> map=new HashMap<>();
        map.put(t.getName(),t);
        return map;
    }

    @Test
    public void insertSanityCheck() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        JsonDoc doc=getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(getTables(),doc,md);
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
        try {
            v=ctx.getVarValue(new Path("$tables.schema.table"));
            Assert.fail();
        } catch (Exception e) {}

        v=ctx.getVarValue(new Path("$tables.schema.table.col1"));
        Assert.assertNotNull(v);
        Assert.assertNull(v.getValue());
    }
    
    @Test
    public void updateSanityCheck() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        JsonDoc doc=getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForUpdate(getTables(),doc,doc,md);
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
        try {
            v=ctx.getVarValue(new Path("$tables.schema.table"));
            Assert.fail();
        } catch (Exception e) {}

        v=ctx.getVarValue(new Path("$tables.schema.table.col1"));
        Assert.assertNotNull(v);
        Assert.assertNull(v.getValue());
    }

    @Test
    public void deleteSanityCheck() throws Exception {
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForDeletion(getTables(),JsonNodeFactory.instance.textNode("id"));
        System.out.println(ctx);
        Value v=ctx.getVarValue(new Path("$docId"));
        Assert.assertEquals(ValueType.primitive,v.getType());
        Assert.assertEquals("id",v.getValue().toString());

        ObjectNode node=JsonNodeFactory.instance.objectNode();
        node.set("id1",JsonNodeFactory.instance.textNode("id1"));
        node.set("id2",JsonNodeFactory.instance.numberNode(1));

        ctx=ScriptExecutionContext.getInstanceForDeletion(getTables(),node);
        System.out.println(ctx);
        v=ctx.getVarValue(new Path("$docId"));
        Assert.assertEquals(ValueType.map,v.getType());
        
        v=ctx.getVarValue(new Path("$docId.id1"));
        Assert.assertEquals("id1",v.getValue().toString());
        v=ctx.getVarValue(new Path("$docId.id2"));
        Assert.assertEquals("1",v.getValue().toString());
    }

    
}
