package ceri.common.color;

import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.geom.Point2d;
import ceri.common.math.MathUtil;

/**
 * Represents xyY color, Y = brightness (b).
 */
public class XybColor {
	public static final double MAX_VALUE = 1.0;
	public static final Point2d CENTER = XyzColor.CIE_E.xyb().xy();
	public final double a;
	public final double x;
	public final double y;
	public final double b;

	public static XybColor from(int argb) {
		double[] xyb = ColorSpaces.rgbToXyb(argb);
		return of(ColorUtil.ratio(ColorUtil.a(argb)), xyb[0], xyb[1], xyb[2]);
	}

	public static XybColor full(Point2d xy) {
		return full(xy.x, xy.y);
	}

	public static XybColor full(double x, double y) {
		return of(x, y, MAX_VALUE);
	}

	public static XybColor of(Point2d xy, double b) {
		return of(xy.x, xy.y, b);
	}

	public static XybColor of(double x, double y, double b) {
		return of(MAX_VALUE, x, y, b);
	}

	public static XybColor of(double a, Point2d xy, double b) {
		return of(a, xy.x, xy.y, b);
	}

	public static XybColor of(double a, double x, double y, double b) {
		return new XybColor(x, y, b, a);
	}

	private XybColor(double x, double y, double b, double a) {
		this.x = x;
		this.y = y;
		this.b = b;
		this.a = a;
	}

	public double[] values() {
		return new double[] { x, y, b };
	}

	public Point2d xy() {
		return Point2d.of(x, y);
	}

	public double[] xyzValues() {
		return ColorSpaces.xybToXyz(x, y, b);
	}

	public XyzColor xyz() {
		double[] xyz = xyzValues();
		return XyzColor.of(a, xyz[0], xyz[1], xyz[2]);
	}

	public int argb() {
		return ColorUtil.alphaArgb(ColorUtil.value(a), ColorSpaces.xybToRgb(x, y, b));
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	public XybColor normalize() {
		double a = limit(this.a);
		Point2d xy = normalize(x, y);
		double b = limit(this.b);
		if (a == this.a && xy.x == this.x && xy.y == this.y && b == this.b) return this;
		return of(a, xy, b);
	}

	public XybColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, x, y, b * ratio);
	}

	private Point2d normalize(double x, double y) {
		// normalize around CIE_E
		double factor = MathUtil.max(MAX_VALUE, //
			(x - CENTER.x) / (MAX_VALUE - CENTER.x), (CENTER.x - x) / CENTER.x,
			(y - CENTER.y) / (MAX_VALUE - CENTER.y), (CENTER.y - y) / CENTER.y);
		if (factor == MAX_VALUE) return Point2d.of(x, y);
		x = ((x - CENTER.x) / factor) + CENTER.x;
		y = ((y - CENTER.y) / factor) + CENTER.y;
		return Point2d.of(x, y);
	}

	public XybColor limit() {
		double a = limit(this.a);
		double x = limit(this.x);
		double y = limit(this.y);
		double b = limit(this.b);
		if (a == this.a && x == this.x && y == this.y && b == this.b) return this;
		return of(a, x, y, b);
	}

	public void verify() {
		validate(a, "alpha");
		validate(x, "x");
		validate(y, "y");
		validate(b, "brightness");
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
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
}
