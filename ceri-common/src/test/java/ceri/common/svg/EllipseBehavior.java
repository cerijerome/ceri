package ceri.common.svg;

import static ceri.common.svg.SvgTest.assertD;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.geom.Size2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.test.TestUtil;

public class EllipseBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var e = new Ellipse(Position.ABSOLUTE_ZERO, Size2d.of(1, 1), 0.0);
		var eq0 = Ellipse.of(Position.ABSOLUTE_ZERO, Size2d.of(1, 1));
		var eq1 = Ellipse.circle(Position.ABSOLUTE_ZERO, 1);
		var ne0 = new Ellipse(Position.RELATIVE_ZERO, Size2d.of(1, 1), 0.0);
		var ne1 = new Ellipse(Position.absolute(0, 1), Size2d.of(1, 1), 0.0);
		var ne2 = new Ellipse(Position.ABSOLUTE_ZERO, Size2d.of(1, 2), 0.0);
		var ne3 = new Ellipse(Position.ABSOLUTE_ZERO, Size2d.of(1, 1), 1.0);
		TestUtil.exerciseEquals(e, eq0, eq1);
		assertAllNotEqual(e, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldDetermineEnd() {
		var e = Ellipse.of(Position.absolute(1, 1), Size2d.of(4, 1));
		assertEquals(e.end(), Position.absolute(1, 1));
	}

	@Test
	public void shouldReflect() {
		var e = Ellipse.of(Position.absolute(1, 1), Size2d.of(4, 1));
		var line = Line2d.of(-1, 0, 1, 0);
		assertD(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertD(e.reflect(line), "M-3,-1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
	}

	@Test
	public void shouldScale() {
		var e = Ellipse.of(Position.absolute(1, 1), Size2d.of(4, 1));
		assertD(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertD(e.scale(Ratio2d.uniform(0.5)),
			"M-1.5,0.5 a2,0.5 0 0,1 4,0 a2,0.5 0 0,1 -4,0 m2,0");
	}

	@Test
	public void shouldTranslate() {
		var e = Ellipse.of(Position.absolute(1, 1), Size2d.of(4, 1));
		assertD(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertD(e.translate(Point2d.of(-1, -1)), "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
	}

	@Test
	public void shouldRotate() {
		var e = Ellipse.of(Position.ABSOLUTE_ZERO, Size2d.of(4, 1));
		assertD(e, "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertD(e.rotate(0), "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertD(e.rotate(90), "M-0,-4 a4,1 0 0,1 0,8 a4,1 0 0,1 -0,-8 m0,4");
		assertD(e.rotate(-60), "M-2,3.464 a4,1 0 0,1 4,-6.928 a4,1 0 0,1 -4,6.928 m2,-3.464");
	}

	@Test
	public void shouldReverse() {
		assertD(Ellipse.of(Position.relative(0, 1), Size2d.of(2, 1)).reverse(),
			"m-2,-1 a2,1 0 0,1 4,0 a2,1 0 0,1 -4,0 m2,0");
	}
}
