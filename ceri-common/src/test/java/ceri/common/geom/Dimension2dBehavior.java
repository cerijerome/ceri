package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static java.lang.Double.POSITIVE_INFINITY;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Dimension2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Dimension2d d0 = Dimension2d.of(5, 10);
		Dimension2d d1 = Dimension2d.of(5, 10);
		Dimension2d d2 = Dimension2d.of(4.999, 10);
		Dimension2d d3 = Dimension2d.of(5, 10.001);
		TestUtil.exerciseEquals(d0, d1);
		assertNotEquals(d0, d2);
		assertNotEquals(d0, d3);
	}

	@Test
	public void shouldResize() {
		Dimension2d d = Dimension2d.of(5, 10);
		assertEquals(d.resize(0.1), Dimension2d.of(0.5, 1));
		assertEquals(d.resize(Ratio2d.of(2, 0.5)), Dimension2d.of(10, 5));
	}

	@Test
	public void shouldProvideAspectRatio() {
		assertEquals(Dimension2d.of(100, 20).aspectRatio(), 5.0);
		assertEquals(Dimension2d.of(20, 100).aspectRatio(), 0.2);
		assertEquals(Dimension2d.of(0, 0).aspectRatio(), 1.0);
		assertEquals(Dimension2d.of(POSITIVE_INFINITY, POSITIVE_INFINITY).aspectRatio(), 1.0);
		assertEquals(Dimension2d.of(1, 0).aspectRatio(), POSITIVE_INFINITY);
		assertEquals(Dimension2d.of(0, 1).aspectRatio(), 0.0);
	}

}
