package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.AlgebraUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipsoid {
	public static final Ellipsoid NULL = new Ellipsoid(0, 0, 0);
	public final double a;
	public final double b;
	public final double c;

	public static Ellipsoid create(double a, double b, double c) {
		if (a == 0 || b == 0 || c == 0) return NULL;
		validateMin(a, 0, "Axis a");
		validateMin(b, 0, "Axis b");
		validateMin(c, 0, "Axis c");
		return new Ellipsoid(a, b, c);
	}

	private Ellipsoid(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	/**
	 * Returns x given a volume from x = -a with plane perpendicular to the x-axis.
	 */
	public double xFromVolume(double volume) {
		return root(volume, a, b, c);
	}

	/**
	 * Returns y given a volume from y = -b with plane perpendicular to the y-axis.
	 */
	public double yFromVolume(double volume) {
		return root(volume, b, c, a);
	}

	/**
	 * Returns z given a volume from z = -c with plane perpendicular to the z-axis.
	 */
	public double zFromVolume(double volume) {
		return root(volume, c, a, b);
	}

	/**
	 * Returns the volume between planes perpendicular to the x-axis at x0 and x1.
	 */
	public double volumeBetweenX(double x0, double x1) {
		return integral(x1, a, b, c) - integral(x0, a, b, c);
	}

	/**
	 * Returns the volume between planes perpendicular to the y-axis at y0 and y1.
	 */
	public double volumeBetweenY(double y0, double y1) {
		return integral(y1, b, a, c) - integral(y0, b, a, c);
	}

	/**
	 * Returns the volume between planes perpendicular to the z-axis at z0 and z1.
	 */
	public double volumeBetweenZ(double z0, double z1) {
		return integral(z1, c, a, b) - integral(z0, c, a, b);
	}

	/**
	 * Volume of this ellipsoid.
	 */
	public double volume() {
		return volume(a, b, c);
	}

	/**
	 * Volume of an ellipsoid with given axes.
	 */
	public static double volume(double a, double b, double c) {
		return 4.0 * Math.PI * a * b * c / 3.0;
	}

	/**
	 * Is this a null ellipsoid?
	 */
	public boolean isNull() {
		return a == 0;
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ellipsoid)) return false;
		Ellipsoid other = (Ellipsoid) obj;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, a, b, c).toString();
	}

	/**
	 * Integral from volume formula from -a to x:
	 * 
	 * <pre>
	 * (pi.b.c / 3.a^2)[x(3.a^2 - x^2)]
	 * </pre>
	 */
	private double integral(double x, double a, double b, double c) {
		if (a == 0) return 0;
		if (Double.isNaN(x)) return Double.NaN;
		if (x < -a || x > a) return Double.NaN;
		return Math.PI * b * c * x * ((3 * a * a) - (x * x)) / (3 * a * a);
	}

	/**
	 * Solution of volume formula where x is known:
	 * 
	 * <pre>
	 * x^3 - 3.a^2.x + (3.a^2.v / pi.b.c) - 2.a^3
	 * </pre>
	 */
	private double root(double v, double a, double b, double c) {
		if (a == 0) return 0;
		if (Double.isNaN(v)) return Double.NaN;
		double a1 = -3.0 * a * a;
		double a0 = (3.0 * a * a * v / (Math.PI * b * c)) - (2 * a * a * a);
		return validRoot(AlgebraUtil.cubicRealRoots(0, a1, a0), a);
	}

	/**
	 * Finds the first cubic root that is within range.
	 */
	private double validRoot(double[] roots, double max) {
		for (double root : roots)
			if (root >= -max && root <= max) return root;
		return Double.NaN;
	}

}
