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

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.rdbms.rdsl.*;

import com.redhat.lightblue.rdbms.tables.Table;
import com.redhat.lightblue.rdbms.tables.PrimaryKey;
import com.redhat.lightblue.rdbms.tables.Column;

public class ApplyBindingTest {

    @Test
    public void apply() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        List<Value> l=new ArrayList<>();
        l.add(new Value("string"));
        l.add(new Value(1));
        l.add(new Value("blah"));
        ctx.setVarValue(new Path("tmp"),new Value(new TempVarListValueAdapter(l)));

        Script s=new Script(new ApplyBindingOperation(new Path("tmp"),
                                                      new Bindings(new VariableBinding(new Path("$document.field1"),Binding.Dir.OUT),
                                                                   new VariableBinding(new Path("$document.field2"),Binding.Dir.OUT),
                                                                   new VariableBinding(new Path("$document.field6.nf1"),Binding.Dir.OUT))));
        s.execute(ctx);

        Assert.assertEquals("string",doc.get(new Path("field1")).asText());
        Assert.assertEquals(1,doc.get(new Path("field2")).asInt());
        Assert.assertEquals("blah",doc.get(new Path("field6.nf1")).asText());
    }

    @Test
    public void parseTest() throws Exception {        
        ApplyBindingOperation n=(ApplyBindingOperation)new OperationRegistry().
            get(TestUtil.json("{'apply-binding' : { 'row':'row', 'bindings': [ 'v1','v2'] } }"));
        
        Assert.assertEquals(new Path("row"),n.getRow());
        Assert.assertEquals(new Path("v1"),((VariableBinding)n.getBindings().get(0)).getVar());
        Assert.assertEquals(new Path("v2"),((VariableBinding)n.getBindings().get(1)).getVar());
    }
}
