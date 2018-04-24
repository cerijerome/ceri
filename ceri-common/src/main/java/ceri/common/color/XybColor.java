package ceri.common.color;

import static ceri.common.validation.ValidationUtil.validateRange;
import ceri.common.geom.Point2d;
import ceri.common.math.MathUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Represents xyY color, Y = brightness (b).
 */
public class XybColor implements ComponentColor<XybColor> {
	public static final Point2d CENTER = XyzColor.CIE_E.toXyb().xy();
	public static final double MAX_VALUE = 1.0;
	public final double x;
	public final double y;
	public final double b;
	public final double a;

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
		return of(x, y, b, MAX_VALUE);
	}

	public static XybColor of(Point2d xy, double b, double a) {
		return of(xy.x, xy.y, b, a);
	}

	public static XybColor of(double x, double y, double b, double a) {
		return new XybColor(x, y, b, a);
	}

	private XybColor(double x, double y, double b, double a) {
		this.x = x;
		this.y = y;
		this.b = b;
		this.a = a;
	}

	public XyzColor toXyz() {
		if (y == 0.0) return XyzColor.of(0, 0, 0, a);
		double y = b;
		double x = (y / this.y) * this.x;
		double z = (y / this.y) * (MAX_VALUE - this.x - this.y);
		return XyzColor.of(x, y, z, a);
	}

	public Point2d xy() {
		return Point2d.of(x, y);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public XybColor normalize() {
		Point2d xy = normalize(x, y);
		double b = limit(this.b);
		double a = limit(this.a);
		if (xy.x == this.x && xy.y == this.y && b == this.b && a == this.a) return this;
		return of(xy, b, a);
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

	@Override
	public XybColor limit() {
		double x = limit(this.x);
		double y = limit(this.y);
		double b = limit(this.b);
		double a = limit(this.a);
		if (x == this.x && y == this.y && b == this.b && a == this.a) return this;
		return of(x, y, b, a);
	}

	@Override
	public void verify() {
		validate(x, "x");
		validate(y, "y");
		validate(b, "brightness");
		validate(a, "alpha");
	}

	private void validate(double value, String name) {
		validateRange(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(x, y, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof XybColor)) return false;
		XybColor other = (XybColor) obj;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		return hasAlpha() ? String.format("%s[x=%.5f,y=%.5f,b=%.5f,a=%.5f]", name, x, y, b, a) :
			String.format("%s[x=%.5f,y=%.5f,b=%.5f]", name, x, y, b);
	}

}
