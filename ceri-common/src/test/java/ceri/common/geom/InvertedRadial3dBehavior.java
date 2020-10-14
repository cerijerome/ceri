package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.NaN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class InvertedRadial3dBehavior {
	private final Cone3d c0 = Cone3d.create(1, 4);
	private final Spheroid3d s0 = Spheroid3d.create(4, 2);
	private final InvertedRadial3d<Cone3d> i0 = InvertedRadial3d.create(c0);
	private final InvertedRadial3d<Spheroid3d> i1 = InvertedRadial3d.create(s0);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(i0, InvertedRadial3d.create(Cone3d.create(1, 4)));
		assertNotEquals(i0, InvertedRadial3d.create(Cone3d.create(1.1, 4)));
		assertNotEquals(i0, InvertedRadial3d.create(Cone3d.create(1, 3.9)));
	}

	@Test
	public void shouldExposeWrappedRadial3d() {
		assertThat(i0.wrapped(), is(c0));
		assertThat(i1.wrapped(), is(s0));
	}

	@Test
	public void shouldInvertRadiusFromHeight() {
		assertApprox(i0.radiusFromHeight(-1), NaN);
		assertApprox(i0.radiusFromHeight(0), 1);
		assertApprox(i0.radiusFromHeight(2), 0.5);
		assertApprox(i0.radiusFromHeight(4), 0);
		assertApprox(i0.radiusFromHeight(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.radiusFromHeight(h), s0.radiusFromHeight(h));
	}

	@Test
	public void shouldInvertHeightFromVolume() {
		assertApprox(i0.heightFromVolume(-1), NaN);
		assertApprox(i0.heightFromVolume(0), 0);
		assertApprox(i0.heightFromVolume(2), 0.778);
		assertApprox(i0.heightFromVolume(4), 2.577);
		assertApprox(i0.heightFromVolume(5), NaN);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.heightFromVolume(h), s0.heightFromVolume(h));
	}

	@Test
	public void shouldInvertVolumeFromHeight() {
		assertApprox(i0.volumeFromHeight(-1), 0);
		assertApprox(i0.volumeFromHeight(0), 0);
		assertApprox(i0.volumeFromHeight(2), 3.665);
		assertApprox(i0.volumeFromHeight(4), 4.189);
		assertApprox(i0.volumeFromHeight(5), 4.189);
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.volumeFromHeight(h), s0.volumeFromHeight(h));
	}

	@Test
	public void shouldInvertGradient() {
		assertApprox(i0.gradientAtHeight(-1), c0.gradientAtHeight(-1));
		assertApprox(i0.gradientAtHeight(0), -c0.gradientAtHeight(4));
		assertApprox(i0.gradientAtHeight(2), -c0.gradientAtHeight(2));
		assertApprox(i0.gradientAtHeight(4), -c0.gradientAtHeight(0));
		assertApprox(i0.gradientAtHeight(5), c0.gradientAtHeight(5));
		for (double h = -0.5; h <= 4.5; h += 0.5)
			assertApprox(i1.gradientAtHeight(h), s0.gradientAtHeight(h));
	}

	@Test
	public void shouldMatchHeightAndVolumeOfWrappedShape() {
		assertApprox(i0.height(), 4);
		assertApprox(i0.volume(), 4.189);
	}

}
