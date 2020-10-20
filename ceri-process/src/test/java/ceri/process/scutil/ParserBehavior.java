package ceri.process.scutil;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.collection.Node;
import ceri.common.collection.NodeBuilder;

public class ParserBehavior {

	@Test
	public void shouldParseValues() {
		assertEquals(Parser.parse(""), Node.NULL);
		assertEquals(Parser.parse("line"), NodeBuilder.of().value(null, "line").build());
		assertEquals(Parser.parse("name:value"), NodeBuilder.of().value("name", "value").build());
	}

	@Test
	public void shouldParseGroups() {
		assertEquals(Parser.parse("<x> {\n}"), NodeBuilder.of().startGroup("", "x").build());
		assertEquals(Parser.parse("n:<x> {\n}"), NodeBuilder.of().startGroup("n", "x").build());
		assertEquals(Parser.parse("n:<x> {\nz\n}"),
			NodeBuilder.of().startGroup("n", "x").value(null, "z").build());
		assertEquals(Parser.parse("n:<x> {\n<y>{\na:b\n}\nc:d\n}"),
			NodeBuilder.of().startGroup("n", "x").startGroup("", "y").value("a", "b").closeGroup()
				.value("c", "d").build());
	}

}
