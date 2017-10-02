package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import ceri.common.math.ReverseFunction;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * The hole inside an elliptical torus.
 */
public class ConcaveSpheroid3d implements Radial3d {
	private static final int REVERSE_STEPS_DEF = 200;
	public static final ConcaveSpheroid3d NULL = new ConcaveSpheroid3d(0, 0, 0, 0);
	private final ReverseFunction hFromVolume;
	private final Ellipse2d ellipse;
	public final double r;
	public final double a;
	public final double c;
	private final double h;
	private final double v0;
	private final double v;

	public static ConcaveSpheroid3d create(double r, double a, double c) {
		if (r == 0 && a == 0 && c == 0) return NULL;
		validateMin(r, 0, "Radius");
		validateRange(a, 0, r, "Axis a");
		validateMin(c, 0, "Axis c");
		return new ConcaveSpheroid3d(r, a, c, REVERSE_STEPS_DEF);
	}

	private ConcaveSpheroid3d(double r, double a, double c, int steps) {
		this.r = r;
		this.a = a;
		this.c = c;
		ellipse = Ellipse2d.create(a, c);
		v0 = volumeToZ(c, r, a, c);
		v = v0 * 2;
		h = c * 2;
		hFromVolume = ReverseFunction.create(0, c, steps, z -> volumeToZ(z, r, a, c));
	}

	@Override
	public double gradientAtHeight(double h) {
		return -ellipse.gradientAtY(h - c);
	}

	@Override
	public double height() {
		return h;
	}

	@Override
	public double radiusFromHeight(double h) {
		return r - ellipse.xFromY(h - c);
	}

	@Override
	public double volume() {
		return v;
	}

	/**
	 * Approximate height from volume using reverse lookup function.
	 */
	@Override
	public double heightFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return height();
		if (v == v0) return c;
		if (v > v0) return c + hFromVolume.x(v - v0);
		return c - hFromVolume.x(v0 - v);
	}

	@Override
	public double volumeFromHeight(double h) {
		if (h <= 0) return 0;
		if (h >= this.h) return this.v;
		if (h == c) return v0;
		if (h > c) return v0 + volumeToZ(h - c, r, a, c);
		return v0 - volumeToZ(c - h, r, a, c);
	}

	/**
	 * Calculates the volume of a concave spheroid.
	 */
	public static double volume(double r, double a, double c) {
		return volumeToZ(c, r, a, c) * 2;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r, a, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConcaveSpheroid3d)) return false;
		ConcaveSpheroid3d other = (ConcaveSpheroid3d) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		if (!EqualsUtil.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, a, c).toString();
	}

	/**
	 * Calculates the volume for 0 <= z <= c.
	 */
	private static double volumeToZ(double z, double r, double a, double c) {
		if (a == 0 || c == 0) return 0;
		double root = Math.sqrt((c * c) - (z * z));
		double t0 = (-a * a * z * z * z) / (3.0 * c * c);
		double t1 = ((r * r) + (a * a)) * z;
		double t2 = -a * r * z * root / c;
		double t3 = -a * c * r * Math.asin(z / c);
		return Math.PI * (t0 + t1 + t2 + t3);
	}

}
