package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipse {
	public static final Ellipse NULL = new Ellipse(0, 0);
	public final double a;
	public final double b;

	public static Ellipse create(double a, double b) {
		if (a == 0 || b == 0) return NULL;
		validateMin(a, 0, "Axis a");
		validateMin(b, 0, "Axis b");
		return new Ellipse(a, b);
	}

	private Ellipse(double a, double b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * Returns the gradient angle at x for y >= 0. Value returned is from -pi/2 to +pi/2.
	 */
	public double angleFromX(double x) {
		if (isNull()) return 0;
		if (x == -a) return Math.PI / 2;
		if (x == a) return -Math.PI / 2;
		double y = yFromX(x);
		if (Double.isNaN(y)) return Double.NaN;
		return Math.atan2(-x * b * b, a * a * y);
	}

	/**
	 * Returns the gradient angle at y for x >= 0. Value returned is from -pi/2 to +pi/2.
	 */
	public double angleFromY(double y) {
		if (isNull()) return 0;
		if (y == 0) return Math.PI / 2;
		double x = xFromY(y);
		if (Double.isNaN(x)) return Double.NaN;
		if (y < 0) return Math.atan2(x * b * b, -a * a * y);
		return Math.atan2(-x * b * b, a * a * y);
	}

	/**
	 * Returns the gradient at x for y >= 0.
	 */
	public double gradientFromX(double x) {
		if (isNull()) return 0;
		if (x == -a) return Double.POSITIVE_INFINITY;
		if (x == a) return Double.NEGATIVE_INFINITY;
		double y = yFromX(x);
		if (Double.isNaN(y)) return Double.NaN;
		return -x * b * b / (a * a * y);
	}

	/**
	 * Returns the gradient at y for x >= 0.
	 */
	public double gradientFromY(double y) {
		if (isNull()) return 0;
		if (y == 0) return Double.POSITIVE_INFINITY;
		double x = xFromY(y);
		if (Double.isNaN(x)) return Double.NaN;
		return -x * b * b / (a * a * y);
	}

	/**
	 * Returns the coordinates (x, y), x >= 0, corresponding to the given gradient.
	 */
	public Point2d pointFromGradient(double m) {
		if (isNull()) return Point2d.ZERO;
		if (m == 0) return new Point2d(0, b);
		if (m == Double.POSITIVE_INFINITY || m == Double.NEGATIVE_INFINITY) return new Point2d(a, 0);
		double d = Math.sqrt((b * b) + (m * m * a * a));
		double x = m * a * a / d;
		double y = -b * b / d;
		if (x >= 0) return new Point2d(x, y);
		return new Point2d(-x, -y);
	}

	/**
	 * Returns positive y value for given x. If x is not within the ellipse NaN is returned.
	 */
	public double yFromX(double x) {
		if (isNull()) return 0;
		if (x < -a || x > a) return Double.NaN;
		if (x == 0) return b;
		if (x == a || x == -a) return 0;
		return Math.sqrt((a * a) - (x * x)) * b / a;
	}

	/**
	 * Returns positive x value for given y. If y is not within the ellipse NaN is returned.
	 */
	public double xFromY(double y) {
		if (isNull()) return 0;
		if (y < -b || y > b) return Double.NaN;
		if (y == 0) return a;
		if (y == b || y == -b) return 0;
		return Math.sqrt((b * b) - (y * y)) * a / b;
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
	 * Returns the area between x0 and x1.
	 */
	public double areaBetweenX(double x0, double x1) {
		return integral(x1, a, b) - integral(x0, a, b);
	}

	/**
	 * Returns the area between y0 and y1.
	 */
	public double areaBetweenY(double y0, double y1) {
		return integral(y1, b, a) - integral(y0, b, a);
	}

	/**
	 * Is this a null ellipse.
	 */
	public boolean isNull() {
		return a == 0;
	}

	/**
	 * Perimeter of the ellipse using Ramanujan's approximation. Not accurate for large a/b and b/a
	 * ratios.
	 */
	public static double perimeter(double a, double b) {
		if (a == 0) return 0;
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
		if (!(obj instanceof Ellipse)) return false;
		Ellipse other = (Ellipse) obj;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, a, b).toString();
	}

	private double integral(double x, double a, double b) {
		if (a == 0) return 0;
		if (x < -a || x > a) return Double.NaN;
		if (x == -a) return -area(a, b) / 2;
		if (x == a) return area(a, b) / 2;
		double sqrt = Math.sqrt((a * a) - (x * x));
		return (b * x * sqrt / a) + (b * a * Math.atan(x / sqrt));
	}

}
