package ceri.serial.mlx90640.display;

import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import java.io.PrintStream;
import ceri.common.math.MathUtil;
import ceri.serial.mlx90640.MlxFrame;

/**
 * Outputs rounded temperature grid to terminal. Designed to be extended for other displays.
 */
public class TerminalDisplay {
	protected final PrintStream out;
	private final Double min;
	private final Double max;

	public static class Builder {
		PrintStream out = System.out;
		Double min = null;
		Double max = null;

		Builder() {}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder min(double min) {
			this.min = min;
			return this;
		}

		public Builder max(double max) {
			this.max = max;
			return this;
		}

		public TerminalDisplay build() {
			return new TerminalDisplay(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	protected TerminalDisplay(Builder builder) {
		out = builder.out;
		min = builder.min;
		max = builder.max;
	}

	/**
	 * Prints context information and frame.
	 */
	public TerminalDisplay print(MlxFrame frame) {
		double min = min(frame);
		double max = max(frame);
		printContext(frame);
		for (int row = 0; row < ROWS; row++)
			printRow(frame, row, min, max);
		return this;
	}

	/**
	 * Print context information for the frame.
	 */
	protected void printContext(MlxFrame frame) {
		out.printf("  %.1f-%.1f\u00b0C  mode=%s  Vdd=%s  Ta=%s  Tr=%s  e=%s  %n", frame.min(),
			frame.max(), frame.mode(), f(frame.vdd(), 2), f(frame.ta(), 1), f(frame.tr(), 1),
			f(frame.emissivity(), 2));
	}

	/**
	 * Print a frame row.
	 */
	protected void printRow(MlxFrame frame, int row, double min, double max) {
		out.print("  ");
		for (int column = 0; column < COLUMNS; column++) {
			double value = frame.value(row, column);
			printPixel(row, column, value, min, max);
		}
		out.println();
	}

	/**
	 * Print a frame pixel.
	 */
	@SuppressWarnings("unused")
	protected void printPixel(int row, int column, double value, double min, double max) {
		out.printf("%3s", Math.round(value));
	}

	protected static boolean lastColumn(int column) {
		return column == COLUMNS - 1;
	}

	protected static boolean lastRow(int row) {
		return row == ROWS - 1;
	}

	protected static String f(double d, int places) {
		return String.valueOf(MathUtil.simpleRound(d, places));
	}

	protected double ratio(double value, double min, double max) {
		double ratio = (value - min) / (max - min);
		if (ratio < 0) ratio = 0;
		if (ratio > 1) ratio = 1;
		return ratio;
	}

	private double min(MlxFrame frame) {
		return min == null ? frame.min() : min;
	}

	private double max(MlxFrame frame) {
		return max == null ? frame.max() : max;
	}

}
