package ceri.common.geom;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Line2d {
	public static final Line2d ZERO = new Line2d(Point2d.ZERO, Point2d.ZERO);
	public static final Line2d X_UNIT = new Line2d(Point2d.ZERO, Point2d.X_UNIT);
	public static final Line2d Y_UNIT = new Line2d(Point2d.ZERO, Point2d.Y_UNIT);
	public final Point2d from;
	public final Point2d to;
	public final Point2d vector;

	public static Line2d create(Point2d to) {
		return create(Point2d.ZERO, to);
	}

	public static Line2d create(Point2d from, Point2d to) {
		return new Line2d(from, to);
	}

	private Line2d(Point2d from, Point2d to) {
		this.from = from;
		this.to = to;
		vector = new Point2d(to.x - from.x, to.y - from.y);
	}

	public Point2d reflect(Point2d point) {
		return Line2dEquation.from(this).reflect(point);
	}

	public Line2d translate(Point2d offset) {
		return new Line2d(from.translate(offset), to.translate(offset));
	}

	public Line2d scale(Ratio2d scale) {
		return new Line2d(from.scale(scale), to.scale(scale));
	}

	public double angle() {
		return Math.atan2(vector.y, vector.x);
	}

	public double length() {
		return Math.hypot(vector.x, vector.y);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(from, to);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Line2d)) return false;
		Line2d other = (Line2d) obj;
		if (!EqualsUtil.equals(from, other.from)) return false;
		if (!EqualsUtil.equals(to, other.to)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, from, to).toString();
	}

}
