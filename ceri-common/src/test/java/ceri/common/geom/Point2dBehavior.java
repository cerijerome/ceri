package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Point2dBehavior {

	@Test
	public void shouldNotAllowNaN() {
		assertTrue(Point2d.NULL.isNull());
		assertThrown(() -> Point2d.of(Double.NaN, 0));
		assertThrown(() -> Point2d.of(0, Double.NaN));
		assertFalse(Point2d.X_UNIT.isNull());
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		Point2d p0 = Point2d.of(5, 10);
		Point2d p1 = Point2d.of(5, 10);
		Point2d p2 = Point2d.of(4.999, 10);
		Point2d p3 = Point2d.of(5, 10.001);
		TestUtil.exerciseEquals(p0, p1);
		assertNotEquals(p0, p2);
		assertNotEquals(p0, p3);
	}

	@Test
	public void shouldReverse() {
		assertEquals(Point2d.ZERO.reverse(), Point2d.ZERO);
		assertEquals(Point2d.of(9.99, -7.77).reverse(), Point2d.of(-9.99, 7.77));
	}

	@Test
	public void shouldCalculateToAndFrom() {
		assertEquals(Point2d.of(10, 5).to(Point2d.of(0, -1)), Point2d.of(-10, -6));
		assertEquals(Point2d.of(-10, -6).from(Point2d.of(0, -1)), Point2d.of(-10, -5));
	}

	@Test
	public void shouldTranslateCoordinates() {
		Point2d p0 = Point2d.of(5, -10);
		Point2d p1 = Point2d.of(1, 1);
		Point2d p2 = Point2d.of(0, 0);
		Point2d p3 = Point2d.of(-5, 10);
		assertEquals(p0.translate(p1), Point2d.of(6, -9));
		assertEquals(p0.translate(p2), Point2d.of(5, -10));
		assertEquals(p0.translate(p3), Point2d.of(0, 0));
	}

	@Test
	public void shouldScaleCoordinates() {
		Point2d p0 = Point2d.of(-5, 10);
		Ratio2d r0 = Ratio2d.of(2, 0.5);
		Ratio2d r1 = Ratio2d.uniform(0.1);
		assertEquals(p0.scale(r0), Point2d.of(-10, 5));
		assertEquals(p0.scale(r1), Point2d.of(-0.5, 1));
		assertEquals(p0.scale(Ratio2d.ONE), p0);
		assertEquals(p0.scale(Ratio2d.ZERO), Point2d.ZERO);
	}

}
