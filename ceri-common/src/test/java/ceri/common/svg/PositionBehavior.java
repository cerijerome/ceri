package ceri.common.svg;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.geom.Point2d;

public class PositionBehavior {

	@BeforeClass
	public static void init() {
		exerciseEnum(PositionType.class);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		Position p = Position.absolute(1, -1);
		Position eq0 = Position.absolute(1, -1);
		Position eq1 = Position.absolute(Point2d.of(1, -1));
		Position ne0 = Position.relative(1, -1);
		Position ne1 = Position.relative(Point2d.of(1, -1));
		Position ne2 = Position.absolute(1.1, -1);
		Position ne3 = Position.absolute(1, 1);
		exerciseEquals(p, eq0, eq1);
		assertAllNotEqual(p, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCombinePositions() {
		Position p0 = Position.absolute(1, -1);
		Position p1 = Position.absolute(2, 0);
		Position p2 = Position.relative(1, 1);
		assertThat(p0.combine(null), is(p0));
		assertThat(p0.combine(p1), is(p1));
		assertThat(p1.combine(p0), is(p0));
		assertThat(p0.combine(p2), is(Position.absolute(2, 0)));
		assertThat(p2.combine(p0), is(p0));
	}

	@Test
	public void shouldCalculateVector() {
		Position p = Position.relative(-10, 100);
		assertThat(p.vector(), is(Point2d.of(-10, 100)));
	}

}
