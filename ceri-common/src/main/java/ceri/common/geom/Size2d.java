package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * Represents dimensions in 2d.
 */
public record Size2d(double w, double h) {
	public static final Size2d ZERO = new Size2d(0.0, 0.0);

	/**
	 * Calculates the area.
	 */
	public static double area(double w, double h) {
		return w * h;
	}

	/**
	 * Creates a validated instance.
	 */
	public static Size2d of(double w, double h) {
		if (w == 0.0 && h == 0.0) return ZERO;
		return new Size2d(w + 0.0, h + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Size2d {
		Validate.finiteMin(w, 0);
		Validate.finiteMin(h, 0);
	}

	/**
	 * Returns true if the dimensions are not positive.
	 */
	public boolean isZero() {
		return equals(ZERO);
	}

	/**
	 * Calculates the area.
	 */
	public double area() {
		return area(w(), h());
	}

	/**
	 * Resizes the dimensions.
	 */
	public Size2d resize(double ratio) {
		return resize(ratio, ratio);
	}

	/**
	 * Resizes the dimensions.
	 */
	public Size2d resize(Ratio2d ratio) {
		return resize(ratio.x(), ratio.y());
	}

	/**
	 * Resizes the dimensions.
	 */
	public Size2d resize(double x, double y) {
		return create(w() * x, h() * y);
	}

	/**
	 * Calculates the aspect ratio of width to height.
	 */
	public double aspectRatio() {
		if (Double.doubleToLongBits(w()) == Double.doubleToLongBits(h())) return 1.0;
		if (h() == 0.0) return Double.POSITIVE_INFINITY;
		return w() / h();
	}

	@Override
	public String toString() {
		return "(" + w() + " x " + h() + ")";
	}

	private Size2d create(double w, double h) {
		if (w() == w && h() == h) return this;
		return of(w, h);
	}
}
