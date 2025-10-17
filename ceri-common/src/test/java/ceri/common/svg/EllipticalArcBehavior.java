package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.geom.Size2d;
import ceri.common.test.TestUtil;

public class EllipticalArcBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var e = new EllipticalArc(Position.ABSOLUTE_ZERO, Size2d.of(2, 2), 0.0,
			Svg.LargeArcFlag.small, Svg.SweepFlag.positive);
		var eq0 = EllipticalArc.of(Position.ABSOLUTE_ZERO, Size2d.of(2, 2));
		var eq1 = EllipticalArc.circular(Position.ABSOLUTE_ZERO, 2);
		var ne0 = EllipticalArc.of(Position.RELATIVE_ZERO, Size2d.of(2, 2));
		var ne1 = EllipticalArc.of(Position.ABSOLUTE_ZERO, Size2d.of(1, 2));
		var ne2 = new EllipticalArc(Position.ABSOLUTE_ZERO, Size2d.of(2, 2), 1.0,
			Svg.LargeArcFlag.small, Svg.SweepFlag.positive);
		var ne3 = new EllipticalArc(Position.ABSOLUTE_ZERO, Size2d.of(2, 2), 0.0,
			Svg.LargeArcFlag.large, Svg.SweepFlag.positive);
		var ne4 = new EllipticalArc(Position.ABSOLUTE_ZERO, Size2d.of(2, 2), 0.0,
			Svg.LargeArcFlag.small, Svg.SweepFlag.negative);
		TestUtil.exerciseEquals(e, eq0, eq1);
		assertAllNotEqual(e, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldModifyFlags() {
		SvgAssert.d(EllipticalArc.of(Position.absolute(1, 1), Size2d.of(1, 2))
			.flag(Svg.LargeArcFlag.large).flag(Svg.SweepFlag.negative), "A1,2 0 1,0 1,1");
	}

	@Test
	public void shouldDetermineEnd() {
		var e = EllipticalArc.of(Position.absolute(1, 1), Size2d.of(4, 1));
		assertEquals(e.end(), Position.absolute(1, 1));
	}

	@Test
	public void shouldReflect() {
		var e = EllipticalArc.of(Position.absolute(1, 1), Size2d.of(4, 1));
		var line = Line2d.of(-1, 0, 1, 0);
		SvgAssert.d(e, "A4,1 0 0,1 1,1");
		SvgAssert.d(e.reflect(line), "A4,1 0 0,0 1,-1");
	}

	@Test
	public void shouldScale() {
		var e = EllipticalArc.of(Position.absolute(1, 1), Size2d.of(4, 1));
		SvgAssert.d(e, "A4,1 0 0,1 1,1");
		SvgAssert.d(e.scale(Ratio2d.uniform(0.5)), "A2,0.5 0 0,1 0.5,0.5");
	}

	@Test
	public void shouldTranslate() {
		var e = EllipticalArc.of(Position.absolute(1, 1), Size2d.of(4, 1));
		SvgAssert.d(e, "A4,1 0 0,1 1,1");
		SvgAssert.d(e.translate(Point2d.of(-1, -1)), "A4,1 0 0,1 0,0");
	}

	@Test
	public void shouldRotate() {
		var e = EllipticalArc.of(Position.ABSOLUTE_ZERO, Size2d.of(4, 1));
		SvgAssert.d(e, "A4,1 0 0,1 0,0");
		SvgAssert.d(e.rotate(0), "A4,1 0 0,1 0,0");
		SvgAssert.d(e.rotate(90), "A4,1 90 0,1 0,0");
		SvgAssert.d(e.rotate(-60), "A4,1 -60 0,1 0,0");
	}

	@Test
	public void shouldReverse() {
		SvgAssert.d(EllipticalArc.of(Position.relative(0, 1), Size2d.of(2, 1)).reverse(),
			"a2,1 0 0,0 0,-1");
	}
}
