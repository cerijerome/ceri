package ceri.common.geom;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class ConcaveSpheroidBehavior {
	private final ConcaveSpheroid s0 = ConcaveSpheroid.of(3, 1, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(s0, ConcaveSpheroid.of(3, 1, 2));
		Assert.notEqual(s0, ConcaveSpheroid.of(3.1, 1, 2));
		Assert.notEqual(s0, ConcaveSpheroid.of(3, 0.9, 2));
		Assert.notEqual(s0, ConcaveSpheroid.of(3, 1, 1.9));
	}

	@Test
	public void shouldFailForInvalidAxes() {
		Assert.thrown(() -> ConcaveSpheroid.of(-0.1, 1, 1));
		Assert.thrown(() -> ConcaveSpheroid.of(0, 1, 1));
		Assert.thrown(() -> ConcaveSpheroid.of(2, -0.1, 1));
		Assert.thrown(() -> ConcaveSpheroid.of(2, 2.1, 1));
		Assert.thrown(() -> ConcaveSpheroid.of(2, 2, -1));
	}

	@Test
	public void shouldDefineNull() {
		Assert.equal(ConcaveSpheroid.of(0, 0, 0), ConcaveSpheroid.ZERO);
		Assert.approx(ConcaveSpheroid.ZERO.h(), 0);
		Assert.approx(ConcaveSpheroid.ZERO.r, 0);
		Assert.approx(ConcaveSpheroid.ZERO.c, 0);
		Assert.approx(ConcaveSpheroid.ZERO.volume(), 0);
		Assert.notEqual(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(0, 0, 1));
		Assert.notEqual(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 0, 0));
		Assert.notEqual(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 1, 0));
		Assert.notEqual(ConcaveSpheroid.ZERO, ConcaveSpheroid.of(1, 0, 1));
	}

	@Test
	public void shouldCalculateGradient() {
		Assert.approx(s0.gradientAtH(-1), NaN);
		Assert.approx(s0.gradientAtH(0), 0);
		Assert.approx(s0.gradientAtH(1), -3.464);
		Assert.approx(s0.gradientAtH(2), POSITIVE_INFINITY);
		Assert.approx(s0.gradientAtH(3), 3.464);
		Assert.approx(s0.gradientAtH(4), 0);
		Assert.approx(s0.gradientAtH(5), NaN);
		Assert.approx(ConcaveSpheroid.of(0, 0, 1).gradientAtH(0), 0);
		Assert.approx(ConcaveSpheroid.of(0, 0, 1).gradientAtH(1), POSITIVE_INFINITY);
		Assert.approx(ConcaveSpheroid.of(0, 0, 1).gradientAtH(2), 0);
		Assert.approx(ConcaveSpheroid.of(1, 0, 0).gradientAtH(0), NaN);
		Assert.approx(ConcaveSpheroid.of(1, 1, 0).gradientAtH(0), 0);
		Assert.approx(ConcaveSpheroid.of(1, 0, 1).gradientAtH(0), 0);
		Assert.approx(ConcaveSpheroid.of(1, 0, 1).gradientAtH(1), POSITIVE_INFINITY);
	}

	@Test
	public void shouldCalculateVolume() {
		Assert.approx(s0.volume(), 62.257);
		Assert.approx(ConcaveSpheroid.volume(3, 1, 2), 62.257);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		Assert.approx(s0.hFromVolume(-1), NaN);
		Assert.approx(s0.hFromVolume(0), 0);
		Assert.approx(s0.hFromVolume(25), 1.517);
		Assert.approx(s0.hFromVolume(s0.volume() / 2), 2);
		Assert.approx(s0.hFromVolume(50), 3.378);
		Assert.approx(s0.hFromVolume(s0.volume()), 4);
		Assert.approx(s0.hFromVolume(63), NaN);
	}

	@Test
	public void shouldCalculateVolumeFromHeight() {
		Assert.approx(s0.volumeFromH(-1), 0);
		Assert.approx(s0.volumeFromH(0), 0);
		Assert.approx(s0.volumeFromH(1), 18.006);
		Assert.approx(s0.volumeFromH(2), 31.129);
		Assert.approx(s0.volumeFromH(3), 44.251);
		Assert.approx(s0.volumeFromH(4), 62.257);
		Assert.approx(s0.volumeFromH(5), 62.257);
	}

	@Test
	public void shouldCalulateRadiusFromHeight() {
		Assert.approx(s0.radiusFromH(-1), NaN);
		Assert.approx(s0.radiusFromH(0), 3);
		Assert.approx(s0.radiusFromH(1), 2.134);
		Assert.approx(s0.radiusFromH(2), 2);
		Assert.approx(s0.radiusFromH(3), 2.134);
		Assert.approx(s0.radiusFromH(4), 3);
		Assert.approx(s0.radiusFromH(5), NaN);
	}

}
