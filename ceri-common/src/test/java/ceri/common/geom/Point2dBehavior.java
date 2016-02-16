package ceri.common.geom;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Point2dBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Point2d p0 = new Point2d(5, 10);
		Point2d p1 = new Point2d(5, 10);
		Point2d p2 = new Point2d(4.999, 10);
		Point2d p3 = new Point2d(5, 10.001);
		exerciseEquals(p0, p1);
		assertNotEquals(p0, p2);
		assertNotEquals(p0, p3);
	}

	@Test
	public void shouldTranslateCoordinates() {
		Point2d p0 = new Point2d(5, -10);
		Point2d p1 = new Point2d(1, 1);
		Point2d p2 = new Point2d(0, 0);
		Point2d p3 = new Point2d(-5, 10);
		assertThat(p0.translate(p1), is(new Point2d(6, -9)));
		assertThat(p0.translate(p2), is(new Point2d(5, -10)));
		assertThat(p0.translate(p3), is(new Point2d(0, 0)));
	}

	@Test
	public void shouldScaleCoordinates() {
		Point2d p0 = new Point2d(-5, 10);
		Ratio2d r0 = new Ratio2d(2, 0.5);
		Ratio2d r1 = Ratio2d.create(0.1);
		assertThat(p0.scale(r0), is(new Point2d(-10, 5)));
		assertThat(p0.scale(r1), is(new Point2d(-0.5, 1)));
		assertThat(p0.scale(Ratio2d.ONE), is(p0));
		assertThat(p0.scale(Ratio2d.ZERO), is(Point2d.ZERO));
	}

}
