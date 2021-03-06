package ceri.common.geom;

/**
 * Describes a 3d shape in which every cross-section perpendicular to the vertical axis is a circle.
 */
public interface Radial3d {

	double height();

	double volumeFromHeight(double h);

	double heightFromVolume(double v);

	double radiusFromHeight(double h);

	double gradientAtHeight(double h);

	default double volume() {
		return volumeFromHeight(height());
	}

	default double constrainHeight(double h) {
		if (h < 0) return 0;
		double H = height();
		return Math.min(h, H);
	}

	default double constrainVolume(double v) {
		if (v < 0) return 0;
		double V = volume();
		return Math.min(v, V);
	}

}
