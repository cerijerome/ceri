package ceri.common.text;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import ceri.common.array.ArrayUtil;
import ceri.common.stream.Streams;

/**
 * Utility for creating text frames in monospace fonts. Characters signify vertical, horizontal,
 * center, edge joins n, s, e, w, and corners ne, nw, se, sw.
 */
public record Table(char v, char h, char c, char n, char s, char e, char w, char ne, char nw,
	char se, char sw) {

	public static final Table BLANK = Table.of(" ");
	public static final Table ASCII = Table.of("|-+");
	public static final Table ASCII2 = Table.of("|-+--||..''");
	public static final Table UTF =
		Table.of("\u2551\u2550\u256c\u2566\u2569\u2563\u2560\u2557\u2554\u255d\u255a");

	/**
	 * Indicates the direction of a frame char relative to the cell contents.
	 */
	public static enum Orientation {
		c, // center = cell content line
		n,
		s,
		e,
		w,
		ne,
		nw,
		se,
		sw;
	}

	/**
	 * Captures cell content lines.
	 */
	public static interface LineReceiver {
		/**
		 * Call to set cell content lines.
		 */
		void lines(String... lines);
	}

	/**
	 * Set cell content lines.
	 */
	public static interface CellProvider {
		/**
		 * Callback with row, column, and an object to register cell content lines.
		 */
		void cell(int row, int col, LineReceiver cell);
	}

	/**
	 * Allows formatting of cell contents and framing, typically with ANSI escapes.
	 */
	public static interface Formatter {
		public static final Formatter NULL = (_, _, _, _, s) -> s;

		/**
		 * Return the formatted string. Visible string length must remain the same.
		 */
		String format(Table frame, int r, int c, Orientation or, String s);
	}

	/**
	 * Collects cell content lines, while tracking row and column sizes.
	 */
	private static class Cells {
		private final Map<Coord, String[]> cells = new HashMap<>();
		private final Map<Integer, Integer> rowSizes = new HashMap<>();
		private final Map<Integer, Integer> colSizes = new HashMap<>();
		private int cols;
		private int rows;

		private static record Coord(int row, int col) {}

		public static Cells from(CellProvider cellFn) {
			var cells = new Cells();
			int row = 0, col = 0;
			while (true) {
				int r = row, c = col;
				cellFn.cell(r, c, lines -> cells.put(r, c, lines));
				if (r >= cells.rows || c >= cells.cols) { // lines not populated
					if (col == 0) break;
					col = 0;
					row++;
				} else col++;
			}
			return cells;
		}

		public String get(int row, int col, int i) {
			var lines = cells.getOrDefault(new Coord(row, col), ArrayUtil.Empty.strings);
			if (i >= lines.length) return StringUtil.repeat(' ', colSize(col));
			return lines[i] + StringUtil.repeat(' ', colSize(col) - lines[i].length());
		}

		public int rows() {
			return rows;
		}

		public int cols() {
			return cols;
		}

		public int rowSize(int row) {
			return rowSizes.getOrDefault(row, 0);
		}

		public int colSize(int col) {
			return colSizes.getOrDefault(col, 0);
		}

		private void put(int row, int col, String... s) {
			cells.put(new Coord(row, col), s);
			rowSizes.merge(row, s.length, Math::max);
			colSizes.merge(col, maxLen(s), Math::max);
			rows = Math.max(rows, row + 1);
			cols = Math.max(cols, col + 1);
		}
	}

	/**
	 * Creates from chars, repeating the final char.
	 */
	public static Table of(String s) {
		int i = 0;
		return new Table(ch(s, i++), ch(s, i++), ch(s, i++), ch(s, i++), ch(s, i++), ch(s, i++),
			ch(s, i++), ch(s, i++), ch(s, i++), ch(s, i++), ch(s, i++));
	}

	/**
	 * Provide repeating horizontal char string.
	 */
	public String h(int n) {
		return StringUtil.repeat(h(), n);
	}

	/**
	 * Print the frame and cells using the cell function to populate lines.
	 */
	public String print(CellProvider cellFn) {
		return print(cellFn, Formatter.NULL);
	}

	/**
	 * Print the frame and cells using the cell function to populate lines.
	 */
	public String print(CellProvider cellFn, Formatter fmt) {
		return StringUtil.print(out -> print(out, cellFn, fmt));
	}

	/**
	 * Print the frame and cells using the cell function to populate lines.
	 */
	public void print(PrintStream out, CellProvider cellFn) {
		print(out, cellFn, Formatter.NULL);
	}

	/**
	 * Print the frame and cells using the cell function to populate lines, and formatter to format
	 * cells and frame. The cell formatter is notified on every cell for [nw, n, w, center], [ne, e]
	 * on last column, [sw, s] on the last row, and [se] for last row and column:
	 * 
	 * <pre>
	 * +- +- +- +-+
	 * |c |c |c |c|
	 * 
	 * +- +- +- +-+
	 * |c |c |c |c|
	 * 
	 * +- +- +- +-+
	 * |c |c |c |c|
	 * +- +- +- +-+
	 * </pre>
	 */
	public void print(PrintStream out, CellProvider cellFn, Formatter fmt) {
		var cells = Cells.from(cellFn);
		for (int r = 0; r < cells.rows(); r++) {
			printLine(out, r, cells, fmt);
			for (int i = 0; i < cells.rowSize(r); i++) {
				for (int c = 0; c < cells.cols(); c++) {
					out.print(fmt.format(this, r, c, Orientation.w, String.valueOf(v())));
					out.print(fmt.format(this, r, c, Orientation.c, cells.get(r, c, i)));
					if (c == cells.cols() - 1)
						out.println(fmt.format(this, r, c, Orientation.e, String.valueOf(v())));
				}
			}
		}
		printLastLine(out, cells.rows() - 1, cells, fmt);
	}

	private void printLine(PrintStream out, int r, Cells cells, Formatter fmt) {
		for (int c = 0; c < cells.cols(); c++) {
			out.print(fmt.format(this, r, c, Orientation.nw, String.valueOf(nw(r, c))));
			out.print(fmt.format(this, r, c, Orientation.n, String.valueOf(h(cells.colSize(c)))));
			if (c == cells.cols() - 1) out.println(
				fmt.format(this, r, c, Orientation.ne, String.valueOf(r == 0 ? ne() : e())));
		}
	}

	private void printLastLine(PrintStream out, int r, Cells cells, Formatter fmt) {
		for (int c = 0; c < cells.cols(); c++) {
			out.print(fmt.format(this, r, c, Orientation.sw, String.valueOf(c == 0 ? sw() : s())));
			out.print(fmt.format(this, r, c, Orientation.s, String.valueOf(h(cells.colSize(c)))));
			if (c == cells.cols() - 1)
				out.println(fmt.format(this, r, c, Orientation.se, String.valueOf(se())));
		}
	}

	private char nw(int r, int c) {
		if (r == 0 && c == 0) return nw();
		if (r == 0) return n();
		if (c == 0) return w();
		return c();
	}

	private static char ch(String s, int i) {
		return s.charAt(Math.min(i, s.length() - 1));
	}

	private static int maxLen(String... strings) {
		return Streams.of(strings).mapToInt(String::length).max(0);
	}
}
