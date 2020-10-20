package ceri.common.geom;

import static ceri.common.test.AssertUtil.APPROX_PRECISION_DEF;
import ceri.common.test.AssertUtil;

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
		AssertUtil.assertApprox(point.x, x, precision);
		AssertUtil.assertApprox(point.y, y, precision);
	}

}
