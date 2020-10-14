package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Spheroid3dBehavior {
	private final Spheroid3d s0 = Spheroid3d.create(2, 3);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(s0, Spheroid3d.create(2, 3));
		assertNotEquals(s0, Spheroid3d.create(2.1, 3));
		assertNotEquals(s0, Spheroid3d.create(2, 2.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		TestUtil.assertThrown(() -> Spheroid3d.create(-0.1, 1));
		TestUtil.assertThrown(() -> Spheroid3d.create(2, -1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Spheroid3d.create(0, 0), is(Spheroid3d.NULL));
		assertApprox(Spheroid3d.NULL.height(), 0);
		assertApprox(Spheroid3d.NULL.r, 0);
		assertApprox(Spheroid3d.NULL.c, 0);
		assertApprox(Spheroid3d.NULL.volume(), 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(s0.gradientAtHeight(-1), NaN);
		assertApprox(s0.gradientAtHeight(0), 0);
		assertApprox(s0.gradientAtHeight(1), 1.677);
		assertApprox(s0.gradientAtHeight(3), NEGATIVE_INFINITY);
		assertApprox(s0.gradientAtHeight(4), -4.243);
		assertApprox(s0.gradientAtHeight(6), 0);
		assertApprox(s0.gradientAtHeight(7), NaN);
		assertApprox(Spheroid3d.create(0, 1).gradientAtHeight(0), 0);
		assertApprox(Spheroid3d.create(0, 1).gradientAtHeight(1), NEGATIVE_INFINITY);
		assertApprox(Spheroid3d.create(1, 0).gradientAtHeight(0), 0);
		assertApprox(Spheroid3d.create(1, 0).gradientAtHeight(1), NaN);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(s0.volume(), 50.265);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(s0.heightFromVolume(-1), NaN);
		assertApprox(s0.heightFromVolume(0), 0);
		assertApprox(s0.heightFromVolume(25), 2.989);
		assertApprox(s0.heightFromVolume(50), 5.745);
		assertApprox(s0.heightFromVolume(51), NaN);
	}

	@Test
	public void shouldCalulateRadiusFromHeight() {
		assertApprox(s0.radiusFromHeight(-1), NaN);
		assertApprox(s0.radiusFromHeight(0), 0);
		assertApprox(s0.radiusFromHeight(3), 2);
		assertApprox(s0.radiusFromHeight(6), 0);
		assertApprox(s0.radiusFromHeight(7), NaN);
	}

	@Test
	public void shouldCalculateVolumeFromHeight() {
		assertApprox(s0.volumeFromHeight(-1), 0);
		assertApprox(s0.volumeFromHeight(0), 0);
		assertApprox(s0.volumeFromHeight(3), 25.133);
		assertApprox(s0.volumeFromHeight(6), 50.265);
		assertApprox(s0.volumeFromHeight(7), 50.265);
	}

}
