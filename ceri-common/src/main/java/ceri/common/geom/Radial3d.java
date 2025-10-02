package ceri.common.geom;

/**
 * A 3d shape in which every cross-section perpendicular to the vertical axis is a circle.
 */
public interface Radial3d {

	/**
	 * Returns the height of the shape.
	 */
	double height();

	/**
	 * Calculates the volume from the height within the shape.
	 */
	double volumeFromHeight(double h);

	/**
	 * Calculates the height within the shape that matches the given volume.
	 */
	double heightFromVolume(double v);

	/**
	 * Calculates the radius from the given height within the shape.
	 */
	double radiusFromHeight(double h);

	/**
	 * Calculates the gradient at the given height within the shape.
	 */
	double gradientAtHeight(double h);

	/**
	 * Calculates the volume of the shape.
	 */
	default double volume() {
		return volumeFromHeight(height());
	}

	/**
	 * Constrains the height within the shape limits.
	 */
	default double constrainHeight(double h) {
		if (h <= 0.0) return 0.0;
		return Math.min(h, height());
	}

	/**
	 * Constrains the volume within the shape limits.
	 */
	default double constrainVolume(double v) {
		if (v <= 0.0) return 0.0;
		return Math.min(v, volume());
	}
}
