package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class TruncatedRadial3dBehavior {
	private final Cone3d c0 = Cone3d.create(1, 4);
	private final Spheroid3d s0 = Spheroid3d.create(4, 2);
	private final TruncatedRadial3d t0 = TruncatedRadial3d.create(c0, 1, 2);
	private final TruncatedRadial3d t1 = TruncatedRadial3d.create(s0, 0, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(t0, TruncatedRadial3d.create(Cone3d.create(1, 4), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.create(Cone3d.create(1.1, 4), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.create(Cone3d.create(1, 3.9), 1, 2));
		assertNotEquals(t0, TruncatedRadial3d.create(Cone3d.create(1, 4), 0.9, 2));
		assertNotEquals(t0, TruncatedRadial3d.create(Cone3d.create(1, 4), 1, 2.1));
	}

	@Test
	public void shouldTruncateRadiusFromHeight() {
		assertApprox(t0.radiusFromHeight(-1), NaN);
		assertApprox(t0.radiusFromHeight(0), 0.25);
		assertApprox(t0.radiusFromHeight(1), 0.5);
		assertApprox(t0.radiusFromHeight(2), 0.75);
		assertApprox(t0.radiusFromHeight(3), NaN);
		assertApprox(t1.radiusFromHeight(-1), NaN);
		assertApprox(t1.radiusFromHeight(0), 0);
		assertApprox(t1.radiusFromHeight(1), 3.464);
		assertApprox(t1.radiusFromHeight(2), 4);
		assertApprox(t1.radiusFromHeight(3), NaN);
	}

	@Test
	public void shouldTruncateHeightFromVolume() {
		assertApprox(t0.heightFromVolume(-1), NaN);
		assertApprox(t0.heightFromVolume(0), 0);
		assertApprox(t0.heightFromVolume(1), 1.534);
		assertApprox(t0.heightFromVolume(1.5), 1.881);
		assertApprox(t0.heightFromVolume(t0.volume()), 2);
		assertApprox(t0.heightFromVolume(2), NaN);
		assertApprox(t1.heightFromVolume(-1), NaN);
		assertApprox(t1.heightFromVolume(0), 0);
		assertApprox(t1.heightFromVolume(30), 1.225);
		assertApprox(t1.heightFromVolume(60), 1.860);
		assertApprox(t1.heightFromVolume(t1.volume()), 2);
		assertApprox(t1.heightFromVolume(68), NaN);
	}

	@Test
	public void shouldTruncateVolumeFromHeight() {
		assertApprox(t0.volumeFromHeight(-1), 0);
		assertApprox(t0.volumeFromHeight(0), 0);
		assertApprox(t0.volumeFromHeight(1), 0.458);
		assertApprox(t0.volumeFromHeight(2), 1.702);
		assertApprox(t0.volumeFromHeight(3), 1.702);
		assertApprox(t1.volumeFromHeight(-1), 0);
		assertApprox(t1.volumeFromHeight(0), 0);
		assertApprox(t1.volumeFromHeight(1), 20.944);
		assertApprox(t1.volumeFromHeight(2), 67.021);
		assertApprox(t1.volumeFromHeight(3), 67.021);
	}

	@Test
	public void shouldTruncateGradientAtHeight() {
		assertApprox(t0.gradientAtHeight(-1), NaN);
		assertApprox(t0.gradientAtHeight(0), 4);
		assertApprox(t0.gradientAtHeight(2), 4);
		assertApprox(t0.gradientAtHeight(3), NaN);
		assertApprox(t1.gradientAtHeight(-1), NaN);
		assertApprox(t1.gradientAtHeight(0), 0);
		assertApprox(t1.gradientAtHeight(1), 0.866);
		assertApprox(t1.gradientAtHeight(2), NEGATIVE_INFINITY);
		assertApprox(t1.gradientAtHeight(3), NaN);
	}

	@Test
	public void shouldTruncateHeight() {
		assertApprox(t0.height(), 2);
		assertApprox(t1.height(), 2);
	}

	@Test
	public void shouldTruncateVolume() {
		assertApprox(t0.volume(), 1.702);
		assertApprox(t1.volume(), 67.021);
	}

}
