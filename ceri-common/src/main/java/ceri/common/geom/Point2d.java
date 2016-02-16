package ceri.common.geom;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Point2d {
	public static final Point2d ZERO = new Point2d(0, 0);
	public static final Point2d X_UNIT = new Point2d(1, 0);
	public static final Point2d Y_UNIT = new Point2d(0, 1);
	public final double x;
	public final double y;

	public Point2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point2d translate(Point2d offset) {
		return new Point2d(x + offset.x, y + offset.y);
	}
	
	public Point2d scale(Ratio2d scale) {
		// +0.0 converts any -0.0 to 0.0
		return new Point2d(x * scale.x + 0.0, y * scale.y + 0.0);
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Point2d)) return false;
		Point2d other = (Point2d) obj;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

}
