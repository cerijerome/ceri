package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class InvertedRadial3dBehavior {
	private final Cone c0 = Cone.of(1, 4);
	private final Spheroid3d s0 = Spheroid3d.of(4, 2);
	private final InvertedRadial3d<Cone> i0 = InvertedRadial3d.of(c0);
	private final InvertedRadial3d<Spheroid3d> i1 = InvertedRadial3d.of(s0);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(i0, InvertedRadial3d.of(Cone.of(1, 4)));
		assertNotEquals(i0, InvertedRadial3d.of(Cone.of(1.1, 4)));
		assertNotEquals(i0, InvertedRadial3d.of(Cone.of(1, 3.9)));
	}

	@Test
	public void shouldExposeWrappedRadial3d() {
		assertEquals(i0.wrapped(), c0);
		assertEquals(i1.wrapped(), s0);
	}

	@Test
	public void shouldInvertRadiusFromHeight() {
		assertApprox(i0.radiusFromH(-1), NaN);
		assertApprox(i0.radiusFromH(0), 1);
		assertApprox(i0.radiusFromH(2), 0.5);
		assertApprox(i0.radiusFromH(4), 0);
		assertApprox(i0.radiusFromH(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.radiusFromH(h), s0.radiusFromH(h));
	}

	@Test
	public void shouldInvertHeightFromVolume() {
		assertApprox(i0.hFromVolume(-1), NaN);
		assertApprox(i0.hFromVolume(0), 0);
		assertApprox(i0.hFromVolume(2), 0.778);
		assertApprox(i0.hFromVolume(4), 2.577);
		assertApprox(i0.hFromVolume(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.hFromVolume(h), s0.hFromVolume(h));
	}

	@Test
	public void shouldInvertVolumeFromHeight() {
		assertApprox(i0.volumeFromH(-1), 0);
		assertApprox(i0.volumeFromH(0), 0);
		assertApprox(i0.volumeFromH(2), 3.665);
		assertApprox(i0.volumeFromH(4), 4.189);
		assertApprox(i0.volumeFromH(5), 4.189);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.volumeFromH(h), s0.volumeFromH(h));
	}

	@Test
	public void shouldInvertGradient() {
		assertApprox(i0.gradientAtH(-1), c0.gradientAtH(-1));
		assertApprox(i0.gradientAtH(0), -c0.gradientAtH(4));
		assertApprox(i0.gradientAtH(2), -c0.gradientAtH(2));
		assertApprox(i0.gradientAtH(4), -c0.gradientAtH(0));
		assertApprox(i0.gradientAtH(5), c0.gradientAtH(5));
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.gradientAtH(h), s0.gradientAtH(h));
	}

	@Test
	public void shouldMatchHeightAndVolumeOfWrappedShape() {
		assertApprox(i0.h(), 4);
		assertApprox(i0.volume(), 4.189);
	}
}
