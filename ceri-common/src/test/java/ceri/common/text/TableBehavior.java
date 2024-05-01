package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.text.AnsiEscape.Sgr.reset;
import org.junit.Test;
import ceri.common.text.Table.Orientation;

public class TableBehavior {

	@Test
	public void shouldCreateTableFromCells() {
		var table = Table.ASCII.print((r, c, lines) -> {
			if (r < 2 && c < 3 && r + c < 3) lines.lines(r + "," + c);
		});
		assertEquals(table, """
			+---+---+---+
			|0,0|0,1|0,2|
			+---+---+---+
			|1,0|1,1|   |
			+---+---+---+
			""");
	}

	@Test
	public void shouldAllowMultiLineCells() {
		var table = Table.ASCII.print((r, c, lines) -> {
			if (r >= 2 || c >= 2) return;
			String[] ss = new String[r + c + 1];
			for (int i = 0; i < ss.length; i++)
				ss[i] = "" + i;
			lines.lines(ss);
		});
		assertEquals(table, """
			+-+-+
			|0|0|
			| |1|
			+-+-+
			|0|0|
			|1|1|
			| |2|
			+-+-+
			""");
	}

	@Test
	public void shouldPrintToOutputStream() {
		var table = StringUtil.print(out -> Table.BLANK.print(out, (r, c, lines) -> {
			if (r < 2 && c < 2) lines.lines("" + (r + c));
		}));
		assertMatch(table, "(?s)\\s+0\\s+1\\s+1\\s+2\\s+");
	}

	@Test
	public void shouldAllowFormatting() {
		var format = AnsiEscape.csi.sgr().bgColor24(0xb0b0b0);
		var table = Table.BLANK.print((r, c, lines) -> {
			if (r < 2 && c < 2) lines.lines(" " + (r + c) + " ");
		}, (f, r, c, or, s) -> or == Orientation.c ? s : format + s + reset);
		assertFind(table, "(?s) 0 .*? 1 .*? 1 .*? 2 ");
	}

}
