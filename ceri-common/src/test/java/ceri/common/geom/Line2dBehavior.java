package ceri.common.geom;

import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

public class Line2dBehavior {
	private final Line2d l0 = Line2d.create(new Point2d(-1, 2), new Point2d(2, -1));
	private final Line2d l1 = Line2d.create(new Point2d(1, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(l0, Line2d.create(new Point2d(-1, 2), new Point2d(2, -1)));
		assertNotEquals(l0, Line2d.create(new Point2d(-1.1, 2), new Point2d(2, -1)));
		assertNotEquals(l0, Line2d.create(new Point2d(-1, 1.9), new Point2d(2, -1)));
		assertNotEquals(l0, Line2d.create(new Point2d(-1, 2), new Point2d(2.1, -1)));
		assertNotEquals(l0, Line2d.create(new Point2d(-1, 2), new Point2d(2, -0.9)));
	}

	@Test
	public void shouldScale() {
		assertThat(l0.scale(Ratio2d.create(1.5)), is(Line2d.create(new Point2d(-1.5, 3), new Point2d(
			3, -1.5))));
		assertThat(l0.scale(Ratio2d.ONE), is(l0));
		assertThat(l0.scale(Ratio2d.ZERO), is(Line2d.ZERO));
	}

	@Test
	public void shouldCalculateVector() {
		assertThat(l0.vector, is(new Point2d(3, -3)));
		assertThat(l1.vector, is(new Point2d(1, 3)));
	}

}
