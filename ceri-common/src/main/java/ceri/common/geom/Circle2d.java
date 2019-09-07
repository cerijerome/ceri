package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.MathUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Circle2d {
	public static final Circle2d NULL = new Circle2d(0);
	public final double r;

	public static Circle2d of(double r) {
		if (r == 0) return NULL;
		validateMin(r, 0, "Radius");
		return new Circle2d(r + .0);
	}

	private Circle2d(double r) {
		this.r = r;
	}

	/**
	 * Calculates the gradient at x for y >= 0.
	 */
	public double gradientAtX(double x) {
		if (r == 0 || x < -r || x > r) return Double.NaN;
		if (x == -r) return Double.POSITIVE_INFINITY;
		if (x == r) return Double.NEGATIVE_INFINITY;
		if (x == 0) return 0;
		return -x / yFromX(x);
	}

	/**
	 * Calculates the gradient at y for x >= 0.
	 */
	public double gradientAtY(double y) {
		if (r == 0 || y < -r || y > r) return Double.NaN;
		if (y == -r || y == r) return 0;
		if (y == 0) return Double.NEGATIVE_INFINITY;
		return -xFromY(y) / y;
	}

	/**
	 * Returns the coordinates (x, y), y >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (r == 0) return Point2d.ZERO;
		if (m == 0) return Point2d.of(0, r);
		if (m == Double.POSITIVE_INFINITY) return Point2d.of(-r, 0);
		if (m == Double.NEGATIVE_INFINITY) return Point2d.of(r, 0);
		double d = Math.sqrt(1 + (m * m));
		double y = r / d;
		double x = -m * y;
		return Point2d.of(x, y);
	}

	/**
	 * Calculates x >= 0 from given y.
	 */
	public double xFromY(double y) {
		if (y < -r || y > r) return Double.NaN;
		if (y == -r || y == r) return 0;
		if (y == 0) return r;
		return Math.sqrt((r * r) - (y * y));
	}

	/**
	 * Calculates y >= 0 from given x.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	public double yFromX(double x) {
		return xFromY(x);
	}

	/**
	 * Area of the circle.
	 */
	public double area() {
		return area(r);
	}

	/**
	 * Area from x = -r to given x.
	 */
	public double areaToX(double x) {
		if (x <= -r) return 0;
		if (x >= r) return area();
		return integral(x, r) - integral(-r, r);
	}

	/**
	 * Area from y = -r to given y.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	public double areaToY(double y) {
		return areaToX(y);
	}

	/**
	 * Circumference of the circle.
	 */
	public double circumference() {
		return circumference(r);
	}

	/**
	 * Radius of a circle with given given area.
	 */
	public static double radiusFromArea(double a) {
		if (a < 0) return Double.NaN;
		return Math.sqrt(a / Math.PI);
	}

	/**
	 * Area of a circle with given radius.
	 */
	public static double area(double r) {
		return Math.PI * r * r;
	}

	/**
	 * Circumference of a circle with given radius.
	 */
	public static double circumference(double r) {
		return Math.PI * 2 * r;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Circle2d)) return false;
		Circle2d other = (Circle2d) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r).toString();
	}

	private static double integral(double x, double r) {
		x = MathUtil.limit(x, -r, r);
		double sqrt = Math.sqrt((r * r) - (x * x));
		return (x * sqrt) + (r * r * Math.atan(x / sqrt));
	}

}
