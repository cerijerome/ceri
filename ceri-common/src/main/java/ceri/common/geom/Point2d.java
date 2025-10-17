package ceri.common.geom;

import ceri.common.math.Matrix;
import ceri.common.util.Validate;

/**
 * A 2d point.
 */
public record Point2d(double x, double y) {
	public static final Point2d ZERO = new Point2d(0, 0);
	public static final Point2d X_UNIT = new Point2d(1, 0);
	public static final Point2d Y_UNIT = new Point2d(0, 1);

	/**
	 * Calculates the angle of the coordinates from the origin, relative to the x-axis.
	 */
	public static double angle(double x, double y) {
		if (x == 0.0 && y == 0.0) return Double.NaN;
		return Math.atan2(y, x);
	}

	/**
	 * Calculates the distance from the origin to the coordinates.
	 */
	public static double distance(double x, double y) {
		return Math.hypot(x, y);
	}

	/**
	 * Returns an instance calculated from polar coordinates.
	 */
	public static Point2d fromPolar(double radius, double angle) {
		return of(radius * Math.cos(angle), radius * Math.sin(angle));
	}

	/**
	 * Returns a point from column vector. Vector is validated for size.
	 */
	public static Point2d from(Matrix vector) {
		vector = vector.vector(2);
		return of(vector.at(0, 0), vector.at(1, 0));
	}

	/**
	 * Returns a validated instance.
	 */
	public static Point2d of(double x, double y) {
		if (ZERO.equals(x, y)) return ZERO;
		if (X_UNIT.equals(x, y)) return X_UNIT;
		if (Y_UNIT.equals(x, y)) return Y_UNIT;
		return new Point2d(x + 0.0, y + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Point2d {
		Validate.finite(x);
		Validate.finite(y);
	}

	/**
	 * Returns true if the coordinates are 0.
	 */
	public boolean isZero() {
		return equals(ZERO);
	}

	/**
	 * Rotates the coordinates by 180 degrees.
	 */
	public Point2d reverse() {
		return create(-x(), -y());
	}

	/**
	 * Moves the coordinates by given offset.
	 */
	public Point2d translate(Point2d offset) {
		return translate(offset.x(), offset.y());
	}

	/**
	 * Moves the coordinates by given offset.
	 */
	public Point2d translate(double x, double y) {
		return create(x() + x, y() + y);
	}

	/**
	 * Returns vector coordinates to the given point from this point .
	 */
	public Point2d to(Point2d end) {
		return to(end.x(), end.y());
	}

	/**
	 * Returns vector coordinates to the given point from this point .
	 */
	public Point2d to(double x, double y) {
		return create(x - x(), y - y());
	}

	/**
	 * Returns vector coordinates from the given point to this point .
	 */
	public Point2d from(Point2d start) {
		return from(start.x(), start.y());
	}

	/**
	 * Returns vector coordinates from the given point to this point .
	 */
	public Point2d from(double x, double y) {
		return create(x() - x, y() - y);
	}

	/**
	 * Scales the coordinates by the given ratios.
	 */
	public Point2d scale(Ratio2d scale) {
		return scale(scale.x(), scale.y());
	}

	/**
	 * Scales the coordinates by the given ratios.
	 */
	public Point2d scale(double x, double y) {
		if (Ratio2d.ZERO.equals(x, y)) return ZERO;
		if (Ratio2d.UNIT.equals(x, y)) return this;
		return of(x() * x + 0.0, y() * y + 0.0);
	}

	/**
	 * Calculates the angle of the coordinates from the origin, relative to the x-axis.
	 */
	public double angle() {
		return angle(x(), y());
	}

	/**
	 * Calculates the distance from the origin to the coordinates.
	 */
	public double distance() {
		return distance(x(), y());
	}

	/**
	 * Calculates the squared distance from the origin to the coordinates.
	 */
	public double quadrance() {
		return x() * x() + y() * y();
	}

	/**
	 * Returns true if this point has the given coordinates.
	 */
	public boolean equals(double x, double y) {
		return this.x() == x && this.y() == y;
	}

	/**
	 * Returns this point as a column vector.
	 */
	public Matrix vector() {
		return Matrix.vector(x(), y());
	}

	@Override
	public String toString() {
		return "(" + x() + ", " + y() + ")";
	}

	private Point2d create(double x, double y) {
		if (equals(x, y)) return this;
		return of(x, y);
	}
}
