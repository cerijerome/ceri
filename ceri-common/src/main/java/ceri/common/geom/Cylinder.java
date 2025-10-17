package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * A cylinder with vertical axis.
 */
public record Cylinder(double r, double h) implements Radial3d {
	public static final Cylinder ZERO = new Cylinder(0.0, 0.0);

	/**
	 * Calculates the volume of a cylinder.
	 */
	public static double volume(double r, double h) {
		return Math.PI * r * r * h;
	}

	/**
	 * Returns a validated instance.
	 */
	public static Cylinder of(double r, double h) {
		if (r == 0.0 && h == 0.0) return ZERO;
		return new Cylinder(r + 0.0, h + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Cylinder {
		Validate.finiteMin(r, 0.0);
		Validate.finiteMin(h, 0.0);
	}
	
	@Override
	public double volume() {
		return volume(r(), h());
	}

	@Override
	public double volumeFromH(double h) {
		if (h <= 0.0) return 0.0;
		if (h >= h()) return volume();
		return volume(r(), h);
	}

	@Override
	public double gradientAtH(double h) {
		if (h < 0.0 || h > h()) return Double.NaN;
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double hFromVolume(double v) {
		if (v == 0.0) return 0.0;
		if (v < 0.0) return Double.NaN;
		double volume = volume();
		if (v > volume) return Double.NaN;
		if (v == volume) return h();
		return Math.min(h(), v / (Math.PI * r() * r()));
	}

	@Override
	public double radiusFromH(double h) {
		if (h < 0.0 || h > h()) return Double.NaN;
		return r();
	}
}
