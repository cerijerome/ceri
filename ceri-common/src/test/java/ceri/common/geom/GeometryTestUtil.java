package ceri.common.geom;

import static ceri.common.test.TestUtil.APPROX_PRECISION_DEF;
import ceri.common.test.TestUtil;

public class GeometryTestUtil {

	private GeometryTestUtil() {}

	public static void assertApprox(Point2d point, Point2d expected) {
		assertApprox(point, expected, APPROX_PRECISION_DEF);
	}

	public static void assertApprox(Point2d point, Point2d expected, int precision) {
		assertApprox(point, expected.x, expected.y, precision);
	}

	public static void assertApprox(Point2d point, double x, double y) {
		assertApprox(point, x, y, APPROX_PRECISION_DEF);
	}

	public static void assertApprox(Point2d point, double x, double y, int precision) {
		TestUtil.assertApprox(point.x, x, precision);
		TestUtil.assertApprox(point.y, y, precision);
	}

}
