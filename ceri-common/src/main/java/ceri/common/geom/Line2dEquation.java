package ceri.common.geom;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Line formula ax + by + c = 0.
 */
public class Line2dEquation {
	public final double a;
	public final double b;
	public final double c;

	public static Line2dEquation from(Line2d line) {
		return between(line.from, line.to);
	}

	public static Line2dEquation between(Point2d from, Point2d to) {
		if (from.equals(to)) throw new IllegalArgumentException("Points cannot be identical: " +
			from + ", " + to);
		double dx = to.x - from.x;
		double dy = to.y - from.y;
		if (dx == 0) return new Line2dEquation(1, 0, -from.x);
		if (dy == 0) return new Line2dEquation(0, 1, -from.y);
		double b = -dx / dy;
		double c = -from.x - (b * from.y);
		return new Line2dEquation(1, b, c);
	}

	public Line2dEquation(double a, double b, double c) {
		if (a == 0 && b == 0) throw new IllegalArgumentException("a and b cannot both be 0: " + a +
			", " + b);
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Point2d reflect(Point2d point) {
		double aa = a * a;
		double bb = b * b;
		double x = ((point.x * (bb - aa)) - (point.y * a * b * 2) - (a * c * 2)) / (aa + bb);
		double y = ((point.y * (aa - bb)) - (point.x * a * b * 2) - (b * c * 2)) / (aa + bb);
		return new Point2d(x, y);
	}
	
	public double angle() {
		if (a == 0) return 0;
		if (b == 0) return Math.PI / 2;
		return Math.atan2(-a, b);
	}
	
	public double gradient() {
		if (a == 0) return 0;
		if (b == 0) return Double.POSITIVE_INFINITY;
		return -a / b;
	}
	
	public Line2dEquation normalize() {
		if (a != 0) return new Line2dEquation(1, b / a, c / a);
		return new Line2dEquation(a / b, 1, c / b);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Line2dEquation)) return false;
		Line2dEquation other = (Line2dEquation) obj;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, a, b, c).toString();
	}

}
