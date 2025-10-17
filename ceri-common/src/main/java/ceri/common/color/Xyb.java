package ceri.common.color;

import java.awt.Color;
import ceri.common.geom.Point2d;
import ceri.common.math.Maths;

/**
 * Represents CIE xyY color, Y = brightness (b). All values approximately 0-1.
 */
public record Xyb(double a, double x, double y, double b) {

	public static final Point2d CENTER = Xyz.CIE_E.xyb().xy();

	/**
	 * Construct from sRGB color. Alpha is maintained.
	 */
	public static Xyb from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from sRGB int value.
	 */
	public static Xyb fromRgb(int rgb) {
		double[] xyb = ColorSpaces.rgbToXyb(rgb);
		return of(xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Construct from sRGB int value. Alpha is maintained.
	 */
	public static Xyb from(int argb) {
		double[] xyb = ColorSpaces.rgbToXyb(argb);
		return of(Colors.ratio(Colors.a(argb)), xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Construct an opaque instance from CIE xy 0-1 values, with full brightness.
	 */
	public static Xyb full(double x, double y) {
		return of(x, y, Colors.MAX_RATIO);
	}

	/**
	 * Construct an opaque instance from CIE xyY 0-1 values.
	 */
	public static Xyb of(double x, double y, double b) {
		return of(Colors.MAX_RATIO, x, y, b);
	}

	/**
	 * Construct from alpha + CIE xyY 0-1 values.
	 */
	public static Xyb of(double a, double x, double y, double b) {
		return new Xyb(a, x, y, b);
	}

	/**
	 * Provide xyY 0-1 values. Alpha is dropped.
	 */
	public double[] xybValues() {
		return new double[] { x(), y(), b() };
	}

	/**
	 * Provide xy 0-1 values. Alpha is dropped.
	 */
	public Point2d xy() {
		return Point2d.of(x(), y());
	}

	/**
	 * Convert to sRGB int. Alpha is maintained.
	 */
	public int argb() {
		return Component.a.set(ColorSpaces.xybToRgb(x(), y(), b()), Colors.value(a()));
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
	public Rgb rgb() {
		double[] rgb = rgbValues();
		return Rgb.of(a(), rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert to sRGB 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return ColorSpaces.xybToSrgb(x(), y(), b());
	}

	/**
	 * Convert to CIE XYZ. Alpha is maintained.
	 */
	public Xyz xyz() {
		double[] xyz = xyzValues();
		return Xyz.of(a(), xyz[0], xyz[1], xyz[2]);
	}

	/**
	 * Convert to CIE XYZ 0-1 values. Alpha is dropped.
	 */
	public double[] xyzValues() {
		return ColorSpaces.xybToXyz(x(), y(), b());
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a() < Colors.MAX_RATIO;
	}

	/**
	 * Dims brightness by given ratio.
	 */
	public Xyb dim(double ratio) {
		if (ratio == 1) return this;
		return of(a(), x(), y(), b() * ratio);
	}

	/**
	 * Normalizes xy around CIE E, limits alpha and brightness to 0-1.
	 */
	public Xyb normalize() {
		double a = Colors.limit(a());
		var xy = normalize(x(), y());
		double b = Colors.limit(b());
		if (a == a() && xy.x() == x() && xy.y() == y() && b == b()) return this;
		return of(a, xy.x(), xy.y(), b);
	}

	/**
	 * Limits values 0-1.
	 */
	public Xyb limit() {
		double a = Colors.limit(a());
		double x = Colors.limit(x());
		double y = Colors.limit(y());
		double b = Colors.limit(b());
		if (a == a() && x == x() && y == y() && b == b()) return this;
		return of(a, x, y, b);
	}

	/**
	 * Verifies values are 0-1. Throws an exception if not.
	 */
	public void verify() {
		Colors.validRatio(a(), "alpha");
		Colors.validRatio(x(), "x");
		Colors.validRatio(y(), "y");
		Colors.validRatio(b(), "brightness");
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,b=%.5f)", a(), x(), y(), b());
	}

	private Point2d normalize(double x, double y) {
		// normalize around CIE_E
		double factor = Maths.max(Colors.MAX_RATIO,
			(x - CENTER.x()) / (Colors.MAX_RATIO - CENTER.x()), (CENTER.x() - x) / CENTER.x(),
			(y - CENTER.y()) / (Colors.MAX_RATIO - CENTER.y()), (CENTER.y() - y) / CENTER.y());
		if (factor == Colors.MAX_RATIO) return Point2d.of(x, y);
		x = ((x - CENTER.x()) / factor) + CENTER.x();
		y = ((y - CENTER.y()) / factor) + CENTER.y();
		return Point2d.of(x, y);
	}
}
