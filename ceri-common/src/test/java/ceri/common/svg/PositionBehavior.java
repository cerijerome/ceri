package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.TestUtil;

public class PositionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Position p = Position.absolute(1, -1);
		Position eq0 = Position.absolute(1, -1);
		Position eq1 = Position.absolute(Point2d.of(1, -1));
		Position ne0 = Position.relative(1, -1);
		Position ne1 = Position.relative(Point2d.of(1, -1));
		Position ne2 = Position.absolute(1.1, -1);
		Position ne3 = Position.absolute(1, 1);
		TestUtil.exerciseEquals(p, eq0, eq1);
		assertAllNotEqual(p, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCombinePositions() {
		Position p0 = Position.absolute(1, -1);
		Position p1 = Position.absolute(2, 0);
		Position p2 = Position.relative(1, 1);
		assertEquals(p0.combine(null), p0);
		assertEquals(p0.combine(p1), p1);
		assertEquals(p1.combine(p0), p0);
		assertEquals(p0.combine(p2), Position.absolute(2, 0));
		assertEquals(p2.combine(p0), p0);
	}

	@Test
	public void shouldCalculateVector() {
		Position p = Position.relative(-10, 100);
		assertEquals(p.vector(), Point2d.of(-10, 100));
	}

}
