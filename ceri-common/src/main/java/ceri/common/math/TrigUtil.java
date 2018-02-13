package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;

public class TrigUtil {
	public static final Interval<Double> INTERVAL =
		Interval.of(Bound.inclusive(0.0), Bound.exclusive(MathUtil.PIx2));

	private TrigUtil() {}

	/**
	 * Normalizes given angle in radians to the range [0..2PI).
	 */
	public static double normalize(double angle) {
		while (angle < 0)
			angle += MathUtil.PIx2;
		while (angle >= MathUtil.PIx2)
			angle -= MathUtil.PIx2;
		return angle;
	}

	/**
	 * Determines if given angle is within inclusive bounds, normalized to [0..2PI).
	 */
	public static boolean withinNormalizedBounds(double value, double lower, double range) {
		value = normalize(value);
		lower = normalize(lower);
		if (value < lower) value += MathUtil.PIx2;
		return Interval.inclusive(lower, lower + range).contains(value);
	}

	/**
	 * Finds the area of a segment of a circle with given radius and segment angle in radians.
	 */
	public static double segmentArea(double radius, double angle) {
		validateRange(angle, 0, MathUtil.PIx2, "angle");
		return 0.5 * (angle - Math.sin(angle)) * radius * radius;
	}

	/**
	 * Finds the angle in radians between the line through the center of a circle and a tangent to
	 * the circle starting at the same point at given distance away from the center.
	 */
	public static double tangentAngle(double radius, double distance) {
		validateMin(distance, radius, "distance");
		return Math.asin(radius / distance);
	}

	/**
	 * Finds the angle in radians of the segment created by the intersecting line from the given
	 * distance, and given angle from the circle origin.
	 */
	public static double intersectionSegmentAngle(double radius, double distance, double angle) {
		double tangentAngle = tangentAngle(radius, distance);
		if (angle <= -tangentAngle) return 0.0;
		if (angle >= tangentAngle) return MathUtil.PIx2;
		if (angle < 0) return 2.0 * Math.acos(distance * Math.sin(-angle) / radius);
		return MathUtil.PIx2 - (2.0 * Math.acos(distance * Math.sin(angle) / radius));
	}

}
