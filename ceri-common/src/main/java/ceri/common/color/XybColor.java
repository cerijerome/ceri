package ceri.common.color;

import ceri.common.geom.Point2d;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Represents xyY color, Y = brightness (b).
 */
public class XybColor {
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

	public boolean hasAlpha() {
		return a < MAX_VALUE;
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
