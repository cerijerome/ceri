package ceri.process.scutil;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.collection.Node;
import ceri.common.collection.NodeBuilder;

public class ParserBehavior {

	@Test
	public void shouldParseValues() {
		assertThat(Parser.parse(""), is(Node.NULL));
		assertThat(Parser.parse("line"), is(NodeBuilder.of().value(null, "line").build()));
		assertThat(Parser.parse("name:value"), is(NodeBuilder.of().value("name", "value").build()));
	}

	@Test
	public void shouldParseGroups() {
		assertThat(Parser.parse("<x> {\n}"), is(NodeBuilder.of().startGroup("", "x").build()));
		assertThat(Parser.parse("n:<x> {\n}"), is(NodeBuilder.of().startGroup("n", "x").build()));
		assertThat(Parser.parse("n:<x> {\nz\n}"),
			is(NodeBuilder.of().startGroup("n", "x").value(null, "z").build()));
		assertThat(Parser.parse("n:<x> {\n<y>{\na:b\n}\nc:d\n}"),
			is(NodeBuilder.of().startGroup("n", "x").startGroup("", "y").value("a", "b")
				.closeGroup().value("c", "d").build()));
	}

}
