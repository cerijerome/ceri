package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Cone3dBehavior {
	private final Cone3d c0 = Cone3d.create(2, 8);
	private final Cone3d c1 = Cone3d.create(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(c0, Cone3d.create(2, 8));
		assertNotEquals(c0, Cone3d.create(1.9, 8));
		assertNotEquals(c0, Cone3d.create(2, 8.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertThrown(() -> Cone3d.create(-0.1, 2));
		assertThrown(() -> Cone3d.create(4, -0.1));
	}

	@Test
	public void shouldDefineNullCone() {
		assertEquals(Cone3d.create(0, 0), Cone3d.NULL);
		assertApprox(Cone3d.NULL.gradient(), Double.NaN);
		assertApprox(Cone3d.NULL.height(), 0);
		assertApprox(Cone3d.NULL.radius(), 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(c0.gradient(), 4);
		assertApprox(c0.gradientAtHeight(-1), Double.NaN);
		assertApprox(c0.gradientAtHeight(0), 4);
		assertApprox(c0.gradientAtHeight(8), 4);
		assertApprox(c0.gradientAtHeight(9), Double.NaN);
		assertApprox(c1.gradient(), 0.25);
		assertApprox(Cone3d.NULL.gradient(), Double.NaN);
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
		assertApprox(Cone3d.NULL.heightFromRadius(0), 0);
		assertApprox(Cone3d.NULL.heightFromRadius(1), Double.NaN);
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		assertApprox(c0.radiusFromHeight(-1), Double.NaN);
		assertApprox(c0.radiusFromHeight(0), 0);
		assertApprox(c0.radiusFromHeight(4), 1);
		assertApprox(c0.radiusFromHeight(8), 2);
		assertApprox(c0.radiusFromHeight(9), Double.NaN);
		assertApprox(c1.radiusFromHeight(0), 0);
		assertApprox(c1.radiusFromHeight(0.5), 2);
		assertApprox(c1.radiusFromHeight(1), 4);
		assertApprox(Cone3d.NULL.radiusFromHeight(0), 0);
		assertApprox(Cone3d.NULL.radiusFromHeight(1), Double.NaN);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(c0.heightFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromHeight(h);
			assertApprox(c0.heightFromVolume(v), h);
		}
		assertApprox(c0.heightFromVolume(34), Double.NaN);
		assertApprox(Cone3d.NULL.heightFromVolume(0), 0);
		assertApprox(Cone3d.NULL.heightFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeFromHeight(-1), 0);
		assertApprox(c0.volumeFromHeight(1), 0.065);
		assertApprox(c0.volumeFromHeight(7), 22.449);
		assertApprox(c0.volumeFromHeight(8), 33.510);
		assertApprox(c0.volumeFromHeight(9), 33.510);
		assertApprox(Cone3d.NULL.volumeFromHeight(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 33.510);
		assertApprox(c1.volume(), 16.755);
		assertApprox(Cone3d.NULL.volume(), 0);
	}

}
