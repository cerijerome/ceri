package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class CylinderBehavior {
	private final Cylinder c0 = Cylinder.of(2, 8);
	private final Cylinder c1 = Cylinder.of(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(c0, Cylinder.of(2, 8));
		assertNotEquals(c0, Cylinder.of(1.9, 8));
		assertNotEquals(c0, Cylinder.of(2, 8.1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(Cylinder.of(0, 0), Cylinder.ZERO);
		GeomAssert.approx(Cylinder.ZERO, 0, 0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(c0.hFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromH(h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(c0.hFromVolume(101), Double.NaN);
		assertApprox(Cylinder.ZERO.hFromVolume(0), 0);
		assertApprox(Cylinder.ZERO.hFromVolume(1), Double.NaN);
		assertApprox(Cylinder.of(0, 1).hFromVolume(1), Double.NaN);
		assertApprox(Cylinder.of(1, 0).hFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldReturnRadiusFromHeight() {
		assertApprox(c0.radiusFromH(-1), Double.NaN);
		assertApprox(c0.radiusFromH(0), 2);
		assertApprox(c0.radiusFromH(4), 2);
		assertApprox(c0.radiusFromH(8), 2);
		assertApprox(c0.radiusFromH(9), Double.NaN);
	}

	@Test
	public void shouldCalculateGradientAtHeight() {
		assertApprox(c0.gradientAtH(0), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtH(8), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtH(-1), Double.NaN);
		assertApprox(c0.gradientAtH(9), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeFromH(-1), 0);
		assertApprox(c0.volumeFromH(0), 0);
		assertApprox(c0.volumeFromH(4), 50.265);
		assertApprox(c0.volumeFromH(8), 100.531);
		assertApprox(c0.volumeFromH(9), 100.531);
		assertApprox(Cylinder.ZERO.volumeFromH(0), 0);
		assertApprox(Cylinder.ZERO.volumeFromH(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 100.531);
		assertApprox(c1.volume(), 50.265);
		assertApprox(Cylinder.ZERO.volume(), 0);
	}
}
