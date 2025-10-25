package ceri.common.svg;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.TestUtil;

public class PositionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var p = Position.absolute(1, -1);
		var eq0 = Position.absolute(1, -1);
		var eq1 = Position.absolute(Point2d.of(1, -1));
		var eq2 = Position.of(Position.Type.absolute, Point2d.of(1, -1));
		var ne0 = Position.relative(1, -1);
		var ne1 = Position.relative(Point2d.of(1, -1));
		var ne2 = Position.absolute(1.1, -1);
		var ne3 = Position.absolute(1, 1);
		TestUtil.exerciseEquals(p, eq0, eq1, eq2);
		assertAllNotEqual(p, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCombinePositions() {
		var p0 = Position.absolute(1, -1);
		var p1 = Position.absolute(2, 0);
		var p2 = Position.relative(1, 1);
		assertEquals(p0.combine(p1), p1);
		assertEquals(p1.combine(p0), p0);
		assertEquals(p0.combine(p2), Position.absolute(2, 0));
		assertEquals(p2.combine(p0), p0);
	}

	@Test
	public void shouldCalculateVector() {
		var p = Position.relative(-10, 100);
		assertEquals(p.offset(), Point2d.of(-10, 100));
	}
}
