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

	/**
	 * Creates a line from origin to the given point.
	 */
	public static Line2d create(double x, double y) {
		return create(new Point2d(x, y));
	}

	/**
	 * Creates a line from origin to the given point.
	 */
	public static Line2d create(Point2d to) {
		return create(Point2d.ZERO, to);
	}

	/**
	 * Creates a line from one point to another.
	 */
	public static Line2d create(double x0, double y0, double x1, double y1) {
		return create(new Point2d(x0, y0), new Point2d(x1, y1));
	}

	/**
	 * Creates a line from one point to another.
	 */
	public static Line2d create(Point2d from, Point2d to) {
		return new Line2d(from, to);
	}

	private Line2d(Point2d from, Point2d to) {
		this.from = from;
		this.to = to;
		vector = new Point2d(to.x - from.x, to.y - from.y);
	}

	/**
	 * Reflect the given point in the extended line.
	 */
	public Point2d reflect(Point2d point) {
		return Line2dEquation.from(this).reflect(point);
	}

	/**
	 * Translate the line by given offset.
	 */
	public Line2d translate(Point2d offset) {
		return new Line2d(from.translate(offset), to.translate(offset));
	}

	/**
	 * Scale the line by given ratios.
	 */
	public Line2d scale(Ratio2d scale) {
		return new Line2d(from.scale(scale), to.scale(scale));
	}

	/**
	 * Angle of the line.
	 */
	public double angle() {
		if (Point2d.ZERO.equals(vector)) return Double.NaN;
		return Math.atan2(vector.y, vector.x);
	}

	/**
	 * Gradient of the line.
	 */
	public double gradient() {
		if (Point2d.ZERO.equals(vector)) return Double.NaN;
		return vector.y / vector.x;
	}

	/**
	 * Length of the line.
	 */
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
