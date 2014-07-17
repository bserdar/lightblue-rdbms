package com.redhat.lightblue.metadata.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class RDBMSPropertyParserImplTest {

    RDBMSPropertyParserImpl cut;
    static final String expectedJSON = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalsTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalsTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalsTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalsTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalsTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";

    @Before
    public void setup() {
        cut = new RDBMSPropertyParserImpl();
    }

    @After
    public void tearDown() {
        cut = null;
    }

    @Test
    public void convertTest() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));

        JsonNode parent = p.newNode();
        RDBMS r = new RDBMS();

        Operation o = new Operation();
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setInList(inList);
        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();

        Statement e1 = new Statement();
        e1.setSQL("SELECT * FROM TABLE1");
        e1.setType("select");
        expressionList.add(e1);

        For e2 = new For();
        e2.setLoopTimes(1);
        e2.setLoopCounterVariableName("i");
        ArrayList<Expression> expressions = new ArrayList<Expression>();
        expressions.add(e1);
        ForEach e3 = new ForEach();
        ArrayList<Expression> expressions1 = new ArrayList<Expression>();
        expressions1.add(e1);
        e3.setExpressions(expressions1);
        e3.setIterateOverPath(new Path("j"));
        expressions.add(e3);
        e2.setExpressions(expressions);
        expressionList.add(e2);


        Conditional e4 = new Conditional();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalsTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
        e4.setIf(anIf);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);

        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, parent, r);

        Assert.assertEquals(expectedJSON, parent.toString());
    }

    @Test
    public void parseTest() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        Object r = cut.parse("rdbms", p, JsonUtils.json(expectedJSON).get("rdbms"));
        JsonNode parent = p.newNode();
        cut.convert(p, parent, r);
        Assert.assertEquals(expectedJSON, parent.toString());
    }

    @Test
    public void convertAndParseBindingsJustIn() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //With just In  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setInList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, r);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsJustOut() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //With just Out  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setOutList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, r);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsNone() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //No  bindings
        Bindings b = null;

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, r);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsBoth() throws IOException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //No  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setInList(inList);
        ArrayList<InOut> outList = new ArrayList<InOut>();
        InOut ou = new InOut();
        ou.setColumn("col1");
        ou.setPath(new Path("pat1"));
        outList.add(ou);
        b.setOutList(outList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, r);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
}
