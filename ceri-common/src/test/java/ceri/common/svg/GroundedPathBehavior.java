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

public class GroundedPathBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Position pos = Position.relative(2, 1);
		LineTo line = LineTo.absolute(-1, -2);
		GroundedPath<LineTo> p = GroundedPath.of(pos, line);
		GroundedPath<LineTo> eq0 = GroundedPath.of(pos, line);
		GroundedPath<MoveTo> ne0 = GroundedPath.of(pos, MoveTo.absolute(-1, -2));
		GroundedPath<LineTo> ne1 = GroundedPath.of(Position.absolute(2, 1), line);
		GroundedPath<LineTo> ne2 = GroundedPath.of(Position.relative(1, 1), line);
		GroundedPath<LineTo> ne3 = GroundedPath.of(pos, LineTo.relative(-1, -2));
		GroundedPath<LineTo> ne4 = GroundedPath.of(pos, LineTo.absolute(-1, -1));
		exerciseEquals(p, eq0);
		assertAllNotEqual(p, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldMove() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.relative(1, 1), MoveTo.relative(2, -2));
		assertPath(p, "m1,1 m2,-2");
		assertPath(p.move(Position.absolute(-1, -1)), "M0,0 m2,-2");
	}

	@Test
	public void shouldDetermineEnd() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		assertThat(p.end(), is(Position.absolute(3, -1)));
	}

	@Test
	public void shouldReflect() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		Line2d line = Line2d.of(-1, 0, 1, 0);
		assertPath(p, "M1,1 m2,-2");
		assertPath(p.reflect(line), "M1,-1 m2,2");
	}

	@Test
	public void shouldScale() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		assertPath(p, "M1,1 m2,-2");
		assertPath(p.scale(Ratio2d.uniform(0.5)), "M0.5,0.5 m1,-1");
	}

	@Test
	public void shouldTranslate() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		assertPath(p, "M1,1 m2,-2");
		assertPath(p.translate(Point2d.of(-1, -1)), "M0,0 m2,-2");
	}

	@Test
	public void shouldReverse() {
		GroundedPath<MoveTo> p = GroundedPath.of(Position.absolute(1, 1), MoveTo.relative(2, -2));
		assertPath(p, "M1,1 m2,-2");
		assertPath(p.reverse(), "M3,-1 m-2,2");
	}

}
