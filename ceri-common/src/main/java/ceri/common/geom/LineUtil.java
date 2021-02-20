package ceri.common.geom;

import static ceri.common.geom.GeometryUtil.point;
import static ceri.common.geom.GeometryUtil.vector;
import ceri.common.math.MathUtil;
import ceri.common.math.Matrix;

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
		Matrix ab = vector(line.vector);
		Matrix ap = vector(line.from.to(point));
		double t = ab.dot(ap) / ab.dot(ab);
		t = MathUtil.limit(t, 0, 1);
		Matrix c = vector(line.from).add(ab.multiply(t));
		return point(c).distanceTo(point);
	}

}
