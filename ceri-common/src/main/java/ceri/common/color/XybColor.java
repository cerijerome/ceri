package ceri.common.color;

import java.awt.Color;
import ceri.common.geom.Point2d;
import ceri.common.math.Maths;
import ceri.common.util.Validate;

/**
 * Represents CIE xyY color, Y = brightness (b). All values approximately 0-1.
 */
public record XybColor(double a, double x, double y, double b) {

	public static final Point2d CENTER = XyzColor.CIE_E.xyb().xy();

	/**
	 * Construct from sRGB color. Alpha is maintained.
	 */
	public static XybColor from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from sRGB int value.
	 */
	public static XybColor fromRgb(int rgb) {
		double[] xyb = ColorSpaces.rgbToXyb(rgb);
		return of(xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Construct from sRGB int value. Alpha is maintained.
	 */
	public static XybColor from(int argb) {
		double[] xyb = ColorSpaces.rgbToXyb(argb);
		return of(Colors.ratio(Colors.a(argb)), xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Construct an opaque instance from CIE xy 0-1 values, with full brightness.
	 */
	public static XybColor full(double x, double y) {
		return of(x, y, Colors.MAX_RATIO);
	}

	/**
	 * Construct an opaque instance from CIE xyY 0-1 values.
	 */
	public static XybColor of(double x, double y, double b) {
		return of(Colors.MAX_RATIO, x, y, b);
	}

	/**
	 * Construct from alpha + CIE xyY 0-1 values.
	 */
	public static XybColor of(double a, double x, double y, double b) {
		return new XybColor(a, x, y, b);
	}

	/**
	 * Provide xyY 0-1 values. Alpha is dropped.
	 */
	public double[] xybValues() {
		return new double[] { x, y, b };
	}

	/**
	 * Provide xy 0-1 values. Alpha is dropped.
	 */
	public Point2d xy() {
		return Point2d.of(x, y);
	}

	/**
	 * Convert to sRGB int. Alpha is maintained.
	 */
	public int argb() {
		return Component.a.set(ColorSpaces.xybToRgb(x, y, b), Colors.value(a));
	}

	/**
	 * Convert to sRGB color. Alpha is maintained.
	 */
	public Color color() {
		return Colors.color(argb());
	}

	/**
	 * Convert to sRGB. Alpha is maintained.
	 */
	public RgbColor rgb() {
		double[] rgb = rgbValues();
		return RgbColor.of(a, rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert to sRGB 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return ColorSpaces.xybToSrgb(x, y, b);
	}

	/**
	 * Convert to CIE XYZ. Alpha is maintained.
	 */
	public XyzColor xyz() {
		double[] xyz = xyzValues();
		return XyzColor.of(a, xyz[0], xyz[1], xyz[2]);
	}

	/**
	 * Convert to CIE XYZ 0-1 values. Alpha is dropped.
	 */
	public double[] xyzValues() {
		return ColorSpaces.xybToXyz(x, y, b);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < Colors.MAX_RATIO;
	}

	/**
	 * Dims brightness by given ratio.
	 */
	public XybColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, x, y, b * ratio);
	}

	/**
	 * Normalizes xy around CIE E, limits alpha and brightness to 0-1.
	 */
	public XybColor normalize() {
		double a = Colors.limit(this.a);
		var xy = normalize(x, y);
		double b = Colors.limit(this.b);
		if (a == this.a && xy.x == this.x && xy.y == this.y && b == this.b) return this;
		return of(a, xy.x, xy.y, b);
	}

	/**
	 * Limits values 0-1.
	 */
	public XybColor limit() {
		double a = Colors.limit(this.a);
		double x = Colors.limit(this.x);
		double y = Colors.limit(this.y);
		double b = Colors.limit(this.b);
		if (a == this.a && x == this.x && y == this.y && b == this.b) return this;
		return of(a, x, y, b);
	}

	/**
	 * Verifies values are 0-1. Throws an exception if not.
	 */
	public void verify() {
		validate(a, "alpha");
		validate(x, "x");
		validate(y, "y");
		validate(b, "brightness");
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,b=%.5f)", a, x, y, b);
	}

	private void validate(double value, String name) {
		Validate.validateRangeFp(value, 0, Colors.MAX_RATIO, name);
	}

	private Point2d normalize(double x, double y) {
		// normalize around CIE_E
		double factor = Maths.max(Colors.MAX_RATIO, //
			(x - CENTER.x) / (Colors.MAX_RATIO - CENTER.x), (CENTER.x - x) / CENTER.x,
			(y - CENTER.y) / (Colors.MAX_RATIO - CENTER.y), (CENTER.y - y) / CENTER.y);
		if (factor == Colors.MAX_RATIO) return Point2d.of(x, y);
		x = ((x - CENTER.x) / factor) + CENTER.x;
		y = ((y - CENTER.y) / factor) + CENTER.y;
		return Point2d.of(x, y);
	}
}
