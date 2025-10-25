package ceri.common.svg;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.test.TestUtil;

public class PathGroupBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var m = MoveTo.relative(1, 1);
		var l = LineTo.absolute(2, 0);
		var g = PathGroup.of(m, l);
		var eq0 = PathGroup.of(m, l);
		var ne0 = PathGroup.of(m);
		var ne1 = PathGroup.of(l);
		var ne2 = PathGroup.of(MoveTo.absolute(1, 1), l);
		var ne3 = PathGroup.of(m, LineTo.relative(2, 0));
		TestUtil.exerciseEquals(g, eq0);
		assertAllNotEqual(g, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldDetermineEnd() {
		var g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), LineTo.absolute(0, 0));
		assertEquals(g.end(), Position.absolute(0, 0));
	}

	@Test
	public void shouldReflect() {
		var g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), LineTo.absolute(0, 0));
		var line = Line2d.of(-1, 0, 1, 0);
		SvgAssert.d(g, "l1,1 m4,1 L0,0");
		SvgAssert.d(g.reflect(line), "l1,-1 m4,-1 L0,0");
	}

	@Test
	public void shouldScale() {
		var g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), LineTo.absolute(0, 0));
		SvgAssert.d(g, "l1,1 m4,1 L0,0");
		SvgAssert.d(g.scale(Ratio2d.uniform(0.5)), "l0.5,0.5 m2,0.5 L0,0");
	}

	@Test
	public void shouldTranslate() {
		var g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), LineTo.absolute(0, 0));
		SvgAssert.d(g, "l1,1 m4,1 L0,0");
		SvgAssert.d(g.translate(Point2d.of(-1, -1)), "l1,1 m4,1 L-1,-1");
	}

	@Test
	public void shouldReverse() {
		var g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), LineTo.absolute(0, 0));
		SvgAssert.d(g, "l1,1 m4,1 L0,0");
		SvgAssert.d(g.reverse(), "L0,0 m-4,-1 l-1,-1");
	}
}
