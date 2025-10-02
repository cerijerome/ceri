package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * A 2d point in polar coordinates using radians.
 */
public record Polar2d(double r, double phi) {
	public static final Polar2d ZERO = new Polar2d(0, 0);

	/**
	 * Normalizes a polar coordinate angle from 0 to 2PI.
	 */
	public static double normalize(double phi) {
		phi %= Math.TAU;
		return phi >= 0.0 ? phi : phi + Math.TAU;
	}
	
	/**
	 * Returns an instance calculated from x,y coordinates.
	 */
	public static Polar2d from(Point2d point) {
		return from(point.x(), point.y());
	}

	/**
	 * Returns an instance calculated from x,y coordinates.
	 */
	public static Polar2d from(double x, double y) {
		Validate.finite(x);
		Validate.finite(y);
		if (x == 0.0 && y == 0.0) return ZERO;
		return of(Point2d.distance(x, y), Point2d.angle(x, y));
	}

	/**
	 * Returns a validated instance.
	 */
	public static Polar2d of(double r, double phi) {
		Validate.finiteMin(r, 0.0);
		Validate.finite(phi);
		if (r == 0.0 && phi == 0.0) return ZERO;
		return new Polar2d(r + 0.0, phi + 0.0);
	}

	/**
	 * Normalizes the angle between 0 and 2PI.
	 */
	public Polar2d normalize() {
		var phi = phi() % Math.TAU;
		if (phi < 0.0) phi += Math.TAU;
		return create(r(), normalize(phi()));
	}

	/**
	 * Converts to x,y coordinates.
	 */
	public Point2d point() {
		return Point2d.fromPolar(r(), phi());
	}

	/**
	 * Rotates by the given angle around the origin.
	 */
	public Polar2d rotate(double angle) {
		return create(r(), phi() + angle);
	}

	/**
	 * Reverses the coordinates about the origin.
	 */
	public Polar2d reverse() {
		return create(r(), -phi());
	}

	@Override
	public String toString() {
		return "(" + r() + ", " + phi() + ")";
	}
	
	private Polar2d create(double r, double phi) {
		if (r == r() && phi == phi()) return this;
		return of(r, phi);
	}
}
