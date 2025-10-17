package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * Represents a cone with its apex at h = 0.
 */
public record Cone(double r, double h) implements Radial3d {
	public static final Cone ZERO = new Cone(0.0, 0.0);

	/**
	 * Calculates the volume of a cone.
	 */
	public static double volume(double r, double h) {
		return Math.PI * r * r * h / 3;
	}

	/**
	 * Returns a validated instance.
	 */
	public static Cone of(double r, double h) {
		if (r == 0.0 && h == 0.0) return ZERO;
		return new Cone(r + 0.0, h + 0.0);
	}

	/**
	 * constructor validation.
	 */
	public Cone {
		Validate.finiteMin(r, 0.0);
		Validate.finiteMin(h, 0.0);
	}
	
	/**
	 * Gradient of the cone.
	 */
	public double gradient() {
		return h() / r();
	}

	@Override
	public double gradientAtH(double h) {
		if (h < 0 || h > h()) return Double.NaN;
		return gradient();
	}

	@Override
	public double hFromVolume(double v) {
		if (v == 0.0) return 0.0;
		if (v < 0.0) return Double.NaN;
		var volume = volume();
		if (v > volume) return Double.NaN;
		if (v == volume) return h();
		return Math.cbrt(3.0 * h() * h() * v / (Math.PI * r() * r()));
	}

	@Override
	public double volumeFromH(double h) {
		if (h <= 0.0) return 0.0;
		if (h >= h()) return volume();
		return volume(radiusFromH(h), h);
	}

	@Override
	public double radiusFromH(double h) {
		if (h < 0.0 || h > h()) return Double.NaN;
		if (h == 0.0) return 0.0;
		if (h == h()) return r();
		return r() * h / h();
	}

	public double heightFromRadius(double r) {
		if (r < 0.0 || r > r()) return Double.NaN;
		if (r == 0.0) return 0.0;
		if (r == r()) return h();
		return h() * r / r();
	}

	@Override
	public double volume() {
		return volume(r(), h());
	}
}
