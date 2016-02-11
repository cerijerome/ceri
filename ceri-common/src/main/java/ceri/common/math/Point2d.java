package ceri.common.math;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Point2d {
	public final double x;
	public final double y;

	public Point2d(double x, double y) {
		this.x = x;
		this.y = y;
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
