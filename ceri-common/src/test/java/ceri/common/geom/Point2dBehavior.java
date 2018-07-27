package ceri.common.geom;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class Point2dBehavior {

	@Test
	public void shouldNotAllowNaN() {
		assertTrue(Point2d.NULL.isNull());
		assertException(() -> Point2d.of(Double.NaN, 0));
		assertException(() -> Point2d.of(0, Double.NaN));
		assertFalse(Point2d.X_UNIT.isNull());
	}

	@Test
	public void shouldNonBreachEqualsContract() {
		Point2d p0 = Point2d.of(5, 10);
		Point2d p1 = Point2d.of(5, 10);
		Point2d p2 = Point2d.of(4.999, 10);
		Point2d p3 = Point2d.of(5, 10.001);
		exerciseEquals(p0, p1);
		assertNotEquals(p0, p2);
		assertNotEquals(p0, p3);
	}

	@Test
	public void shouldReverse() {
		assertThat(Point2d.ZERO.reverse(), is(Point2d.ZERO));
		assertThat(Point2d.of(9.99, -7.77).reverse(), is(Point2d.of(-9.99, 7.77)));
	}

	@Test
	public void shouldCalculateToAndFrom() {
		assertThat(Point2d.of(10, 5).to(Point2d.of(0, -1)), is(Point2d.of(-10, -6)));
		assertThat(Point2d.of(-10, -6).from(Point2d.of(0, -1)), is(Point2d.of(-10, -5)));
	}

	@Test
	public void shouldTranslateCoordinates() {
		Point2d p0 = Point2d.of(5, -10);
		Point2d p1 = Point2d.of(1, 1);
		Point2d p2 = Point2d.of(0, 0);
		Point2d p3 = Point2d.of(-5, 10);
		assertThat(p0.translate(p1), is(Point2d.of(6, -9)));
		assertThat(p0.translate(p2), is(Point2d.of(5, -10)));
		assertThat(p0.translate(p3), is(Point2d.of(0, 0)));
	}

	@Test
	public void shouldScaleCoordinates() {
		Point2d p0 = Point2d.of(-5, 10);
		Ratio2d r0 = Ratio2d.of(2, 0.5);
		Ratio2d r1 = Ratio2d.uniform(0.1);
		assertThat(p0.scale(r0), is(Point2d.of(-10, 5)));
		assertThat(p0.scale(r1), is(Point2d.of(-0.5, 1)));
		assertThat(p0.scale(Ratio2d.ONE), is(p0));
		assertThat(p0.scale(Ratio2d.ZERO), is(Point2d.ZERO));
	}

}
