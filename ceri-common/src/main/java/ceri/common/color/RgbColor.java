package ceri.common.color;

import static ceri.common.color.Colors.MAX_RATIO;
import static ceri.common.color.Colors.ratio;
import static ceri.common.color.Colors.value;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import ceri.common.math.MathUtil;

/**
 * Encapsulates an unscaled (0-1 inclusive) RGB color with alpha. Provides higher precision for
 * color calculations than an (a)rgb int value. May encapsulate any conceptual rgb values, such as
 * sRGB compressed and sRGB linear values.
 */
public record RgbColor(double a, double r, double g, double b) {

	public static final RgbColor clear = RgbColor.of(0, 0, 0, 0);
	public static final RgbColor black = RgbColor.of(0, 0, 0);
	public static final RgbColor white = RgbColor.of(MAX_RATIO, MAX_RATIO, MAX_RATIO);

	/**
	 * Construct from color.
	 */
	public static RgbColor from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from rgb int value.
	 */
	public static RgbColor fromRgb(int rgb) {
		return from(Colors.r(rgb), Colors.g(rgb), Colors.b(rgb));
	}

	/**
	 * Construct from argb int value.
	 */
	public static RgbColor from(int argb) {
		return from(Colors.a(argb), Colors.r(argb), Colors.g(argb), Colors.b(argb));
	}

	/**
	 * Construct an opaque instance from rgb 0-255 component values.
	 */
	public static RgbColor from(int r, int g, int b) {
		return of(ratio(r), ratio(g), ratio(b));
	}

	/**
	 * Construct from argb 0-255 component values.
	 */
	public static RgbColor from(int a, int r, int g, int b) {
		return of(ratio(a), ratio(r), ratio(g), ratio(b));
	}

	/**
	 * Construct an opaque instance from rgb 0-1 component values.
	 */
	public static RgbColor of(double r, double g, double b) {
		return of(MAX_RATIO, r, g, b);
	}

	/**
	 * Construct from argb 0-1 component values.
	 */
	public static RgbColor of(double a, double r, double g, double b) {
		return new RgbColor(a, r, g, b);
	}

	/**
	 * Provide rgb 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return new double[] { r, g, b };
	}

	/**
	 * Convert to argb int. Alpha is maintained.
	 */
	public int argb() {
		return Colors.argb(value(a), value(r), value(g), value(b));
	}

	/**
	 * Convert to color. Alpha is maintained.
	 */
	public Color color() {
		return Colors.color(argb());
	}

	/**
	 * Convert to HSB. Alpha is maintained.
	 */
	public HsbColor hsb() {
		double[] hsb = hsbValues();
		return HsbColor.of(a, hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Convert to HSB 0-1 values. Alpha is dropped.
	 */
	public double[] hsbValues() {
		return ColorSpaces.srgbToHsb(r, g, b);
	}

	/**
	 * Convert as sRGB to CIE XYZ. Alpha is maintained.
	 */
	public XyzColor xyz() {
		double[] xyz = xyzValues();
		return XyzColor.of(a, xyz[0], xyz[1], xyz[2]);
	}

	/**
	 * Convert from sRGB to CIE XYZ 0-1 values. Alpha is dropped.
	 */
	public double[] xyzValues() {
		return ColorSpaces.srgbToXyz(r, g, b);
	}

	/**
	 * Convert from sRGB to CIE xyY. Alpha is maintained.
	 */
	public XybColor xyb() {
		double[] xyb = xybValues();
		return XybColor.of(a, xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Convert from sRGB to CIE xyY 0-1 values. Alpha is dropped.
	 */
	public double[] xybValues() {
		return ColorSpaces.srgbToXyb(r, g, b);
	}

	/**
	 * Apply alpha to convert to an opaque color.
	 */
	public RgbColor applyAlpha() {
		if (a == MAX_RATIO) return this;
		return of(r * a, g * a, b * a);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < MAX_RATIO;
	}

	/**
	 * Dims rgb components by given ratio.
	 */
	public RgbColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, r * ratio, g * ratio, b * ratio);
	}

	/**
	 * Normalize components, limiting a, and scaling rgb so that max and min are 0-1.
	 */
	public RgbColor normalize() {
		double a = Colors.limit(this.a);
		double min = MathUtil.min(r, g, b, 0);
		double d = MathUtil.max(r, g, b, 1) - min;
		if (min == 0 && d == 1 && a == this.a) return this;
		return of(a, (this.r - min) / d, (this.g - min) / d, (this.b - min) / d);
	}

	/**
	 * Truncates components to range 0-1.
	 */
	public RgbColor limit() {
		double a = Colors.limit(this.a);
		double r = Colors.limit(this.r);
		double g = Colors.limit(this.g);
		double b = Colors.limit(this.b);
		if (a == this.a && r == this.r && g == this.g && b == this.b) return this;
		return of(a, r, g, b);
	}

	/**
	 * Validates all components are 0-1. Throws an exception if not.
	 */
	public void verify() {
		validate(a, "alpha");
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,r=%.5f,g=%.5f,b=%.5f)", a, r, g, b);
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_RATIO, name);
	}
}
