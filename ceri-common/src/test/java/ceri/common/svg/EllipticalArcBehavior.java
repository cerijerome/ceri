package ceri.common.svg;

import static ceri.common.svg.LargeArcFlag.large;
import static ceri.common.svg.Position.ABSOLUTE_ZERO;
import static ceri.common.svg.Position.RELATIVE_ZERO;
import static ceri.common.svg.SvgTestUtil.assertPath;
import static ceri.common.svg.SweepFlag.negative;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public class EllipticalArcBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		EllipticalArc e = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2));
		EllipticalArc eq0 = EllipticalArc.builder(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).build();
		EllipticalArc eq1 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2));
		EllipticalArc eq2 = EllipticalArc.builder(e).build();
		EllipticalArc ne0 = EllipticalArc.of(RELATIVE_ZERO, Dimension2d.of(1, 2));
		EllipticalArc ne1 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(2, 2));
		EllipticalArc ne2 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 1));
		EllipticalArc ne3 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).rotate(0.1);
		EllipticalArc ne4 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).flag(large);
		EllipticalArc ne5 = EllipticalArc.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).flag(negative);
		EllipticalArc ne6 = EllipticalArc.circular(ABSOLUTE_ZERO, 1);
		exerciseEquals(e, eq0, eq1, eq2);
		assertAllNotEqual(e, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldDetermineEnd() {
		EllipticalArc e = EllipticalArc.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertEquals(e.end(), Position.absolute(1, 1));
	}

	@Test
	public void shouldReflect() {
		EllipticalArc e = EllipticalArc.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		Line2d line = Line2d.of(-1, 0, 1, 0);
		assertPath(e, "A4,1 0 0,1 1,1");
		assertPath(e.reflect(line), "A4,1 0 0,0 1,-1");
	}

	@Test
	public void shouldScale() {
		EllipticalArc e = EllipticalArc.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertPath(e, "A4,1 0 0,1 1,1");
		assertPath(e.scale(Ratio2d.uniform(0.5)), "A2,0.5 0 0,1 0.5,0.5");
	}

	@Test
	public void shouldTranslate() {
		EllipticalArc e = EllipticalArc.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertPath(e, "A4,1 0 0,1 1,1");
		assertPath(e.translate(Point2d.of(-1, -1)), "A4,1 0 0,1 0,0");
	}

	@Test
	public void shouldRotate() {
		EllipticalArc e = EllipticalArc.of(Position.ABSOLUTE_ZERO, Dimension2d.of(4, 1));
		assertPath(e, "A4,1 0 0,1 0,0");
		assertPath(e.rotate(0), "A4,1 0 0,1 0,0");
		assertPath(e.rotate(90), "A4,1 90 0,1 0,0");
		assertPath(e.rotate(-60), "A4,1 -60 0,1 0,0");
	}

	@Test
	public void shouldReverse() {
		assertPath(EllipticalArc.of(Position.relative(0, 1), Dimension2d.of(2, 1)).reverse(),
			"a2,1 0 0,0 0,-1");
	}

}
