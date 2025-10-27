package ceri.common.geom;

import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class InvertedRadial3dBehavior {
	private final Cone c0 = Cone.of(1, 4);
	private final Spheroid3d s0 = Spheroid3d.of(4, 2);
	private final InvertedRadial3d<Cone> i0 = InvertedRadial3d.of(c0);
	private final InvertedRadial3d<Spheroid3d> i1 = InvertedRadial3d.of(s0);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(i0, InvertedRadial3d.of(Cone.of(1, 4)));
		Assert.notEqual(i0, InvertedRadial3d.of(Cone.of(1.1, 4)));
		Assert.notEqual(i0, InvertedRadial3d.of(Cone.of(1, 3.9)));
	}

	@Test
	public void shouldExposeWrappedRadial3d() {
		Assert.equal(i0.wrapped(), c0);
		Assert.equal(i1.wrapped(), s0);
	}

	@Test
	public void shouldInvertRadiusFromHeight() {
		Assert.approx(i0.radiusFromH(-1), NaN);
		Assert.approx(i0.radiusFromH(0), 1);
		Assert.approx(i0.radiusFromH(2), 0.5);
		Assert.approx(i0.radiusFromH(4), 0);
		Assert.approx(i0.radiusFromH(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			Assert.approx(i1.radiusFromH(h), s0.radiusFromH(h));
	}

	@Test
	public void shouldInvertHeightFromVolume() {
		Assert.approx(i0.hFromVolume(-1), NaN);
		Assert.approx(i0.hFromVolume(0), 0);
		Assert.approx(i0.hFromVolume(2), 0.778);
		Assert.approx(i0.hFromVolume(4), 2.577);
		Assert.approx(i0.hFromVolume(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			Assert.approx(i1.hFromVolume(h), s0.hFromVolume(h));
	}

	@Test
	public void shouldInvertVolumeFromHeight() {
		Assert.approx(i0.volumeFromH(-1), 0);
		Assert.approx(i0.volumeFromH(0), 0);
		Assert.approx(i0.volumeFromH(2), 3.665);
		Assert.approx(i0.volumeFromH(4), 4.189);
		Assert.approx(i0.volumeFromH(5), 4.189);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			Assert.approx(i1.volumeFromH(h), s0.volumeFromH(h));
	}

	@Test
	public void shouldInvertGradient() {
		Assert.approx(i0.gradientAtH(-1), c0.gradientAtH(-1));
		Assert.approx(i0.gradientAtH(0), -c0.gradientAtH(4));
		Assert.approx(i0.gradientAtH(2), -c0.gradientAtH(2));
		Assert.approx(i0.gradientAtH(4), -c0.gradientAtH(0));
		Assert.approx(i0.gradientAtH(5), c0.gradientAtH(5));
		for (double h = -0.5; h <= 4.5; h += 0.5)
			Assert.approx(i1.gradientAtH(h), s0.gradientAtH(h));
	}

	@Test
	public void shouldMatchHeightAndVolumeOfWrappedShape() {
		Assert.approx(i0.h(), 4);
		Assert.approx(i0.volume(), 4.189);
	}
}
