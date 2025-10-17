package ceri.common.geom;

import ceri.common.math.Maths;
import ceri.common.util.Validate;

public record Ellipse(double a, double b) {
	public static final Ellipse ZERO = new Ellipse(0.0, 0.0);

	/**
	 * Calculates the perimeter of the ellipse using Ramanujan's approximation; not accurate for
	 * large a/b and b/a ratios.
	 */
	public static double perimeter(double a, double b) {
		if (a == 0.0) return b * 4;
		if (b == 0.0) return a * 4;
		double h = (a - b) / (a + b);
		h *= h;
		return Math.PI * (a + b) * (1.0 + (3.0 * h / (10.0 + Math.sqrt(4.0 - (3.0 * h)))));
	}

	/**
	 * Returns the area of an ellipse.
	 */
	public static double area(double a, double b) {
		return Math.PI * a * b;
	}

	/**
	 * Returns a validated instance.
	 */
	public static Ellipse of(double a, double b) {
		if (a == 0.0 && b == 0.0) return ZERO;
		return new Ellipse(a + 0.0, b + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Ellipse {
		Validate.finiteMin(a, 0.0);
		Validate.finiteMin(b, 0.0);
	}
	
	/**
	 * Returns the gradient at x for y >= 0.
	 */
	public double gradientAtX(double x) {
		if ((a() == 0.0 && b() == 0.0) || x < -a() || x > a()) return Double.NaN;
		if (x == a()) return Double.NEGATIVE_INFINITY;
		if (x == -a()) return Double.POSITIVE_INFINITY;
		if (x == 0.0 || b() == 0.0) return 0.0;
		double y = yFromX(x);
		return -x * b() * b() / (a() * a() * y);
	}

	/**
	 * Returns the gradient at y for x >= 0.
	 */
	public double gradientAtY(double y) {
		if ((a() == 0.0 && b() == 0.0) || y < -b() || y > b()) return Double.NaN;
		if (y == -b() || y == b()) return 0.0;
		if (y == 0.0 || a() == 0.0) return Double.NEGATIVE_INFINITY;
		double x = xFromY(y);
		return -x * b() * b() / (a() * a() * y);
	}

	/**
	 * Returns the coordinates (x, y), y >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (a() == 0.0 && b() == 0.0) return Point2d.ZERO;
		if (m == 0.0) return Point2d.of(0.0, b());
		if (m == Double.POSITIVE_INFINITY) return Point2d.of(-a(), 0.0);
		if (m == Double.NEGATIVE_INFINITY) return Point2d.of(a(), 0.0);
		double d = Math.sqrt((b() * b()) + (m * m * a() * a()));
		double x = -m * a() * a() / d;
		double y = b() * b() / d;
		return Point2d.of(x, y);
	}

	/**
	 * Returns positive x value for given y. If y is not within the ellipse NaN is returned.
	 */
	public double xFromY(double y) {
		if (y < -b() || y > b()) return Double.NaN;
		if (y == -b() || y == b()) return 0.0;
		if (y == 0.0) return a();
		return Math.sqrt((b() * b()) - (y * y)) * a() / b();
	}

	/**
	 * Returns positive y value for given x. If x is not within the ellipse NaN is returned.
	 */
	public double yFromX(double x) {
		if (x < -a() || x > a()) return Double.NaN;
		if (x == a() || x == -a()) return 0.0;
		if (x == 0.0) return b();
		return Math.sqrt((a() * a()) - (x * x)) * b() / a();
	}

	/**
	 * Approximation of the perimeter of the ellipse.
	 */
	public double perimeter() {
		return perimeter(a(), b());
	}

	/**
	 * Calculates the area of the ellipse.
	 */
	public double area() {
		return area(a(), b());
	}

	/**
	 * Calculates the area from x = -a to the given x.
	 */
	public double areaToX(double x) {
		if (x <= -a()) return 0.0;
		if (x >= a()) return area();
		return integral(x, a(), b()) - integral(-a(), a(), b());
	}

	/**
	 * Calculates the area from y = -b to the given y.
	 */
	public double areaToY(double y) {
		if (y <= -b()) return 0.0;
		if (y >= b()) return area();
		return integral(y, b(), a()) - integral(-b(), b(), a());
	}

	private static double integral(double r, double a, double b) {
		if (a == 0.0 || b == 0.0) return 0.0;
		r = Maths.limit(r, -a, a);
		double sqrt = Math.sqrt((a * a) - (r * r));
		return (b * r * sqrt / a) + (b * a * Math.atan(r / sqrt));
	}
}
