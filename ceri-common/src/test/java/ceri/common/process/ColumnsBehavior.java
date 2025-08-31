package ceri.common.process;

import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import java.util.List;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.common.text.Patterns;
import ceri.common.text.Splitter;

public class ColumnsBehavior {
	private static final String fixed = TestUtil.resource("column-fixed.txt");
	private static final List<String> fixedLines = Patterns.Split.LINE.list(fixed);
	private static final String tabs = TestUtil.resource("column-tabs.txt");
	private static final List<String> tabLines = Patterns.Split.LINE.list(tabs);

	@Test
	public void shouldParseFixedColumns() {
		Columns col = Columns.fromFixedWidthHeader(fixedLines.get(0));
		assertOrdered(col.names, "COL1", "COL2", "COL3");
		assertOrdered(col.parse(fixedLines.get(1)), "r1=c1", "r1c2", "r1c3");
		assertOrdered(col.parse(fixedLines.get(2)), "r2-c1", "r2 c2", "");
		assertMap(col.parseAsMap(fixedLines.get(1)), "COL1", "r1=c1", "COL2", "r1c2", "COL3",
			"r1c3");
		assertMap(col.parseAsMap(fixedLines.get(2)), "COL1", "r2-c1", "COL2", "r2 c2", "COL3", "");
	}

	@Test
	public void shouldParseTabColumns() {
		Columns col = Columns.fromHeader(tabLines.get(0), Splitter.Extractor.byTabs());
		assertOrdered(col.names, "COL1", "COL2", "COL3");
		assertOrdered(col.parse(tabLines.get(1)), "r1=c1", "r1c2", "r1c3");
		assertOrdered(col.parse(tabLines.get(2)), "r2-c1", "r2 c2", "");
		assertMap(col.parseAsMap(tabLines.get(1)), "COL1", "r1=c1", "COL2", "r1c2", "COL3", "r1c3");
		assertMap(col.parseAsMap(tabLines.get(2)), "COL1", "r2-c1", "COL2", "r2 c2", "COL3", "");
	}

	@Test
	public void shouldParseWithoutHeader() {
		String s = "a bc  def     g";
		Columns col = Columns.builder().add(2, 4, 8, Integer.MAX_VALUE).build();
		assertOrdered(col.parse(s), "a", "bc", "def", "g");
	}

}
