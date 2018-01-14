package ceri.common.svg;

import static ceri.common.svg.SvgTestUtil.assertPath;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public class PathGroupBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		MoveTo m = MoveTo.relative(1, 1);
		LineTo l = LineTo.absolute(2, 0);
		PathGroup g = PathGroup.of(m, l);
		PathGroup eq0 = PathGroup.of(m, l);
		PathGroup ne0 = PathGroup.of(m);
		PathGroup ne1 = PathGroup.of(l);
		PathGroup ne2 = PathGroup.of(MoveTo.absolute(1, 1), l);
		PathGroup ne3 = PathGroup.of(m, LineTo.relative(2, 0));
		exerciseEquals(g, eq0);
		assertAllNotEqual(g, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldDetermineEnd() {
		PathGroup g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), //
			LineTo.absolute(0, 0));
		assertThat(g.end(), is(Position.absolute(0, 0)));
	}

	@Test
	public void shouldReflect() {
		PathGroup g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), //
			LineTo.absolute(0, 0));
		Line2d line = Line2d.of(-1, 0, 1, 0);
		assertPath(g, "l1,1 m4,1 L0,0");
		assertPath(g.reflect(line), "l1,-1 m4,-1 L0,0");
	}

	@Test
	public void shouldScale() {
		PathGroup g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), //
			LineTo.absolute(0, 0));
		assertPath(g, "l1,1 m4,1 L0,0");
		assertPath(g.scale(Ratio2d.uniform(0.5)), "l0.5,0.5 m2,0.5 L0,0");
	}

	@Test
	public void shouldTranslate() {
		PathGroup g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), //
			LineTo.absolute(0, 0));
		assertPath(g, "l1,1 m4,1 L0,0");
		assertPath(g.translate(Point2d.of(-1, -1)), "l1,1 m4,1 L-1,-1");
	}

	@Test
	public void shouldReverse() {
		PathGroup g = PathGroup.of(LineTo.relative(1, 1), MoveTo.relative(4, 1), //
			LineTo.absolute(0, 0));
		assertPath(g, "l1,1 m4,1 L0,0");
		assertPath(g.reverse(), "L0,0 m-4,-1 l-1,-1");
	}

}
