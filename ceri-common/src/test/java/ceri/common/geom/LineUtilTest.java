package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class LineUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LineUtil.class);
	}

	@Test
	public void testDistanceOfZeroLengthLineToPoint() {
		Line2d l = Line2d.ZERO;
		assertEquals(LineUtil.distance(l, 0, 0), 0.0);
		assertEquals(LineUtil.distance(l, 1, 0), 1.0);
		assertEquals(LineUtil.distance(l, 0, 1), 1.0);
		assertEquals(LineUtil.distance(l, -3, -4), 5.0);
		l = Line2d.of(Point2d.X_UNIT, Point2d.X_UNIT);
		assertEquals(LineUtil.distance(l, 0, 0), 1.0);
		assertEquals(LineUtil.distance(l, 1, 0), 0.0);
		assertApprox(LineUtil.distance(l, 0, -1), 1.4142);
	}

	@Test
	public void testDistanceOfLineToPoint() {
		Line2d l = Line2d.of(Point2d.Y_UNIT, Point2d.ZERO);
		assertEquals(LineUtil.distance(l, 0, 0), 0.0);
		assertEquals(LineUtil.distance(l, 1, 0), 1.0);
		assertEquals(LineUtil.distance(l, 0, 1), 0.0);
		assertEquals(LineUtil.distance(l, 0, 3), 2.0);
		assertEquals(LineUtil.distance(l, 0, -2), 2.0);
		assertEquals(LineUtil.distance(l, 3, -4), 5.0);
		assertEquals(LineUtil.distance(l, -4, 4), 5.0);
	}

}
