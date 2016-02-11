package ceri.common.math;

import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Dimension2dBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Dimension2d d0 = new Dimension2d(5, 10);
		Dimension2d d1 = new Dimension2d(5, 10);
		Dimension2d d2 = new Dimension2d(4.999, 10);
		Dimension2d d3 = new Dimension2d(5, 10.001);
		exerciseEquals(d0, d1);
		assertNotEquals(d0, d2);
		assertNotEquals(d0, d3);
	}

	@Test
	public void shouldResize() {
		Dimension2d d = new Dimension2d(5, 10);
		assertThat(d.resize(0.1), is(new Dimension2d(0.5, 1)));
		assertThat(d.resize(new Ratio2d(2, 0.5)), is(new Dimension2d(10, 5)));
	}

	@Test
	public void shouldProvideAspectRatio() {
		assertThat(new Dimension2d(100, 20).aspectRatio(), is(5.0));
		assertThat(new Dimension2d(20, 100).aspectRatio(), is(0.2));
		assertThat(new Dimension2d(0, 0).aspectRatio(), is(1.0));
		assertThat(new Dimension2d(POSITIVE_INFINITY, POSITIVE_INFINITY).aspectRatio(), is(1.0));
		assertThat(new Dimension2d(1, 0).aspectRatio(), is(POSITIVE_INFINITY));
		assertThat(new Dimension2d(0, 1).aspectRatio(), is(0.0));
	}

}
