package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import org.junit.Test;
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
		assertApprox(Cone.ZERO.gradient(), Double.NaN);
		GeomAssert.approx(Cone.ZERO, 0, 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(c0.gradient(), 4);
		assertApprox(c0.gradientAtH(-1), Double.NaN);
		assertApprox(c0.gradientAtH(0), 4);
		assertApprox(c0.gradientAtH(8), 4);
		assertApprox(c0.gradientAtH(9), Double.NaN);
		assertApprox(c1.gradient(), 0.25);
		assertApprox(Cone.ZERO.gradient(), Double.NaN);
	}

	@Test
	public void shouldCalculateHeightFromRadius() {
		assertApprox(c0.heightFromRadius(-1), Double.NaN);
		assertApprox(c0.heightFromRadius(0), 0);
		assertApprox(c0.heightFromRadius(1), 4);
		assertApprox(c0.heightFromRadius(2), 8);
		assertApprox(c0.heightFromRadius(3), Double.NaN);
		assertApprox(c1.heightFromRadius(0), 0);
		assertApprox(c1.heightFromRadius(2), 0.5);
		assertApprox(c1.heightFromRadius(4), 1);
		assertApprox(Cone.ZERO.heightFromRadius(0), 0);
		assertApprox(Cone.ZERO.heightFromRadius(1), Double.NaN);
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		assertApprox(c0.radiusFromH(-1), Double.NaN);
		assertApprox(c0.radiusFromH(0), 0);
		assertApprox(c0.radiusFromH(4), 1);
		assertApprox(c0.radiusFromH(8), 2);
		assertApprox(c0.radiusFromH(9), Double.NaN);
		assertApprox(c1.radiusFromH(0), 0);
		assertApprox(c1.radiusFromH(0.5), 2);
		assertApprox(c1.radiusFromH(1), 4);
		assertApprox(Cone.ZERO.radiusFromH(0), 0);
		assertApprox(Cone.ZERO.radiusFromH(1), Double.NaN);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(c0.hFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromH(h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(c0.hFromVolume(34), Double.NaN);
		assertApprox(Cone.ZERO.hFromVolume(0), 0);
		assertApprox(Cone.ZERO.hFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeFromH(-1), 0);
		assertApprox(c0.volumeFromH(1), 0.065);
		assertApprox(c0.volumeFromH(7), 22.449);
		assertApprox(c0.volumeFromH(8), 33.510);
		assertApprox(c0.volumeFromH(9), 33.510);
		assertApprox(Cone.ZERO.volumeFromH(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 33.510);
		assertApprox(c1.volume(), 16.755);
		assertApprox(Cone.ZERO.volume(), 0);
	}
}
