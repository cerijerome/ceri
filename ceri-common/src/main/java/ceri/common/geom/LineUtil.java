package ceri.common.geom;

import ceri.common.math.Maths;

public class LineUtil {

	private LineUtil() {}

	/**
	 * Calculates the shortest distance from a point to a line segment.
	 */
	public static double distance(Line2d line, double x, double y) {
		return distance(line, Point2d.of(x, y));
	}

	/**
	 * Calculates the shortest distance from a point to a line segment.
	 */
	public static double distance(Line2d line, Point2d point) {
		// a = start of line, b = end of line, p = point, c = closest point on line to p
		// a--c---b
		// |
		// p
		if (line.vector.equals(Point2d.ZERO)) return line.from.distanceTo(point);
		var ab = GeometryUtil.vector(line.vector);
		var ap = GeometryUtil.vector(line.from.to(point));
		double t = ab.dot(ap) / ab.dot(ab);
		t = Maths.limit(t, 0, 1);
		var c = GeometryUtil.vector(line.from).add(ab.multiply(t));
		return GeometryUtil.point(c).distanceTo(point);
	}
}
