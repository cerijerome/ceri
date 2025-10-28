package ceri.common.geom;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class TruncatedRadial3dBehavior {
	private final Cone c0 = Cone.of(1, 4);
	private final Spheroid3d s0 = Spheroid3d.of(4, 2);
	private final TruncatedRadial3d<Cone> t0 = TruncatedRadial3d.of(c0, 1, 2);
	private final TruncatedRadial3d<Spheroid3d> t1 = TruncatedRadial3d.of(s0, 0, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		Testing.exerciseEquals(t0, TruncatedRadial3d.of(Cone.of(1, 4), 1, 2));
		Assert.notEqual(t0, TruncatedRadial3d.of(Cone.of(1.1, 4), 1, 2));
		Assert.notEqual(t0, TruncatedRadial3d.of(Cone.of(1, 3.9), 1, 2));
		Assert.notEqual(t0, TruncatedRadial3d.of(Cone.of(1, 4), 0.9, 2));
		Assert.notEqual(t0, TruncatedRadial3d.of(Cone.of(1, 4), 1, 2.1));
	}

	@Test
	public void shouldExposeWrappedRadial3d() {
		Assert.equal(t0.wrapped(), c0);
		Assert.equal(t1.wrapped(), s0);
	}

	@Test
	public void shouldTruncateRadiusFromHeight() {
		Assert.approx(t0.radiusFromH(-1), NaN);
		Assert.approx(t0.radiusFromH(0), 0.25);
		Assert.approx(t0.radiusFromH(1), 0.5);
		Assert.approx(t0.radiusFromH(2), 0.75);
		Assert.approx(t0.radiusFromH(3), NaN);
		Assert.approx(t1.radiusFromH(-1), NaN);
		Assert.approx(t1.radiusFromH(0), 0);
		Assert.approx(t1.radiusFromH(1), 3.464);
		Assert.approx(t1.radiusFromH(2), 4);
		Assert.approx(t1.radiusFromH(3), NaN);
	}

	@Test
	public void shouldTruncateHeightFromVolume() {
		Assert.approx(t0.hFromVolume(-1), NaN);
		Assert.approx(t0.hFromVolume(0), 0);
		Assert.approx(t0.hFromVolume(1), 1.534);
		Assert.approx(t0.hFromVolume(1.5), 1.881);
		Assert.approx(t0.hFromVolume(t0.volume()), 2);
		Assert.approx(t0.hFromVolume(2), NaN);
		Assert.approx(t1.hFromVolume(-1), NaN);
		Assert.approx(t1.hFromVolume(0), 0);
		Assert.approx(t1.hFromVolume(30), 1.225);
		Assert.approx(t1.hFromVolume(60), 1.860);
		Assert.approx(t1.hFromVolume(t1.volume()), 2);
		Assert.approx(t1.hFromVolume(68), NaN);
	}

	@Test
	public void shouldTruncateVolumeFromHeight() {
		Assert.approx(t0.volumeFromH(-1), 0);
		Assert.approx(t0.volumeFromH(0), 0);
		Assert.approx(t0.volumeFromH(1), 0.458);
		Assert.approx(t0.volumeFromH(2), 1.702);
		Assert.approx(t0.volumeFromH(3), 1.702);
		Assert.approx(t1.volumeFromH(-1), 0);
		Assert.approx(t1.volumeFromH(0), 0);
		Assert.approx(t1.volumeFromH(1), 20.944);
		Assert.approx(t1.volumeFromH(2), 67.021);
		Assert.approx(t1.volumeFromH(3), 67.021);
	}

	@Test
	public void shouldTruncateGradientAtHeight() {
		Assert.approx(t0.gradientAtH(-1), NaN);
		Assert.approx(t0.gradientAtH(0), 4);
		Assert.approx(t0.gradientAtH(2), 4);
		Assert.approx(t0.gradientAtH(3), NaN);
		Assert.approx(t1.gradientAtH(-1), NaN);
		Assert.approx(t1.gradientAtH(0), 0);
		Assert.approx(t1.gradientAtH(1), 0.866);
		Assert.approx(t1.gradientAtH(2), NEGATIVE_INFINITY);
		Assert.approx(t1.gradientAtH(3), NaN);
	}

	@Test
	public void shouldTruncateHeight() {
		Assert.approx(t0.h(), 2);
		Assert.approx(t1.h(), 2);
	}

	@Test
	public void shouldTruncateVolume() {
		Assert.approx(t0.volume(), 1.702);
		Assert.approx(t1.volume(), 67.021);
	}
}
