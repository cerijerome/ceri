package ceri.common.geom;

import java.util.Objects;
import ceri.common.math.ReverseFunction;
import ceri.common.text.ToString;
import ceri.common.util.Validate;

/**
 * A bounded 3d shape matching the hole inside an elliptical torus (doughnut).
 */
public class ConcaveSpheroid implements Radial3d {
	private static final int REVERSE_STEPS_DEF = 200;
	public static final ConcaveSpheroid ZERO = new ConcaveSpheroid(0.0, 0.0, 0.0, 0);
	private final ReverseFunction hFromVolume;
	private final Ellipse ellipse;
	public final double r;
	public final double a;
	public final double c;
	private final double h;
	private final double v0;
	private final double v;

	public static ConcaveSpheroid of(double r, double a, double c) {
		if (r == 0.0 && a == 0.0 && c == 0.0) return ZERO;
		Validate.finiteMin(r, 0.0);
		Validate.range(a, 0.0, r);
		Validate.finiteMin(c, 0.0);
		return new ConcaveSpheroid(r + 0.0, a + 0.0, c + 0.0, REVERSE_STEPS_DEF);
	}

	private ConcaveSpheroid(double r, double a, double c, int steps) {
		this.r = r;
		this.a = a;
		this.c = c;
		ellipse = Ellipse.of(a, c);
		v0 = volumeToZ(c, r, a, c);
		v = v0 * 2;
		h = c * 2;
		hFromVolume = ReverseFunction.from(0, c, steps, z -> volumeToZ(z, r, a, c));
	}

	@Override
	public double gradientAtH(double h) {
		return -ellipse.gradientAtY(h - c);
	}

	@Override
	public double h() {
		return h;
	}

	@Override
	public double radiusFromH(double h) {
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
	public double hFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return h();
		if (v == v0) return c;
		if (v > v0) return c + hFromVolume.x(v - v0);
		return c - hFromVolume.x(v0 - v);
	}

	@Override
	public double volumeFromH(double h) {
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
		return Objects.hash(r, a, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConcaveSpheroid other)) return false;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, r, a, c);
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
