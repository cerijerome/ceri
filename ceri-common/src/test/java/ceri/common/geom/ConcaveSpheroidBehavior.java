package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ConcaveSpheroidBehavior {
	private final ConcaveSpheroid s0 = ConcaveSpheroid.of(3, 1, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(s0, ConcaveSpheroid.of(3, 1, 2));
		assertNotEquals(s0, ConcaveSpheroid.of(3.1, 1, 2));
		assertNotEquals(s0, ConcaveSpheroid.of(3, 0.9, 2));
		assertNotEquals(s0, ConcaveSpheroid.of(3, 1, 1.9));
	}

	@Test
	public void shouldFailForInvalidAxes() {
		assertThrown(() -> ConcaveSpheroid.of(-0.1, 1, 1));
		assertThrown(() -> ConcaveSpheroid.of(0, 1, 1));
		assertThrown(() -> ConcaveSpheroid.of(2, -0.1, 1));
		assertThrown(() -> ConcaveSpheroid.of(2, 2.1, 1));
		assertThrown(() -> ConcaveSpheroid.of(2, 2, -1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(ConcaveSpheroid.of(0, 0, 0), ConcaveSpheroid.ZERO);
		assertApprox(ConcaveSpheroid.ZERO.h(), 0);
		assertApprox(ConcaveSpheroid.ZERO.r, 0);
		assertApprox(ConcaveSpheroid.ZERO.c, 0);
		assertApprox(ConcaveSpheroid.ZERO.volume(), 0);
		assertNotEquals(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(0, 0, 1));
		assertNotEquals(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 0, 0));
		assertNotEquals(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 1, 0));
		assertNotEquals(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 0, 1));
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(s0.gradientAtH(-1), NaN);
		assertApprox(s0.gradientAtH(0), 0);
		assertApprox(s0.gradientAtH(1), -3.464);
		assertApprox(s0.gradientAtH(2), POSITIVE_INFINITY);
		assertApprox(s0.gradientAtH(3), 3.464);
		assertApprox(s0.gradientAtH(4), 0);
		assertApprox(s0.gradientAtH(5), NaN);
		assertApprox(ConcaveSpheroid.of(0, 0, 1).gradientAtH(0), 0);
		assertApprox(ConcaveSpheroid.of(0, 0, 1).gradientAtH(1), POSITIVE_INFINITY);
		assertApprox(ConcaveSpheroid.of(0, 0, 1).gradientAtH(2), 0);
		assertApprox(ConcaveSpheroid.of(1, 0, 0).gradientAtH(0), NaN);
		assertApprox(ConcaveSpheroid.of(1, 1, 0).gradientAtH(0), 0);
		assertApprox(ConcaveSpheroid.of(1, 0, 1).gradientAtH(0), 0);
		assertApprox(ConcaveSpheroid.of(1, 0, 1).gradientAtH(1), POSITIVE_INFINITY);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(s0.volume(), 62.257);
		assertApprox(ConcaveSpheroid.volume(3, 1, 2), 62.257);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(s0.hFromVolume(-1), NaN);
		assertApprox(s0.hFromVolume(0), 0);
		assertApprox(s0.hFromVolume(25), 1.517);
		assertApprox(s0.hFromVolume(s0.volume() / 2), 2);
		assertApprox(s0.hFromVolume(50), 3.378);
		assertApprox(s0.hFromVolume(s0.volume()), 4);
		assertApprox(s0.hFromVolume(63), NaN);
	}

	@Test
	public void shouldCalculateVolumeFromHeight() {
		assertApprox(s0.volumeFromH(-1), 0);
		assertApprox(s0.volumeFromH(0), 0);
		assertApprox(s0.volumeFromH(1), 18.006);
		assertApprox(s0.volumeFromH(2), 31.129);
		assertApprox(s0.volumeFromH(3), 44.251);
		assertApprox(s0.volumeFromH(4), 62.257);
		assertApprox(s0.volumeFromH(5), 62.257);
	}

	@Test
	public void shouldCalulateRadiusFromHeight() {
		assertApprox(s0.radiusFromH(-1), NaN);
		assertApprox(s0.radiusFromH(0), 3);
		assertApprox(s0.radiusFromH(1), 2.134);
		assertApprox(s0.radiusFromH(2), 2);
		assertApprox(s0.radiusFromH(3), 2.134);
		assertApprox(s0.radiusFromH(4), 3);
		assertApprox(s0.radiusFromH(5), NaN);
	}

}
