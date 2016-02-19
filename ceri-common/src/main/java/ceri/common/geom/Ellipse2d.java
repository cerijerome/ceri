package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.MathUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipse2d {
	public static final Ellipse2d NULL = new Ellipse2d(0, 0);
	public final double a;
	public final double b;

	public static Ellipse2d create(double a, double b) {
		if (a == 0 && b == 0) return NULL;
		validateMin(a, 0, "Axis a");
		validateMin(b, 0, "Axis b");
		return new Ellipse2d(a, b);
	}

	private Ellipse2d(double a, double b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * Returns the gradient at x for y >= 0.
	 */
	public double gradientAtX(double x) {
		if ((a == 0 && b == 0) || x < -a || x > a) return Double.NaN;
		if (x == a) return Double.NEGATIVE_INFINITY;
		if (x == -a) return Double.POSITIVE_INFINITY;
		if (x == 0 || b == 0) return 0;
		double y = yFromX(x);
		return -x * b * b / (a * a * y);
	}

	/**
	 * Returns the gradient at y for x >= 0.
	 */
	public double gradientAtY(double y) {
		if ((a == 0 && b == 0) || y < -b || y > b) return Double.NaN;
		if (y == -b || y == b) return 0;
		if (y == 0 || a == 0) return Double.NEGATIVE_INFINITY;
		double x = xFromY(y);
		return -x * b * b / (a * a * y);
	}

	/**
	 * Returns the coordinates (x, y), y >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (a == 0 && b == 0) return Point2d.ZERO;
		if (m == 0) return new Point2d(0, b);
		if (m == Double.POSITIVE_INFINITY) return new Point2d(-a, 0);
		if (m == Double.NEGATIVE_INFINITY) return new Point2d(a, 0);
		double d = Math.sqrt((b * b) + (m * m * a * a));
		double x = -m * a * a / d;
		double y = b * b / d;
		return new Point2d(x, y);
	}

	/**
	 * Returns positive x value for given y. If y is not within the ellipse NaN is returned.
	 */
	public double xFromY(double y) {
		if (y < -b || y > b) return Double.NaN;
		if (y == -b || y == b) return 0;
		if (y == 0) return a;
		return Math.sqrt((b * b) - (y * y)) * a / b;
	}

	/**
	 * Returns positive y value for given x. If x is not within the ellipse NaN is returned.
	 */
	public double yFromX(double x) {
		if (x < -a || x > a) return Double.NaN;
		if (x == a || x == -a) return 0;
		if (x == 0) return b;
		return Math.sqrt((a * a) - (x * x)) * b / a;
	}

	/**
	 * Approximation of the perimeter of the ellipse.
	 */
	public double perimeter() {
		return perimeter(a, b);
	}

	/**
	 * Returns the area of the ellipse.
	 */
	public double area() {
		return area(a, b);
	}

	/**
	 * Area from x = -a to given x.
	 */
	public double areaToX(double x) {
		if (x <= -a) return 0;
		if (x >= a) return area();
		return integral(x, a, b) - integral(-a, a, b);
	}

	/**
	 * Area from y = -b to given y.
	 */
	public double areaToY(double y) {
		if (y <= -b) return 0;
		if (y >= b) return area();
		return integral(y, b, a) - integral(-b, b, a);
	}

	/**
	 * Perimeter of the ellipse using Ramanujan's approximation. Not accurate for large a/b and b/a
	 * ratios.
	 */
	public static double perimeter(double a, double b) {
		if (a == 0) return b * 4;
		if (b == 0) return a * 4;
		double h = (a - b) / (a + b);
		h *= h;
		return Math.PI * (a + b) * (1 + (3 * h / (10 + Math.sqrt(4 - (3 * h)))));
	}

	/**
	 * Returns the area of an ellipse.
	 */
	public static double area(double a, double b) {
		return Math.PI * a * b;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(a, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ellipse2d)) return false;
		Ellipse2d other = (Ellipse2d) obj;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, a, b).toString();
	}

	private static double integral(double x, double a, double b) {
		if (a == 0 || b == 0) return 0;
		x = MathUtil.limit(x, -a, a);
		double sqrt = Math.sqrt((a * a) - (x * x));
		return (b * x * sqrt / a) + (b * a * Math.atan(x / sqrt));
	}

}
