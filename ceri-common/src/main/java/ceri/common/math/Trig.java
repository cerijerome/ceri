package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import static java.lang.Math.TAU;

public class Trig {
	public static final Interval<Double> INTERVAL =
		Interval.of(Bound.inclusive(0.0), Bound.exclusive(TAU));

	private Trig() {}

	/**
	 * Normalizes given angle in radians to the range [0..2PI).
	 */
	public static double normalize(double angle) {
		angle = angle % TAU;
		return angle >= 0 ? angle : angle + TAU;
	}

	/**
	 * Determines if given angle is within inclusive bounds, normalized to [0..2PI).
	 */
	public static boolean withinNormalizedBounds(double value, double lower, double range) {
		value = normalize(value);
		lower = normalize(lower);
		if (value < lower) value += TAU;
		return Interval.inclusive(lower, lower + range).contains(value);
	}

	/**
	 * Finds the area of a segment of a circle with given radius and segment angle in radians.
	 */
	public static double segmentArea(double radius, double angle) {
		validateRangeFp(angle, 0, TAU, "angle");
		return 0.5 * (angle - Math.sin(angle)) * radius * radius;
	}

	/**
	 * Finds the angle in radians between the line through the center of a circle and a tangent to
	 * the circle starting at the same point at given distance away from the center.
	 */
	public static double tangentAngle(double radius, double distance) {
		validateMinFp(distance, radius, "distance");
		return Math.asin(radius / distance);
	}

	/**
	 * Finds the angle in radians of the segment created by the intersecting line from the given
	 * distance, and given angle from the circle origin.
	 */
	public static double intersectionSegmentAngle(double radius, double distance, double angle) {
		double tangentAngle = tangentAngle(radius, distance);
		if (angle <= -tangentAngle) return 0.0;
		if (angle >= tangentAngle) return TAU;
		if (angle < 0) return 2.0 * Math.acos(distance * Math.sin(-angle) / radius);
		return TAU - (2.0 * Math.acos(distance * Math.sin(angle) / radius));
	}

}
