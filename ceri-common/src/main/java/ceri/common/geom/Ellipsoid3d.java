package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.AlgebraUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipsoid3d {
	public static final Ellipsoid3d NULL = new Ellipsoid3d(0, 0, 0);
	public final double a;
	public final double b;
	public final double c;
	private final double v;

	public static Ellipsoid3d create(double a, double b, double c) {
		if (a == 0 && b == 0 && c == 0) return NULL;
		validateMin(a, 0, "Axis a");
		validateMin(b, 0, "Axis b");
		validateMin(c, 0, "Axis c");
		return new Ellipsoid3d(a, b, c);
	}

	protected Ellipsoid3d(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
		v = volume(a, b, c);
	}

	/**
	 * Returns x given a volume from x = -a with plane perpendicular to the x-axis.
	 */
	public double xFromVolume(double v) {
		return xFromVolume(v, a, b, c);
	}

	/**
	 * Returns y given a volume from y = -b with plane perpendicular to the y-axis.
	 */
	public double yFromVolume(double v) {
		return xFromVolume(v, b, c, a);
	}

	/**
	 * Returns z given a volume from z = -c with plane perpendicular to the z-axis.
	 */
	public double zFromVolume(double v) {
		return xFromVolume(v, c, a, b);
	}

	/**
	 * Returns the volume between planes perpendicular to the x-axis at -a to x.
	 */
	public double volumeToX(double x) {
		return volumeToX(x, a, b, c);
	}

	/**
	 * Returns the volume between planes perpendicular to the y-axis at -b and y.
	 */
	public double volumeToY(double y) {
		return volumeToX(y, b, c, a);
	}

	/**
	 * Returns the volume between planes perpendicular to the z-axis at -c and z.
	 */
	public double volumeToZ(double z) {
		return volumeToX(z, c, a, b);
	}

	/**
	 * Volume of this ellipsoid.
	 */
	public double volume() {
		return v;
	}

	/**
	 * Volume of an ellipsoid with given axes.
	 */
	public static double volume(double a, double b, double c) {
		return 4.0 * Math.PI * a * b * c / 3.0;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ellipsoid3d)) return false;
		Ellipsoid3d other = (Ellipsoid3d) obj;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, a, b, c).toString();
	}

	public double volumeToX(double x, double a, double b, double c) {
		if (x <= -a) return 0;
		if (x >= a) return volume();
		return integral(x, a, b, c) - integral(-a, a, b, c);
	}

	private double xFromVolume(double v, double a, double b, double c) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return -a;
		if (v == this.v) return a;
		return root(v, a, b, c);
	}

	/**
	 * Integral from volume formula from -a to x:
	 * 
	 * <pre>
	 * (pi.b.c / 3.a^2)[x(3.a^2 - x^2)]
	 * </pre>
	 */
	private double integral(double x, double a, double b, double c) {
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