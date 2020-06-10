package ceri.serial.mlx90640.util;

import static ceri.common.text.AnsiEscape.csi;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import java.io.PrintStream;
import java.util.function.DoubleUnaryOperator;
import ceri.common.color.ColorUtil;
import ceri.common.color.HsbColor;
import ceri.serial.mlx90640.MlxFrame;

/**
 * Outputs an animated ANSI-color terminal display of frames.
 */
public class AnsiDisplay extends TerminalDisplay {
	private final DoubleUnaryOperator hueFn;
	private final double saturation;
	private final double brightness;
	private boolean printed = false;

	public static class Builder extends TerminalDisplay.Builder {
		DoubleUnaryOperator hueFn = h -> shiftHue(h, 0.333);
		double saturation = 0.75;
		double brightness = 0.9;

		Builder() {}

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

		@Override
		public Builder out(PrintStream out) {
			super.out(out);
			return this;
		}

		@Override
		public Builder min(Double min) {
			super.min(min);
			return this;
		}

		@Override
		public Builder max(Double max) {
			super.max(max);
			return this;
		}

		@Override
		public AnsiDisplay build() {
			return new AnsiDisplay(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	AnsiDisplay(Builder builder) {
		super(builder);
		hueFn = builder.hueFn;
		saturation = builder.saturation;
		brightness = builder.brightness;
	}

	public AnsiDisplay reset() {
		printed = false;
		return this;
	}

	/**
	 * Shifts a hue value.
	 */
	public static double shiftHue(double hue, double shift) {
		hue += shift;
		while (hue > 1.0) hue -= 1.0;
		while (hue < 0.0) hue += 1.0;
		return hue;
	}
	
	/**
	 * Prints context information and color frame. Subsequent printing overwrites.
	 */
	@Override
	public AnsiDisplay print(MlxFrame frame) {
		if (printed) out.print(csi.cursorUp(ROWS + 1));
		super.print(frame);
		printed = true;
		return this;
	}

	@Override
	protected void printPixel(int row, int column, double value, double min, double max) {
		double hue = hueFn.applyAsDouble(ratio(value, min, max));
		HsbColor hsb = HsbColor.of(hue, saturation, brightness);
		int rgb = ColorUtil.rgb(hsb.asColor());
		out.print(csi.sgr().bgColor8(rgb) + "  ");
		if (lastColumn(column)) out.print(csi.sgr().reset());
	}

}
