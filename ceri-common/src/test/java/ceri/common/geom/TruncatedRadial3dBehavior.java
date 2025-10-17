package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class TruncatedRadial3dBehavior {
	private final Cone c0 = Cone.of(1, 4);
	private final Spheroid3d s0 = Spheroid3d.of(4, 2);
	private final TruncatedRadial3d<Cone> t0 = TruncatedRadial3d.of(c0, 1, 2);
	private final TruncatedRadial3d<Spheroid3d> t1 = TruncatedRadial3d.of(s0, 0, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(t0, TruncatedRadial3d.of(Cone.of(1, 4), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.of(Cone.of(1.1, 4), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.of(Cone.of(1, 3.9), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.of(Cone.of(1, 4), 0.9, 2));
		assertNotEquals(t0, TruncatedRadial3d.of(Cone.of(1, 4), 1, 2.1));
	}

	@Test
	public void shouldExposeWrappedRadial3d() {
		assertEquals(t0.wrapped(), c0);
		assertEquals(t1.wrapped(), s0);
	}

	@Test
	public void shouldTruncateRadiusFromHeight() {
		assertApprox(t0.radiusFromH(-1), NaN);
		assertApprox(t0.radiusFromH(0), 0.25);
		assertApprox(t0.radiusFromH(1), 0.5);
		assertApprox(t0.radiusFromH(2), 0.75);
		assertApprox(t0.radiusFromH(3), NaN);
		assertApprox(t1.radiusFromH(-1), NaN);
		assertApprox(t1.radiusFromH(0), 0);
		assertApprox(t1.radiusFromH(1), 3.464);
		assertApprox(t1.radiusFromH(2), 4);
		assertApprox(t1.radiusFromH(3), NaN);
	}

	@Test
	public void shouldTruncateHeightFromVolume() {
		assertApprox(t0.hFromVolume(-1), NaN);
		assertApprox(t0.hFromVolume(0), 0);
		assertApprox(t0.hFromVolume(1), 1.534);
		assertApprox(t0.hFromVolume(1.5), 1.881);
		assertApprox(t0.hFromVolume(t0.volume()), 2);
		assertApprox(t0.hFromVolume(2), NaN);
		assertApprox(t1.hFromVolume(-1), NaN);
		assertApprox(t1.hFromVolume(0), 0);
		assertApprox(t1.hFromVolume(30), 1.225);
		assertApprox(t1.hFromVolume(60), 1.860);
		assertApprox(t1.hFromVolume(t1.volume()), 2);
		assertApprox(t1.hFromVolume(68), NaN);
	}

	@Test
	public void shouldTruncateVolumeFromHeight() {
		assertApprox(t0.volumeFromH(-1), 0);
		assertApprox(t0.volumeFromH(0), 0);
		assertApprox(t0.volumeFromH(1), 0.458);
		assertApprox(t0.volumeFromH(2), 1.702);
		assertApprox(t0.volumeFromH(3), 1.702);
		assertApprox(t1.volumeFromH(-1), 0);
		assertApprox(t1.volumeFromH(0), 0);
		assertApprox(t1.volumeFromH(1), 20.944);
		assertApprox(t1.volumeFromH(2), 67.021);
		assertApprox(t1.volumeFromH(3), 67.021);
	}

	@Test
	public void shouldTruncateGradientAtHeight() {
		assertApprox(t0.gradientAtH(-1), NaN);
		assertApprox(t0.gradientAtH(0), 4);
		assertApprox(t0.gradientAtH(2), 4);
		assertApprox(t0.gradientAtH(3), NaN);
		assertApprox(t1.gradientAtH(-1), NaN);
		assertApprox(t1.gradientAtH(0), 0);
		assertApprox(t1.gradientAtH(1), 0.866);
		assertApprox(t1.gradientAtH(2), NEGATIVE_INFINITY);
		assertApprox(t1.gradientAtH(3), NaN);
	}

	@Test
	public void shouldTruncateHeight() {
		assertApprox(t0.h(), 2);
		assertApprox(t1.h(), 2);
	}

	@Test
	public void shouldTruncateVolume() {
		assertApprox(t0.volume(), 1.702);
		assertApprox(t1.volume(), 67.021);
	}
}
