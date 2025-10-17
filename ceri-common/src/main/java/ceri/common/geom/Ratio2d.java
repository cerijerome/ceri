package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * A ratio multiplier for x and y coordinates.
 */
public record Ratio2d(double x, double y) {
	public static final Ratio2d ZERO = new Ratio2d(0, 0);
	public static final Ratio2d UNIT = new Ratio2d(1, 1);

	/**
	 * Returns an instance with equal x and y ratios.
	 */
	public static Ratio2d uniform(double scale) {
		return of(scale, scale);
	}

	/**
	 * Returns an instance for x and y ratios.
	 */
	public static Ratio2d of(double x, double y) {
		if (x == 0.0 && y == 0.0) return ZERO;
		if (x == 1.0 && y == 1.0) return UNIT;
		return new Ratio2d(x + 0.0, y + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Ratio2d {
		Validate.finiteMin(x, 0.0);
		Validate.finiteMin(y, 0.0);
	}

	/**
	 * Returns true if this ratio is zero.
	 */
	public boolean isZero() {
		return equals(ZERO);
	}

	/**
	 * Returns true if this ratio is non-modifying unit ratio.
	 */
	public boolean isUnit() {
		return equals(UNIT);
	}

	/**
	 * Returns an instance with multiplied x and y ratios.
	 */
	public Ratio2d multiply(double r) {
		return multiply(r, r);
	}

	/**
	 * Returns an instance with multiplied x and y ratios.
	 */
	public Ratio2d multiply(double x, double y) {
		return create(x() * x, y() * y);
	}

	/**
	 * Returns an instance with multiplied x and y ratios.
	 */
	public Ratio2d multiply(Ratio2d r) {
		return multiply(r.x(), r.y());
	}

	/**
	 * Returns true if the ratios are the same is the given x and y values.
	 */
	public boolean equals(double x, double y) {
		return x() == x && y() == y;
	}

	@Override
	public String toString() {
		return "(" + x() + " x " + y() + ")";
	}

	private Ratio2d create(double x, double y) {
		if (equals(x, y)) return this;
		return of(x, y);
	}
}
