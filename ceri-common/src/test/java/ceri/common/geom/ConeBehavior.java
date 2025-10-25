package ceri.common.geom;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class ConeBehavior {
	private final Cone c0 = Cone.of(2, 8);
	private final Cone c1 = Cone.of(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(c0, Cone.of(2, 8));
		assertNotEquals(c0, Cone.of(1.9, 8));
		assertNotEquals(c0, Cone.of(2, 8.1));
		assertNotEquals(c0, Cone.of(2, 0));
		assertNotEquals(c0, Cone.of(0, 8));
	}

	@Test
	public void shouldDefineNullCone() {
		assertEquals(Cone.of(0, 0), Cone.ZERO);
		Assert.approx(Cone.ZERO.gradient(), Double.NaN);
		GeomAssert.approx(Cone.ZERO, 0, 0);
	}

	@Test
	public void shouldCalculateGradient() {
		Assert.approx(c0.gradient(), 4);
		Assert.approx(c0.gradientAtH(-1), Double.NaN);
		Assert.approx(c0.gradientAtH(0), 4);
		Assert.approx(c0.gradientAtH(8), 4);
		Assert.approx(c0.gradientAtH(9), Double.NaN);
		Assert.approx(c1.gradient(), 0.25);
		Assert.approx(Cone.ZERO.gradient(), Double.NaN);
	}

	@Test
	public void shouldCalculateHeightFromRadius() {
		Assert.approx(c0.heightFromRadius(-1), Double.NaN);
		Assert.approx(c0.heightFromRadius(0), 0);
		Assert.approx(c0.heightFromRadius(1), 4);
		Assert.approx(c0.heightFromRadius(2), 8);
		Assert.approx(c0.heightFromRadius(3), Double.NaN);
		Assert.approx(c1.heightFromRadius(0), 0);
		Assert.approx(c1.heightFromRadius(2), 0.5);
		Assert.approx(c1.heightFromRadius(4), 1);
		Assert.approx(Cone.ZERO.heightFromRadius(0), 0);
		Assert.approx(Cone.ZERO.heightFromRadius(1), Double.NaN);
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		Assert.approx(c0.radiusFromH(-1), Double.NaN);
		Assert.approx(c0.radiusFromH(0), 0);
		Assert.approx(c0.radiusFromH(4), 1);
		Assert.approx(c0.radiusFromH(8), 2);
		Assert.approx(c0.radiusFromH(9), Double.NaN);
		Assert.approx(c1.radiusFromH(0), 0);
		Assert.approx(c1.radiusFromH(0.5), 2);
		Assert.approx(c1.radiusFromH(1), 4);
		Assert.approx(Cone.ZERO.radiusFromH(0), 0);
		Assert.approx(Cone.ZERO.radiusFromH(1), Double.NaN);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		Assert.approx(c0.hFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromH(h);
			Assert.approx(c0.hFromVolume(v), h);
		}
		Assert.approx(c0.hFromVolume(34), Double.NaN);
		Assert.approx(Cone.ZERO.hFromVolume(0), 0);
		Assert.approx(Cone.ZERO.hFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		Assert.approx(c0.volumeFromH(-1), 0);
		Assert.approx(c0.volumeFromH(1), 0.065);
		Assert.approx(c0.volumeFromH(7), 22.449);
		Assert.approx(c0.volumeFromH(8), 33.510);
		Assert.approx(c0.volumeFromH(9), 33.510);
		Assert.approx(Cone.ZERO.volumeFromH(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		Assert.approx(c0.volume(), 33.510);
		Assert.approx(c1.volume(), 16.755);
		Assert.approx(Cone.ZERO.volume(), 0);
	}
}
