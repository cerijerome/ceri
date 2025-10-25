package ceri.process.scutil;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.collect.Node;

public class ParserBehavior {

	@Test
	public void shouldParseValues() {
		assertEquals(Parser.parse(""), Node.NULL);
		assertEquals(Parser.parse("line"), Node.tree().value(null, "line").build());
		assertEquals(Parser.parse("name:value"), Node.tree().value("name", "value").build());
	}

	@Test
	public void shouldParseGroups() {
		assertEquals(Parser.parse("<x> {\n}"), Node.tree().startGroup("", "x").build());
		assertEquals(Parser.parse("n:<x> {\n}"), Node.tree().startGroup("n", "x").build());
		assertEquals(Parser.parse("n:<x> {\nz\n}"),
			Node.tree().startGroup("n", "x").value(null, "z").build());
		assertEquals(Parser.parse("n:<x> {\n<y>{\na:b\n}\nc:d\n}"), Node.tree().startGroup("n", "x")
			.startGroup("", "y").value("a", "b").closeGroup().value("c", "d").build());
	}

}
