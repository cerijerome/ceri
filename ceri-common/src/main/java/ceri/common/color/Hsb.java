package ceri.common.color;

import java.awt.Color;

/**
 * Encapsulates an HSB color with alpha, all values 0-1 inclusive.
 */
public record Hsb(double a, double h, double s, double b) {

	public static final Hsb clear = Hsb.of(0, 0, 0, 0);
	public static final Hsb black = Hsb.of(0, 0, 0);
	public static final Hsb white = Hsb.of(0, 0, Colors.MAX_RATIO);

	/**
	 * Construct from color.
	 */
	public static Hsb from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from rgb int value.
	 */
	public static Hsb fromRgb(int rgb) {
		double[] hsb = ColorSpaces.rgbToHsb(rgb);
		return of(hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Construct from argb int value.
	 */
	public static Hsb from(int argb) {
		double[] hsb = ColorSpaces.rgbToHsb(argb);
		return of(Colors.ratio(Colors.a(argb)), hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Construct an opaque instance from HSB 0-1 values.
	 */
	public static Hsb of(double h, double s, double b) {
		return of(Colors.MAX_RATIO, h, s, b);
	}

	/**
	 * Construct alpha+HSB 0-1 values.
	 */
	public static Hsb of(double a, double h, double s, double b) {
		return new Hsb(a, h, s, b);
	}

	/**
	 * Construct maximized instance for given hue 0-1 value.
	 */
	public static Hsb max(double h) {
		return of(h, Colors.MAX_RATIO, Colors.MAX_RATIO);
	}

	/**
	 * Provide hsb 0-1 values. Alpha is dropped.
	 */
	public double[] hsbValues() {
		return new double[] { h, s, b };
	}

	/**
	 * Convert to argb int. Alpha is maintained.
	 */
	public int argb() {
		return ColorSpaces.hsbToRgb(h, s, b);
	}

	/**
	 * Convert to color. Alpha is maintained.
	 */
	public Color color() {
		return Colors.color(argb());
	}

	/**
	 * Convert to RGB. Alpha is maintained.
	 */
	public Rgb rgb() {
		double[] rgb = rgbValues();
		return Rgb.of(a, rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert to RGB 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return ColorSpaces.hsbToSrgb(h, s, b);
	}

	/**
	 * Return true if this color represents black.
	 */
	public boolean isBlack() {
		return b <= 0;
	}

	/**
	 * Return true if this color represents white.
	 */
	public boolean isWhite() {
		return s <= 0 && b >= Colors.MAX_RATIO;
	}

	/**
	 * Apply alpha to convert to an opaque color.
	 */
	public Hsb applyAlpha() {
		double b = Colors.limit(this.b * a);
		return b == this.b ? this : of(h, s, b);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < Colors.MAX_RATIO;
	}

	/**
	 * Shift hue by given amount, normalized to 0-1.
	 */
	public Hsb shiftHue(double delta) {
		double h = Colors.limitHue(this.h + delta);
		return h == this.h ? this : of(a, h, s, b);
	}

	/**
	 * Dims by given ratio.
	 */
	public Hsb dim(double ratio) {
		double b = Colors.limit(this.b * ratio);
		return b == this.b ? this : of(a, h, s, b);
	}

	/**
	 * Hue wraps to 0-1, other components truncated to 0-1.
	 */
	public Hsb normalize() {
		double a = Colors.limit(this.a);
		double h = Colors.limitHue(this.h);
		double s = Colors.limit(this.s);
		double b = Colors.limit(this.b);
		if (a == this.a && h == this.h && s == this.s && b == this.b) return this;
		return of(a, h, s, b);
	}

	/**
	 * Validates all components are 0-1. Throws an exception if not.
	 */
	public void verify() {
		Colors.validateRatio(a, "alpha");
		Colors.validateRatio(h, "hue");
		Colors.validateRatio(s, "saturation");
		Colors.validateRatio(b, "brightness");
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,h=%.5f,s=%.5f,b=%.5f)", a, h, s, b);
	}
}
