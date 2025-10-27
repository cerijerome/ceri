package ceri.common.svg;

import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.Assert;
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
		Assert.notEqualAll(p, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCombinePositions() {
		var p0 = Position.absolute(1, -1);
		var p1 = Position.absolute(2, 0);
		var p2 = Position.relative(1, 1);
		Assert.equal(p0.combine(p1), p1);
		Assert.equal(p1.combine(p0), p0);
		Assert.equal(p0.combine(p2), Position.absolute(2, 0));
		Assert.equal(p2.combine(p0), p0);
	}

	@Test
	public void shouldCalculateVector() {
		var p = Position.relative(-10, 100);
		Assert.equal(p.offset(), Point2d.of(-10, 100));
	}
}
