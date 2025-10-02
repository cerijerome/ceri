package ceri.common.geom;

import ceri.common.math.Maths;
import ceri.common.util.Validate;

public record Ellipse(double axisX, double axisY) {
	public static final Ellipse NULL = new Ellipse(0.0, 0.0);

	/**
	 * Calculates the perimeter of the ellipse using Ramanujan's approximation; not accurate for
	 * large a/b and b/a ratios.
	 */
	public static double perimeter(double axisX, double axisY) {
		if (axisX == 0.0) return axisY * 4;
		if (axisY == 0.0) return axisX * 4;
		double h = (axisX - axisY) / (axisX + axisY);
		h *= h;
		return Math.PI * (axisX + axisY) * (1.0 + (3.0 * h / (10.0 + Math.sqrt(4.0 - (3.0 * h)))));
	}

	/**
	 * Returns the area of an ellipse.
	 */
	public static double area(double axisX, double axisY) {
		return Math.PI * axisX * axisY;
	}

	/**
	 * Returns a validated instance.
	 */
	public static Ellipse of(double axisX, double axisY) {
		Validate.finiteMin(axisX, 0.0);
		Validate.finiteMin(axisY, 0.0);
		if (axisX == 0.0 && axisY == 0.0) return NULL;
		return new Ellipse(axisX + 0.0, axisY + 0.0);
	}

	/**
	 * Returns the gradient at x for y >= 0.
	 */
	public double gradientAtX(double x) {
		if ((axisX() == 0.0 && axisY() == 0.0) || x < -axisX() || x > axisX()) return Double.NaN;
		if (x == axisX()) return Double.NEGATIVE_INFINITY;
		if (x == -axisX()) return Double.POSITIVE_INFINITY;
		if (x == 0.0 || axisY() == 0.0) return 0.0;
		double y = yFromX(x);
		return -x * axisY() * axisY() / (axisX() * axisX() * y);
	}

	/**
	 * Returns the gradient at y for x >= 0.
	 */
	public double gradientAtY(double y) {
		if ((axisX() == 0.0 && axisY() == 0.0) || y < -axisY() || y > axisY()) return Double.NaN;
		if (y == -axisY() || y == axisY()) return 0.0;
		if (y == 0.0 || axisX() == 0.0) return Double.NEGATIVE_INFINITY;
		double x = xFromY(y);
		return -x * axisY() * axisY() / (axisX() * axisX() * y);
	}

	/**
	 * Returns the coordinates (x, y), y >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (axisX() == 0.0 && axisY() == 0.0) return Point2d.ZERO;
		if (m == 0.0) return Point2d.of(0.0, axisY());
		if (m == Double.POSITIVE_INFINITY) return Point2d.of(-axisX(), 0.0);
		if (m == Double.NEGATIVE_INFINITY) return Point2d.of(axisX(), 0.0);
		double d = Math.sqrt((axisY() * axisY()) + (m * m * axisX() * axisX()));
		double x = -m * axisX() * axisX() / d;
		double y = axisY() * axisY() / d;
		return Point2d.of(x, y);
	}

	/**
	 * Returns positive x value for given y. If y is not within the ellipse NaN is returned.
	 */
	public double xFromY(double y) {
		if (y < -axisY() || y > axisY()) return Double.NaN;
		if (y == -axisY() || y == axisY()) return 0.0;
		if (y == 0.0) return axisX();
		return Math.sqrt((axisY() * axisY()) - (y * y)) * axisX() / axisY();
	}

	/**
	 * Returns positive y value for given x. If x is not within the ellipse NaN is returned.
	 */
	public double yFromX(double x) {
		if (x < -axisX() || x > axisX()) return Double.NaN;
		if (x == axisX() || x == -axisX()) return 0.0;
		if (x == 0.0) return axisY();
		return Math.sqrt((axisX() * axisX()) - (x * x)) * axisY() / axisX();
	}

	/**
	 * Approximation of the perimeter of the ellipse.
	 */
	public double perimeter() {
		return perimeter(axisX(), axisY());
	}

	/**
	 * Calculates the area of the ellipse.
	 */
	public double area() {
		return area(axisX(), axisY());
	}

	/**
	 * Calculates the area from x = -a to the given x.
	 */
	public double areaToX(double x) {
		if (x <= -axisX()) return 0.0;
		if (x >= axisX()) return area();
		return integral(x, axisX(), axisY()) - integral(-axisX(), axisX(), axisY());
	}

	/**
	 * Calculates the area from y = -b to the given y.
	 */
	public double areaToY(double y) {
		if (y <= -axisY()) return 0.0;
		if (y >= axisY()) return area();
		return integral(y, axisY(), axisX()) - integral(-axisY(), axisY(), axisX());
	}

	private static double integral(double r, double a, double b) {
		if (a == 0.0 || b == 0.0) return 0.0;
		r = Maths.limit(r, -a, a);
		double sqrt = Math.sqrt((a * a) - (r * r));
		return (b * r * sqrt / a) + (b * a * Math.atan(r / sqrt));
	}
}
