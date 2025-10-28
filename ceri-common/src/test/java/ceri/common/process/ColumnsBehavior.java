package ceri.common.process;

import java.util.List;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.text.Regex;
import ceri.common.text.Splitter;

public class ColumnsBehavior {
	private static final String fixed = Testing.resource("column-fixed.txt");
	private static final List<String> fixedLines = Regex.Split.LINE.list(fixed);
	private static final String tabs = Testing.resource("column-tabs.txt");
	private static final List<String> tabLines = Regex.Split.LINE.list(tabs);

	@Test
	public void shouldParseFixedColumns() {
		Columns col = Columns.fromFixedWidthHeader(fixedLines.get(0));
		Assert.ordered(col.names, "COL1", "COL2", "COL3");
		Assert.ordered(col.parse(fixedLines.get(1)), "r1=c1", "r1c2", "r1c3");
		Assert.ordered(col.parse(fixedLines.get(2)), "r2-c1", "r2 c2", "");
		Assert.map(col.parseAsMap(fixedLines.get(1)), "COL1", "r1=c1", "COL2", "r1c2", "COL3",
			"r1c3");
		Assert.map(col.parseAsMap(fixedLines.get(2)), "COL1", "r2-c1", "COL2", "r2 c2", "COL3", "");
	}

	@Test
	public void shouldParseTabColumns() {
		Columns col = Columns.fromHeader(tabLines.get(0), Splitter.Extractor.byTabs());
		Assert.ordered(col.names, "COL1", "COL2", "COL3");
		Assert.ordered(col.parse(tabLines.get(1)), "r1=c1", "r1c2", "r1c3");
		Assert.ordered(col.parse(tabLines.get(2)), "r2-c1", "r2 c2", "");
		Assert.map(col.parseAsMap(tabLines.get(1)), "COL1", "r1=c1", "COL2", "r1c2", "COL3",
			"r1c3");
		Assert.map(col.parseAsMap(tabLines.get(2)), "COL1", "r2-c1", "COL2", "r2 c2", "COL3", "");
	}

	@Test
	public void shouldParseWithoutHeader() {
		String s = "a bc  def     g";
		Columns col = Columns.builder().add(2, 4, 8, Integer.MAX_VALUE).build();
		Assert.ordered(col.parse(s), "a", "bc", "def", "g");
	}
}
