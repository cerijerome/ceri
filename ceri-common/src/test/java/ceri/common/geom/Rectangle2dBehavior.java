package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Rectangle2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Rectangle2d r = Rectangle2d.of(0, 0, 20, 40);
		Rectangle2d eq0 = Rectangle2d.of(Point2d.of(0, 0), Dimension2d.of(20, 40));
		Rectangle2d ne0 = Rectangle2d.of(1, 0, 20, 40);
		Rectangle2d ne1 = Rectangle2d.of(0, -1, 20, 40);
		Rectangle2d ne2 = Rectangle2d.of(0, 0, 19, 40);
		Rectangle2d ne3 = Rectangle2d.of(0, 0, 20, 41);
		Rectangle2d ne4 = Rectangle2d.of(0, 0, 0, 0);
		TestUtil.exerciseEquals(r, eq0);
		assertAllNotEqual(r, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldCalculateArea() {
		assertEquals(Rectangle2d.of(100, -200, 0, 0).area(), 0.0);
		assertEquals(Rectangle2d.of(10, -20, 50, 20).area(), 1000.0);
	}

	@Test
	public void shouldExposeDimensions() {
		Rectangle2d r = Rectangle2d.of(100, -20, 50, 10);
		assertEquals(r.position(), Point2d.of(100, -20));
		assertEquals(r.size(), Dimension2d.of(50, 10));
		assertEquals(r.corner(), Point2d.of(150, -10));
	}

}
