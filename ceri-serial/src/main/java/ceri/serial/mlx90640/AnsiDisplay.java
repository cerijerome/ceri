package ceri.serial.mlx90640;

import static ceri.common.text.AnsiEscape.csi;
import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import java.io.PrintStream;
import java.util.function.DoubleUnaryOperator;
import ceri.common.color.ColorUtil;
import ceri.common.color.HsbColor;
import ceri.common.math.MathUtil;

/**
 * Outputs an animated ANSI-color terminal display of frames.
 */
public class AnsiDisplay {
	private final PrintStream out;
	private final DoubleUnaryOperator hueFn;
	private final double saturation;
	private final double brightness;
	private final Double min;
	private final Double max;
	private boolean printed = false;

	public static class Builder {
		PrintStream out = System.out;
		DoubleUnaryOperator hueFn = d -> d;
		double saturation = 0.75;
		double brightness = 0.9;
		Double min = null;
		Double max = null;

		Builder() {}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder hueFn(DoubleUnaryOperator hueFn) {
			this.hueFn = hueFn;
			return this;
		}

		public Builder saturation(double saturation) {
			this.saturation = saturation;
			return this;
		}

		public Builder brightness(double brightness) {
			this.brightness = brightness;
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

		public AnsiDisplay build() {
			return new AnsiDisplay(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	AnsiDisplay(Builder builder) {
		out = builder.out;
		hueFn = builder.hueFn;
		saturation = builder.saturation;
		brightness = builder.brightness;
		min = builder.min;
		max = builder.max;
	}

	public AnsiDisplay reset() {
		printed = false;
		return this;
	}
	
	/**
	 * Prints context information and color frame. Subsequent printing overwrites.
	 */
	public AnsiDisplay print(MlxFrame frame) {
		double min = min(frame);
		double max = max(frame);
		if (printed) System.out.print(csi.cursorUp(ROWS + 2));
		out.printf("  Params: mode=%s Vdd=%s Ta=%s Tr=%s e=%s %n", frame.mode(), f(frame.vdd(), 2),
			f(frame.ta(), 1), f(frame.tr(), 1), f(frame.emissivity(), 2));
		out.printf("  Range:  %.1f - %.1f \u00b0C %n", frame.min(), frame.max());
		for (int i = 0; i < ROWS; i++) {
			out.print("  ");
			for (int j = 0; j < COLUMNS; j++) {
				double ratio = (frame.value(i, j) - min) / (max - min);
				int rgb = rgb(ratio);
				out.print(csi.sgr().bgColor8(rgb) + "  ");
			}
			out.println(csi.sgr().reset());
		}
		printed = true;
		return this;
	}

	private String f(double d, int places) {
		return String.valueOf(MathUtil.simpleRound(d, places));
	}

	private double min(MlxFrame frame) {
		return min == null ? frame.min() : min;
	}

	private double max(MlxFrame frame) {
		return max == null ? frame.max() : max;
	}

	private int rgb(double value) {
		while (value > 1.0) value -= 1.0;
		while (value < 0.0) value += 1.0;
		double hue = hueFn.applyAsDouble(value);
		HsbColor hsb = HsbColor.of(hue, saturation, brightness);
		return ColorUtil.rgb(hsb.asColor());
	}

}
