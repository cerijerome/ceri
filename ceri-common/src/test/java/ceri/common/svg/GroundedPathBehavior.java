package ceri.common.svg;

import org.junit.Test;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class GroundedPathBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var pos = Position.relative(2, 1);
		var line = LineTo.absolute(-1, -2);
		var p = GroundedPath.of(pos, line);
		var eq0 = GroundedPath.of(pos, line);
		var ne0 = GroundedPath.of(pos, MoveTo.absolute(-1, -2));
		var ne1 = GroundedPath.of(Position.absolute(2, 1), line);
		var ne2 = GroundedPath.of(Position.relative(1, 1), line);
		var ne3 = GroundedPath.of(pos, LineTo.relative(-1, -2));
		var ne4 = GroundedPath.of(pos, LineTo.absolute(-1, -1));
		TestUtil.exerciseEquals(p, eq0);
		Assert.notEqualAll(p, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldMove() {
		var p = GroundedPath.of(Position.relative(1, 1), MoveTo.relative(2, -2));
		SvgAssert.d(p, "m1,1 m2,-2");
		SvgAssert.d(p.move(Position.absolute(-1, -1)), "M0,0 m2,-2");
	}

	@Test
	public void shouldDetermineEnd() {
		var p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		Assert.equal(p.end(), Position.absolute(3, -1));
	}

	@Test
	public void shouldReflect() {
		var p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		var line = Line2d.of(-1, 0, 1, 0);
		SvgAssert.d(p, "M1,1 m2,-2");
		SvgAssert.d(p.reflect(line), "M1,-1 m2,2");
	}

	@Test
	public void shouldScale() {
		var p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		SvgAssert.d(p, "M1,1 m2,-2");
		SvgAssert.d(p.scale(Ratio2d.uniform(0.5)), "M0.5,0.5 m1,-1");
	}

	@Test
	public void shouldTranslate() {
		var p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		SvgAssert.d(p, "M1,1 m2,-2");
		SvgAssert.d(p.translate(Point2d.of(-1, -1)), "M0,0 m2,-2");
	}

	@Test
	public void shouldReverse() {
		var p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		SvgAssert.d(p, "M1,1 m2,-2");
		SvgAssert.d(p.reverse(), "M3,-1 m-2,2");
	}
}
