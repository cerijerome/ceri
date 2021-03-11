package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.a;
import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.color.ColorUtil.value;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.geom.Point2d;
import ceri.common.math.MathUtil;

/**
 * Represents CIE xyY color, Y = brightness (b). All values approximately 0-1.
 */
public class XybColor {
	public static final Point2d CENTER = XyzColor.CIE_E.xyb().xy();
	public final double a;
	public final double x;
	public final double y;
	public final double b;

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
		return of(ratio(a(argb)), xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Construct an opaque instance from CIE xy 0-1 values, with full brightness.
	 */
	public static XybColor full(double x, double y) {
		return of(x, y, MAX_RATIO);
	}

	/**
	 * Construct an opaque instance from CIE xyY 0-1 values.
	 */
	public static XybColor of(double x, double y, double b) {
		return of(MAX_RATIO, x, y, b);
	}

	/**
	 * Construct from alpha + CIE xyY 0-1 values.
	 */
	public static XybColor of(double a, double x, double y, double b) {
		return new XybColor(x, y, b, a);
	}

	private XybColor(double x, double y, double b, double a) {
		this.x = x;
		this.y = y;
		this.b = b;
		this.a = a;
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
		return Component.a.set(ColorSpaces.xybToRgb(x, y, b), value(a));
	}

	/**
	 * Convert to sRGB color. Alpha is maintained.
	 */
	public Color color() {
		return ColorUtil.color(argb());
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
		return a < MAX_RATIO;
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
		double a = ColorUtil.limit(this.a);
		Point2d xy = normalize(x, y);
		double b = ColorUtil.limit(this.b);
		if (a == this.a && xy.x == this.x && xy.y == this.y && b == this.b) return this;
		return of(a, xy.x, xy.y, b);
	}

	/**
	 * Limits values 0-1.
	 */
	public XybColor limit() {
		double a = ColorUtil.limit(this.a);
		double x = ColorUtil.limit(this.x);
		double y = ColorUtil.limit(this.y);
		double b = ColorUtil.limit(this.b);
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
	public int hashCode() {
		return Objects.hash(a, x, y, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof XybColor)) return false;
		XybColor other = (XybColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(x, other.x)) return false;
		if (!Objects.equals(y, other.y)) return false;
		if (!Objects.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,b=%.5f)", a, x, y, b);
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_RATIO, name);
	}

	private Point2d normalize(double x, double y) {
		// normalize around CIE_E
		double factor = MathUtil.max(MAX_RATIO, //
			(x - CENTER.x) / (MAX_RATIO - CENTER.x), (CENTER.x - x) / CENTER.x,
			(y - CENTER.y) / (MAX_RATIO - CENTER.y), (CENTER.y - y) / CENTER.y);
		if (factor == MAX_RATIO) return Point2d.of(x, y);
		x = ((x - CENTER.x) / factor) + CENTER.x;
		y = ((y - CENTER.y) / factor) + CENTER.y;
		return Point2d.of(x, y);
	}
}
