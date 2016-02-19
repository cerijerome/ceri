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
public class ConcaveSpheroid3d {
	public static final ConcaveSpheroid3d NULL = new ConcaveSpheroid3d(0, 0, 0, 0);
	private static final int REVERSE_STEPS_DEF = 200;
	private final ReverseFunction hFromVolume;
	public final double r;
	public final double a;
	public final double c;
	private final double v;

	public static ConcaveSpheroid3d create(double r, double a, double c) {
		if (r == 0 || a == 0 || c == 0) return NULL;
		validateMin(r, 0, "Radius");
		validateRange(a, 0, r, "Axis a");
		validateMin(c, 0, "Axis c");
		return new ConcaveSpheroid3d(r, a, c, REVERSE_STEPS_DEF);
	}

	private ConcaveSpheroid3d(double r, double a, double c, int steps) {
		this.r = r;
		this.a = a;
		this.c = c;
		v = volume(r, a, c) / 2;
		hFromVolume = ReverseFunction.create(0, c, steps, h -> volumeFromH(h, r, a, c));
	}

	/**
	 * Gradient at height h, with -c <= h <= c.
	 */
	public double gradientFromH(double h) {
		if (isNull()) return Double.NaN;
		if (h < -c || h > c) return Double.NaN;
		if (h == 0) return Double.POSITIVE_INFINITY;
		return c * Math.sqrt((c * c) - (h * h)) / (a * h);
	}
	
	/**
	 * Returns the radius at height h, with -c <= h <= c.
	 */
	public double rFromH(double h) {
		if (isNull()) return Double.NaN;
		if (h < -c || h > c) return Double.NaN;
		if (h == -c || h == c) return r;
		return r - (a * Math.sqrt((c * c) - (h * h)) / c);
	}
	
	/**
	 * Calculates the volume.
	 */
	public double volume() {
		if (isNull()) return 0;
		return volume(r, a, c);
	}

	/**
	 * Approximate height from volume.
	 */
	public double hFromVolume(double v) {
		if (isNull()) return 0;
		if (v == this.v) return 0;
		if (v == 0) return -c;
		if (v == this.v * 2) return c;
		if (v > this.v) return hFromVolume.x(v - this.v);
		return -hFromVolume.x(this.v - v);
	}

	/**
	 * Calculates volume from height h, between -c and +c.
	 */
	public double volumeFromH(double h) {
		if (isNull()) return 0;
		if (h >= 0) return v + volumeFromH(h, r, a, c);
		return v - volumeFromH(-h, r, a, c);
	}

	/**
	 * Is this a null shape?
	 */
	public boolean isNull() {
		return r == 0;
	}
	
	/**
	 * Calculates the volume.
	 */
	public static double volume(double r, double a, double c) {
		return volumeFromH(c, r, a, c) * 2;
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
	 * Calculates the volume for 0 <= h <= c.
	 */
	private static double volumeFromH(double h, double r, double a, double c) {
		double root = Math.sqrt((c * c) - (h * h));
		double t0 = (-a * a * h * h * h) / (3.0 * c * c);
		double t1 = ((r * r) + (a * a)) * h;
		double t2 = -a * r * h * root / c;
		double t3 = -a * c * r * Math.asin(h / c);
		return Math.PI * (t0 + t1 + t2 + t3);
	}

}
