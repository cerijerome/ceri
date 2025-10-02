package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
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
		assertEquals(Cylinder.of(0, 0), Cylinder.NULL);
		assertEquals(Cylinder.NULL.height(), 0.0);
		assertEquals(Cylinder.NULL.radius(), 0.0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(c0.heightFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromHeight(h);
			assertApprox(c0.heightFromVolume(v), h);
		}
		assertApprox(c0.heightFromVolume(101), Double.NaN);
		assertApprox(Cylinder.NULL.heightFromVolume(0), 0);
		assertApprox(Cylinder.NULL.heightFromVolume(1), Double.NaN);
		assertApprox(Cylinder.of(0, 1).heightFromVolume(1), Double.NaN);
		assertApprox(Cylinder.of(1, 0).heightFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldReturnRadiusFromHeight() {
		assertApprox(c0.radiusFromHeight(-1), Double.NaN);
		assertApprox(c0.radiusFromHeight(0), 2);
		assertApprox(c0.radiusFromHeight(4), 2);
		assertApprox(c0.radiusFromHeight(8), 2);
		assertApprox(c0.radiusFromHeight(9), Double.NaN);
	}

	@Test
	public void shouldCalculateGradientAtHeight() {
		assertApprox(c0.gradientAtHeight(0), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtHeight(8), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtHeight(-1), Double.NaN);
		assertApprox(c0.gradientAtHeight(9), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeFromHeight(-1), 0);
		assertApprox(c0.volumeFromHeight(0), 0);
		assertApprox(c0.volumeFromHeight(4), 50.265);
		assertApprox(c0.volumeFromHeight(8), 100.531);
		assertApprox(c0.volumeFromHeight(9), 100.531);
		assertApprox(Cylinder.NULL.volumeFromHeight(0), 0);
		assertApprox(Cylinder.NULL.volumeFromHeight(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 100.531);
		assertApprox(c1.volume(), 50.265);
		assertApprox(Cylinder.NULL.volume(), 0);
	}
}
