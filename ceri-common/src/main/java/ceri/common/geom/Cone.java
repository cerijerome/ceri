package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * Represents a cone with its apex at h = 0.
 */
public record Cone(double radius, double height) implements Radial3d {
	public static final Cone NULL = new Cone(0.0, 0.0);

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
		Validate.finiteMin(r, 0.0);
		Validate.finiteMin(h, 0.0);
		if (r == 0.0 && h == 0.0) return NULL;
		return new Cone(r + 0.0, h + 0.0);
	}

	/**
	 * Gradient of the cone.
	 */
	public double gradient() {
		return height() / radius();
	}

	@Override
	public double gradientAtHeight(double h) {
		if (h < 0 || h > height()) return Double.NaN;
		return gradient();
	}

	@Override
	public double heightFromVolume(double v) {
		if (v == 0.0) return 0.0;
		if (v < 0.0) return Double.NaN;
		var volume = volume();
		if (v > volume) return Double.NaN;
		if (v == volume) return height();
		return Math.cbrt(3.0 * height() * height() * v / (Math.PI * radius() * radius()));
	}

	@Override
	public double volumeFromHeight(double h) {
		if (h <= 0.0) return 0.0;
		if (h >= height()) return volume();
		return volume(radiusFromHeight(h), h);
	}

	@Override
	public double radiusFromHeight(double h) {
		if (h < 0.0 || h > height()) return Double.NaN;
		if (h == 0.0) return 0.0;
		if (h == height()) return radius();
		return radius() * h / height();
	}

	public double heightFromRadius(double r) {
		if (r < 0.0 || r > radius()) return Double.NaN;
		if (r == 0.0) return 0.0;
		if (r == radius()) return height();
		return height() * r / radius();
	}

	@Override
	public double volume() {
		return volume(radius(), height());
	}
}
