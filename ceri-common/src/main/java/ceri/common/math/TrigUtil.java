package ceri.common.math;

import static ceri.common.validation.ValidationUtil.*;

public class TrigUtil {

	private TrigUtil() {}

	/**
	 * Finds the area of a segment of a circle with given radius and segment angle in radians.
	 */
	public static double segmentArea(double radius, double angle) {
		validateRange(angle, 0, MathUtil.PIx2, "angle");
		return 0.5 * (angle - Math.sin(angle)) * radius * radius;
	}

	/**
	 * Finds the angle in radians of the tangent to the circle of given radius, from the given
	 * distance from the circle origin.
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
