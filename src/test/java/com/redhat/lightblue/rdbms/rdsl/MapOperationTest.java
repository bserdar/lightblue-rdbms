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

public class MapOperationTest {

    @Test
    public void mapTableToDoc() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        // Assign some values to columns
        ctx.setVarValue(new Path("$tables.schema.table.id1"),new Value(1));
        ctx.setVarValue(new Path("$tables.schema.table.id2"),new Value(2));
        ctx.setVarValue(new Path("$tables.schema.table.col1"),new Value(3));
        ctx.setVarValue(new Path("$tables.schema.table.col2"),new Value(4));
        ctx.setVarValue(new Path("$tables.schema.table.col3"),new Value(5));
        ctx.setVarValue(new Path("$tables.schema.table.col4"),new Value(6));
        ctx.setVarValue(new Path("$tables.schema.table.col5"),new Value(7));
        ctx.setVarValue(new Path("$tables.schema.table.col6"),new Value(8));
        ctx.setVarValue(new Path("$tables.schema.table.col7"),new Value(9));

        Script s=new Script(new MapOperation(new Path("$tables.schema.table"),
                                             new Path("$document")));
        s.execute(ctx);

        Assert.assertEquals("1",doc.get(new Path("field1")).asText());
        Assert.assertEquals("2",doc.get(new Path("field2")).asText());
        Assert.assertEquals("3",doc.get(new Path("field3")).asText());
        Assert.assertEquals("4",doc.get(new Path("field4")).asText());
        Assert.assertEquals("true",doc.get(new Path("field5")).asText());
        Assert.assertEquals("6",doc.get(new Path("field6.nf1")).asText());
        Assert.assertEquals("7",doc.get(new Path("field6.nf2")).asText());
        Assert.assertEquals("8",doc.get(new Path("field6.nf3")).asText());
        Assert.assertEquals("true",doc.get(new Path("field6.nf4")).asText());
    }

    @Test
    public void mapDocToTable() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        Script s=new Script(new MapOperation(new Path("$document"),new Path("$tables.schema.table")));
        s.execute(ctx);
        
        Assert.assertEquals("value1",ctx.getVarValue(new Path("$tables.schema.table.id1")).toString());
        Assert.assertEquals("value2",ctx.getVarValue(new Path("$tables.schema.table.id2")).toString());
        Assert.assertEquals("3",ctx.getVarValue(new Path("$tables.schema.table.col1")).toString());
        Assert.assertEquals("4",ctx.getVarValue(new Path("$tables.schema.table.col2")).toString());
        Assert.assertEquals("true",ctx.getVarValue(new Path("$tables.schema.table.col3")).toString());
        Assert.assertEquals("nvalue1",ctx.getVarValue(new Path("$tables.schema.table.col4")).toString());
        Assert.assertEquals("nvalue2",ctx.getVarValue(new Path("$tables.schema.table.col5")).toString());
        Assert.assertEquals("4",ctx.getVarValue(new Path("$tables.schema.table.col6")).toString());
        Assert.assertEquals("false",ctx.getVarValue(new Path("$tables.schema.table.col7")).toString());
    }
}
