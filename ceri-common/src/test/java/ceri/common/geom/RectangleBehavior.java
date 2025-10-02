package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class RectangleBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Rectangle r = Rectangle.of(0, 0, 20, 40);
		Rectangle eq0 = Rectangle.of(Point2d.of(0, 0), Size2d.of(20, 40));
		Rectangle ne0 = Rectangle.of(1, 0, 20, 40);
		Rectangle ne1 = Rectangle.of(0, -1, 20, 40);
		Rectangle ne2 = Rectangle.of(0, 0, 19, 40);
		Rectangle ne3 = Rectangle.of(0, 0, 20, 41);
		Rectangle ne4 = Rectangle.of(0, 0, 0, 0);
		TestUtil.exerciseEquals(r, eq0);
		assertAllNotEqual(r, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldCalculateArea() {
		assertEquals(Rectangle.of(100, -200, 0, 0).area(), 0.0);
		assertEquals(Rectangle.of(10, -20, 50, 20).area(), 1000.0);
	}

	@Test
	public void shouldExposeDimensions() {
		Rectangle r = Rectangle.of(100, -20, 50, 10);
		assertEquals(r.position(), Point2d.of(100, -20));
		assertEquals(r.size(), Size2d.of(50, 10));
		assertEquals(r.corner(), Point2d.of(150, -10));
	}

}
