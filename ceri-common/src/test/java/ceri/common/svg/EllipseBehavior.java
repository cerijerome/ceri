package ceri.common.svg;

import static ceri.common.svg.Position.ABSOLUTE_ZERO;
import static ceri.common.svg.Position.RELATIVE_ZERO;
import static ceri.common.svg.SvgTestUtil.assertPath;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public class EllipseBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Ellipse e = Ellipse.builder(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).build();
		Ellipse eq0 = Ellipse.builder(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).build();
		Ellipse eq1 = Ellipse.of(ABSOLUTE_ZERO, Dimension2d.of(1, 2));
		Ellipse eq2 = Ellipse.builder(e).build();
		Ellipse ne0 = Ellipse.builder(RELATIVE_ZERO, Dimension2d.of(1, 2)).build();
		Ellipse ne1 = Ellipse.builder(ABSOLUTE_ZERO, Dimension2d.of(2, 2)).build();
		Ellipse ne2 = Ellipse.builder(ABSOLUTE_ZERO, Dimension2d.of(1, 1)).build();
		Ellipse ne3 = Ellipse.builder(ABSOLUTE_ZERO, Dimension2d.of(1, 2)).rotation(0.1).build();
		Ellipse ne4 = Ellipse.circle(ABSOLUTE_ZERO, 1);
		exerciseEquals(e, eq0, eq1, eq2);
		assertAllNotEqual(e, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineEnd() {
		Ellipse e = Ellipse.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertThat(e.end(), is(Position.absolute(1, 1)));
	}

	@Test
	public void shouldReflect() {
		Ellipse e = Ellipse.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		Line2d line = Line2d.of(-1, 0, 1, 0);
		assertPath(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertPath(e.reflect(line), "M-3,-1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
	}

	@Test
	public void shouldScale() {
		Ellipse e = Ellipse.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertPath(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertPath(e.scale(Ratio2d.uniform(0.5)),
			"M-1.5,0.5 a2,0.5 0 0,1 4,0 a2,0.5 0 0,1 -4,0 m2,0");
	}

	@Test
	public void shouldTranslate() {
		Ellipse e = Ellipse.of(Position.absolute(1, 1), Dimension2d.of(4, 1));
		assertPath(e, "M-3,1 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertPath(e.translate(Point2d.of(-1, -1)), "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
	}

	@Test
	public void shouldRotate() {
		Ellipse e = Ellipse.of(Position.ABSOLUTE_ZERO, Dimension2d.of(4, 1));
		assertPath(e, "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertPath(e.rotate(0), "M-4,0 a4,1 0 0,1 8,0 a4,1 0 0,1 -8,0 m4,0");
		assertPath(e.rotate(90), "M-0,-4 a4,1 0 0,1 0,8 a4,1 0 0,1 -0,-8 m0,4");
		assertPath(e.rotate(-60), "M-2,3.464 a4,1 0 0,1 4,-6.928 a4,1 0 0,1 -4,6.928 m2,-3.464");
	}

	@Test
	public void shouldReverse() {
		assertPath(Ellipse.of(Position.relative(0, 1), Dimension2d.of(2, 1)).reverse(),
			"m-2,-1 a2,1 0 0,1 4,0 a2,1 0 0,1 -4,0 m2,0");
	}

}
