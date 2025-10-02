package ceri.common.geom;

import static ceri.common.test.AssertUtil.APPROX_PRECISION_DEF;
import static ceri.common.test.AssertUtil.assertEquals;
import ceri.common.test.AssertUtil;

public class GeometryAssert {

	private GeometryAssert() {}

	public static void approx(Point2d point, Point2d expected) {
		approx(point, expected, APPROX_PRECISION_DEF);
	}

	public static void approx(Point2d point, Point2d expected, int precision) {
		approx(point, expected.x(), expected.y(), precision);
	}

	public static void approx(Point2d point, double x, double y) {
		approx(point, x, y, APPROX_PRECISION_DEF);
	}

	public static void approx(Point2d point, double x, double y, int precision) {
		AssertUtil.assertApprox(point.x(), x, precision);
		AssertUtil.assertApprox(point.y(), y, precision);
	}
	
	public static void assertRect(Rectangle rect, double x, double y, double w, double h) {
		AssertUtil.approx(rect.x(), x);
		assertEquals(rect.y(), y);
		assertEquals(rect.w(), w);
		assertEquals(rect.h(), h);
	}
}
