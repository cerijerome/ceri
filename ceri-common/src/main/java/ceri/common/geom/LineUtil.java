package ceri.common.geom;

import static ceri.common.math.VectorUtil.fromPoint;
import ceri.common.math.MathUtil;
import ceri.common.math.Vector;
import ceri.common.math.VectorUtil;

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
		//    |
		//    p
		if (line.vector.equals(Point2d.ZERO)) return line.from.distanceTo(point);
		Vector ab = fromPoint(line.vector);
		Vector ap = fromPoint(line.from.to(point));
		double t = ab.scalarProduct(ap) / ab.scalarProduct(ab);
		t = MathUtil.limit(t, 0, 1);
		Vector c = fromPoint(line.from).add(ab.multiply(t));
		return VectorUtil.toPoint(c).distanceTo(point);
	}
	
}
