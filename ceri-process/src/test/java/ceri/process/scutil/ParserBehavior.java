package ceri.process.scutil;

import org.junit.Test;
import ceri.common.collect.Node;
import ceri.common.test.Assert;

public class ParserBehavior {

	@Test
	public void shouldParseValues() {
		Assert.equal(Parser.parse(""), Node.NULL);
		Assert.equal(Parser.parse("line"), Node.tree().value(null, "line").build());
		Assert.equal(Parser.parse("name:value"), Node.tree().value("name", "value").build());
	}

	@Test
	public void shouldParseGroups() {
		Assert.equal(Parser.parse("<x> {\n}"), Node.tree().startGroup("", "x").build());
		Assert.equal(Parser.parse("n:<x> {\n}"), Node.tree().startGroup("n", "x").build());
		Assert.equal(Parser.parse("n:<x> {\nz\n}"),
			Node.tree().startGroup("n", "x").value(null, "z").build());
		Assert.equal(Parser.parse("n:<x> {\n<y>{\na:b\n}\nc:d\n}"), Node.tree().startGroup("n", "x")
			.startGroup("", "y").value("a", "b").closeGroup().value("c", "d").build());
	}

}
