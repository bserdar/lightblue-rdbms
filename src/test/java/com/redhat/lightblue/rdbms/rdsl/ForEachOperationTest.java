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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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

public class ForEachOperationTest {

    public static class CallbackOperation implements ScriptOperation {

        List<Map<String,String>> list=new ArrayList<>();
        
        public String getName() {
            return "callback";
        }
        
        public Value execute(ScriptExecutionContext ctx) {
            Value v=ctx.getVarValue(new Path("elem"));
            Assert.assertEquals(ValueType.map,v.getType());
            Map<String,String> row=new HashMap<>();
            for(Iterator<String> names=v.getMapValue().getNames();names.hasNext();) {
                String name=names.next();
                String value=v.getMapValue().getValue(name).toString();
                row.put(name,value);
            }
            list.add(row);
            return Value.NULL_VALUE;
        }
    }

    @Test
    public void arrayTest() throws Exception {
        EntityMetadata md=TestUtil.getMd("testMetadata.json");
        JsonDoc doc=TestUtil.getDoc("sample1.json");
        ScriptExecutionContext ctx=ScriptExecutionContext.getInstanceForInsertion(TestUtil.getTables(md),doc,md,null);

        CallbackOperation op=new CallbackOperation();
        Script s=new Script(new ForEachOperation(new Path("$document.field7"),
                                                 new Path("elem"),
                                                 new Script(op)));

        s.execute(ctx);

        Assert.assertEquals(4,op.list.size());
        Assert.assertEquals("elvalue0_1",op.list.get(0).get("elemf1"));
        Assert.assertEquals("elvalue1_1",op.list.get(1).get("elemf1"));
        Assert.assertEquals("elvalue2_1",op.list.get(2).get("elemf1"));
        Assert.assertEquals("elvalue3_1",op.list.get(3).get("elemf1"));

        Assert.assertEquals("elvalue0_2",op.list.get(0).get("elemf2"));
        Assert.assertEquals("elvalue1_2",op.list.get(1).get("elemf2"));
        Assert.assertEquals("elvalue2_2",op.list.get(2).get("elemf2"));
        Assert.assertEquals("elvalue3_2",op.list.get(3).get("elemf2"));

        Assert.assertEquals("3",op.list.get(0).get("elemf3"));
        Assert.assertEquals("4",op.list.get(1).get("elemf3"));
        Assert.assertEquals("5",op.list.get(2).get("elemf3"));
        Assert.assertEquals("6",op.list.get(3).get("elemf3"));
        
    }

}
