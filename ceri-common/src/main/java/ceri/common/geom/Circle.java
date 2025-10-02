package ceri.common.geom;

import ceri.common.math.Maths;
import ceri.common.util.Validate;

/**
 * Represents a circle 2d shape with radius.
 */
public record Circle(double radius) {
	public static final Circle NULL = new Circle(0.0);

	/**
	 * Calculates the radius for a circle of given area.
	 */
	public static double radiusFromArea(double a) {
		if (a < 0.0) return Double.NaN;
		return Math.sqrt(a / Math.PI);
	}

	/**
	 * Calculates the area for a circle of given radius.
	 */
	public static double area(double r) {
		return Math.PI * r * r;
	}

	/**
	 * Calculates the circumference for a circle of given radius.
	 */
	public static double circumference(double r) {
		return Math.PI * 2 * r;
	}

	/**
	 * Returns a validated instance.
	 */
	public static Circle of(double r) {
		Validate.finiteMin(r, 0.0);
		return new Circle(r + 0.0);
	}

	/**
	 * Returns true if the circle has no radius.
	 */
	public boolean isNull() {
		return radius() <= 0.0;
	}

	/**
	 * Calculates the gradient at x for y >= 0.
	 */
	public double gradientAtX(double x) {
		if (isNull() || x < -radius() || x > radius()) return Double.NaN;
		if (x == -radius()) return Double.POSITIVE_INFINITY;
		if (x == radius()) return Double.NEGATIVE_INFINITY;
		if (x == 0.0) return 0.0;
		return -x / yFromX(x);
	}

	/**
	 * Calculates the gradient at y for x >= 0.
	 */
	public double gradientAtY(double y) {
		if (isNull() || y < -radius() || y > radius()) return Double.NaN;
		if (y == -radius() || y == radius()) return 0.0;
		if (y == 0.0) return Double.NEGATIVE_INFINITY;
		return -xFromY(y) / y;
	}

	/**
	 * Returns the coordinates (x, y), y >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (isNull()) return Point2d.ZERO;
		if (m == 0.0) return Point2d.of(0.0, radius);
		if (m == Double.POSITIVE_INFINITY) return Point2d.of(-radius(), 0.0);
		if (m == Double.NEGATIVE_INFINITY) return Point2d.of(radius(), 0.0);
		double d = Math.sqrt(1 + (m * m));
		double y = radius() / d;
		double x = -m * y;
		return Point2d.of(x, y);
	}

	/**
	 * Calculates x >= 0 from given y.
	 */
	public double xFromY(double y) {
		if (y < -radius() || y > radius()) return Double.NaN;
		if (y == -radius() || y == radius()) return 0.0;
		if (y == 0.0) return radius();
		return Math.sqrt((radius() * radius()) - (y * y));
	}

	/**
	 * Calculates y >= 0 from given x.
	 */
	public double yFromX(double x) {
		return xFromY(x);
	}

	/**
	 * Area of the circle.
	 */
	public double area() {
		return area(radius());
	}

	/**
	 * Area from x = -r to given x.
	 */
	public double areaToX(double x) {
		if (x <= -radius()) return 0.0;
		if (x >= radius()) return area();
		return integral(x, radius()) - integral(-radius(), radius());
	}

	/**
	 * Area from y = -r to given y.
	 */
	public double areaToY(double y) {
		return areaToX(y);
	}

	/**
	 * Circumference of the circle.
	 */
	public double circumference() {
		return circumference(radius());
	}

	private static double integral(double x, double r) {
		x = Maths.limit(x, -r, r);
		double sqrt = Math.sqrt((r * r) - (x * x));
		return (x * sqrt) + (r * r * Math.atan(x / sqrt));
	}
}
