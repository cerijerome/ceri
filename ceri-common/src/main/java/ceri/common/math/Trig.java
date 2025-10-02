package ceri.common.math;

import ceri.common.util.Validate;

public class Trig {
	public static final Interval<Double> INTERVAL =
		Interval.of(Bound.inclusive(0.0), Bound.exclusive(Math.TAU));

	private Trig() {}

	/**
	 * Normalizes the angle in radians from 0 to 2PI inclusive.
	 */
	public static double normalize(double angle) {
		return angle == Math.TAU ? angle : normalizeStrict(angle);
	}

	/**
	 * Normalizes the angle in radians from 0 to 2PI exclusive.
	 */
	public static double normalizeStrict(double angle) {
		angle = angle % Math.TAU;
		return angle >= 0 ? angle : angle + Math.TAU;
	}

	/**
	 * Determines if given angle is within inclusive bounds, normalized to [0..2PI).
	 */
	public static boolean withinNormalizedBounds(double value, double lower, double range) {
		value = normalize(value);
		lower = normalize(lower);
		if (value < lower) value += Math.TAU;
		return Interval.inclusive(lower, lower + range).contains(value);
	}

	/**
	 * Finds the area of a segment of a circle with given radius and normalized segment angle in
	 * radians.
	 */
	public static double segmentArea(double radius, double angle) {
		angle = normalize(angle);
		return 0.5 * (angle - Math.sin(angle)) * radius * radius;
	}

	/**
	 * Finds the angle in radians between the line through the center of a circle and a tangent to
	 * the circle starting at the same point at given distance away from the center.
	 */
	public static double tangentAngle(double radius, double distance) {
		Validate.finiteMin(distance, radius, "distance");
		return Math.asin(radius / distance);
	}

	/**
	 * Finds the angle in radians of the segment created by the intersecting line from the given
	 * distance, and given angle from the circle origin.
	 */
	public static double intersectionSegmentAngle(double radius, double distance, double angle) {
		double tangentAngle = tangentAngle(radius, distance);
		if (angle <= -tangentAngle) return 0.0;
		if (angle >= tangentAngle) return Math.TAU;
		if (angle < 0) return 2.0 * Math.acos(distance * Math.sin(-angle) / radius);
		return Math.TAU - (2.0 * Math.acos(distance * Math.sin(angle) / radius));
	}
}
