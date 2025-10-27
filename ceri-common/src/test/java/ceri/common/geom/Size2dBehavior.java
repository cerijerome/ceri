package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class Size2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var d0 = Size2d.of(5, 10);
		var d1 = Size2d.of(5, 10);
		var d2 = Size2d.of(4.999, 10);
		var d3 = Size2d.of(5, 10.001);
		TestUtil.exerciseEquals(d0, d1);
		Assert.notEqualAll(d0, d2, d3);
	}

	@Test
	public void shouldDetermineIfZero() {
		Assert.equal(Size2d.ZERO.isZero(), true);
		Assert.equal(Size2d.of(0, 1).isZero(), false);
	}

	@Test
	public void shouldCalculateArea() {
		Assert.equal(Size2d.ZERO.area(), 0.0);
		Assert.equal(Size2d.of(2, 4).area(), 8.0);
	}

	@Test
	public void shouldResize() {
		var d = Size2d.of(5, 10);
		GeomAssert.approx(d.resize(1), 5, 10);
		GeomAssert.approx(d.resize(1, 0.5), 5, 5);
		GeomAssert.approx(d.resize(0.5, 1), 2.5, 10);
		GeomAssert.approx(d.resize(0.1), 0.5, 1);
		GeomAssert.approx(d.resize(Ratio2d.of(2, 0.5)), 10, 5);
	}

	@Test
	public void shouldProvideAspectRatio() {
		Assert.equal(Size2d.of(100, 20).aspectRatio(), 5.0);
		Assert.equal(Size2d.of(20, 100).aspectRatio(), 0.2);
		Assert.equal(Size2d.of(0, 0).aspectRatio(), 1.0);
		Assert.equal(Size2d.of(1, 0).aspectRatio(), Double.POSITIVE_INFINITY);
		Assert.equal(Size2d.of(0, 1).aspectRatio(), 0.0);
	}
}
