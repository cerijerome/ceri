package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipse {
	public final double a;
	public final double b;

	public Ellipse(double a, double b) {
		if (a <= 0) throw new IllegalArgumentException("Axis a must be > 0: " + a);
		if (b <= 0) throw new IllegalArgumentException("Axis b must be > 0: " + b);
		validateMin(b, 0, "Axis b");
		this.a = a;
		this.b = b;
	}

	/**
	 * Returns positive y value for given x.
	 * If x is not within the ellipse NaN is returned.
	 */
	public double yFromX(double x) {
		if (x < -a || x > a) return Double.NaN;
		return Math.sqrt((a * a) - (x * x)) * b / a;
	}
	
	/**
	 * Returns positive x value for given y.
	 * If y is not within the ellipse NaN is returned.
	 */
	public double xFromY(double y) {
		if (y < -b || y > b) return Double.NaN;
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
	 * Approximation of the perimeter of the ellipse. Not accurate for large a/b and b/a ratios.
	 */
	public static double perimeter(double a, double b) {
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
		if (x < -a || x > a) return Double.NaN;
		if (x == -a) return -area(a, b) / 2;
		if (x == a) return area(a, b) / 2;
		double sqrt = Math.sqrt((a * a) - (x * x));
		return (b * x * sqrt / a) + (b * a * Math.atan(x / sqrt));
	 }
	 
}
