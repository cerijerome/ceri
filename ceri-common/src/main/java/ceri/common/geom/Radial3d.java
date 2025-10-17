package ceri.common.geom;

/**
 * A 3d shape in which every cross-section perpendicular to the vertical axis is a circle.
 */
public interface Radial3d {

	/**
	 * Returns the height of the shape.
	 */
	double h();

	/**
	 * Calculates the volume from the height within the shape.
	 */
	double volumeFromH(double h);

	/**
	 * Calculates the height within the shape that matches the given volume.
	 */
	double hFromVolume(double v);

	/**
	 * Calculates the radius from the given height within the shape.
	 */
	double radiusFromH(double h);

	/**
	 * Calculates the gradient at the given height within the shape.
	 */
	double gradientAtH(double h);

	/**
	 * Calculates the volume of the shape.
	 */
	default double volume() {
		return volumeFromH(h());
	}

	/**
	 * Constrains the height within the shape limits.
	 */
	default double constrainH(double h) {
		if (h <= 0.0) return 0.0;
		return Math.min(h, h());
	}

	/**
	 * Constrains the volume within the shape limits.
	 */
	default double constrainVolume(double v) {
		if (v <= 0.0) return 0.0;
		return Math.min(v, volume());
	}
}
