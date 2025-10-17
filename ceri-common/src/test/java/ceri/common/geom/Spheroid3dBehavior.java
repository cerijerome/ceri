package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Spheroid3dBehavior {
	private final Spheroid3d s0 = Spheroid3d.of(2, 3);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(s0, Spheroid3d.of(2, 3));
		assertNotEquals(s0, Spheroid3d.of(2.1, 3));
		assertNotEquals(s0, Spheroid3d.of(2, 2.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertThrown(() -> Spheroid3d.of(-0.1, 1));
		assertThrown(() -> Spheroid3d.of(2, -1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(Spheroid3d.of(0, 0), Spheroid3d.NULL);
		assertApprox(Spheroid3d.NULL.h(), 0);
		assertApprox(Spheroid3d.NULL.r, 0);
		assertApprox(Spheroid3d.NULL.c, 0);
		assertApprox(Spheroid3d.NULL.volume(), 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(s0.gradientAtH(-1), NaN);
		assertApprox(s0.gradientAtH(0), 0);
		assertApprox(s0.gradientAtH(1), 1.677);
		assertApprox(s0.gradientAtH(3), NEGATIVE_INFINITY);
		assertApprox(s0.gradientAtH(4), -4.243);
		assertApprox(s0.gradientAtH(6), 0);
		assertApprox(s0.gradientAtH(7), NaN);
		assertApprox(Spheroid3d.of(0, 1).gradientAtH(0), 0);
		assertApprox(Spheroid3d.of(0, 1).gradientAtH(1), NEGATIVE_INFINITY);
		assertApprox(Spheroid3d.of(1, 0).gradientAtH(0), 0);
		assertApprox(Spheroid3d.of(1, 0).gradientAtH(1), NaN);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(s0.volume(), 50.265);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(s0.hFromVolume(-1), NaN);
		assertApprox(s0.hFromVolume(0), 0);
		assertApprox(s0.hFromVolume(25), 2.989);
		assertApprox(s0.hFromVolume(50), 5.745);
		assertApprox(s0.hFromVolume(51), NaN);
	}

	@Test
	public void shouldCalulateRadiusFromHeight() {
		assertApprox(s0.radiusFromH(-1), NaN);
		assertApprox(s0.radiusFromH(0), 0);
		assertApprox(s0.radiusFromH(3), 2);
		assertApprox(s0.radiusFromH(6), 0);
		assertApprox(s0.radiusFromH(7), NaN);
	}

	@Test
	public void shouldCalculateVolumeFromHeight() {
		assertApprox(s0.volumeFromH(-1), 0);
		assertApprox(s0.volumeFromH(0), 0);
		assertApprox(s0.volumeFromH(3), 25.133);
		assertApprox(s0.volumeFromH(6), 50.265);
		assertApprox(s0.volumeFromH(7), 50.265);
	}

}
