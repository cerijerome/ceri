package ceri.common.geom;

import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Ratio2d;

public class Dimension2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Dimension2d d0 = Dimension2d.of(5, 10);
		Dimension2d d1 = Dimension2d.of(5, 10);
		Dimension2d d2 = Dimension2d.of(4.999, 10);
		Dimension2d d3 = Dimension2d.of(5, 10.001);
		exerciseEquals(d0, d1);
		assertNotEquals(d0, d2);
		assertNotEquals(d0, d3);
	}

	@Test
	public void shouldResize() {
		Dimension2d d = Dimension2d.of(5, 10);
		assertThat(d.resize(0.1), is(Dimension2d.of(0.5, 1)));
		assertThat(d.resize(Ratio2d.of(2, 0.5)), is(Dimension2d.of(10, 5)));
	}

	@Test
	public void shouldProvideAspectRatio() {
		assertThat(Dimension2d.of(100, 20).aspectRatio(), is(5.0));
		assertThat(Dimension2d.of(20, 100).aspectRatio(), is(0.2));
		assertThat(Dimension2d.of(0, 0).aspectRatio(), is(1.0));
		assertThat(Dimension2d.of(POSITIVE_INFINITY, POSITIVE_INFINITY).aspectRatio(), is(1.0));
		assertThat(Dimension2d.of(1, 0).aspectRatio(), is(POSITIVE_INFINITY));
		assertThat(Dimension2d.of(0, 1).aspectRatio(), is(0.0));
	}

}
