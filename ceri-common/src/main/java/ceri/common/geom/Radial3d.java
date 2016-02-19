package ceri.common.geom;


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
		if (h > H) return H;
		return h;
	}

	default double constrainVolume(double v) {
		if (v < 0) return 0;
		double V = volume();
		if (v > V) return V;
		return v;
	}

}
