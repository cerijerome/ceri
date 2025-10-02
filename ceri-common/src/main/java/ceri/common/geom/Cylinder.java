package ceri.common.geom;

import ceri.common.util.Validate;

public record Cylinder(double radius, double height) implements Radial3d {
	public static final Cylinder NULL = new Cylinder(0.0, 0.0);

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
		Validate.finiteMin(r, 0.0);
		Validate.finiteMin(h, 0.0);
		if (r == 0.0 && h == 0.0) return NULL;
		return new Cylinder(r + 0.0, h + 0.0);
	}

	@Override
	public double volume() {
		return volume(radius(), height());
	}

	@Override
	public double volumeFromHeight(double h) {
		if (h <= 0.0) return 0.0;
		if (h >= height()) return volume();
		return volume(radius(), h);
	}

	@Override
	public double gradientAtHeight(double h) {
		if (h < 0.0 || h > height()) return Double.NaN;
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double heightFromVolume(double v) {
		if (v == 0.0) return 0.0;
		if (v < 0.0) return Double.NaN;
		double volume = volume();
		if (v > volume) return Double.NaN;
		if (v == volume) return height();
		return Math.min(height(), v / (Math.PI * radius() * radius()));
	}

	@Override
	public double radiusFromHeight(double h) {
		if (h < 0.0 || h > height()) return Double.NaN;
		return radius();
	}
}
