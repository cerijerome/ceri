package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ConcaveSpheroidBehavior {
	private final ConcaveSpheroid3d s0 = ConcaveSpheroid3d.create(3, 1, 2);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(s0, ConcaveSpheroid3d.create(3, 1, 2));
		assertNotEquals(s0, ConcaveSpheroid3d.create(3.1, 1, 2));
		assertNotEquals(s0, ConcaveSpheroid3d.create(3, 0.9, 2));
		assertNotEquals(s0, ConcaveSpheroid3d.create(3, 1, 1.9));
	}

	@Test
	public void shouldFailForInvalidAxes() {
		assertException(() -> ConcaveSpheroid3d.create(-0.1, 1, 1));
		assertException(() -> ConcaveSpheroid3d.create(0, 1, 1));
		assertException(() -> ConcaveSpheroid3d.create(2, -0.1, 1));
		assertException(() -> ConcaveSpheroid3d.create(2, 2.1, 1));
		assertException(() -> ConcaveSpheroid3d.create(2, 2, -1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(ConcaveSpheroid3d.create(0, 0, 0), is(ConcaveSpheroid3d.NULL));
		assertApprox(ConcaveSpheroid3d.NULL.height(), 0);
		assertApprox(ConcaveSpheroid3d.NULL.r, 0);
		assertApprox(ConcaveSpheroid3d.NULL.c, 0);
		assertApprox(ConcaveSpheroid3d.NULL.volume(), 0);
		assertNotEquals(ConcaveSpheroid3d.NULL, ConcaveSpheroid3d.create(0, 0, 1));
		assertNotEquals(ConcaveSpheroid3d.NULL, ConcaveSpheroid3d.create(1, 0, 0));
		assertNotEquals(ConcaveSpheroid3d.NULL, ConcaveSpheroid3d.create(1, 1, 0));
		assertNotEquals(ConcaveSpheroid3d.NULL, ConcaveSpheroid3d.create(1, 0, 1));
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(s0.gradientAtHeight(-1), NaN);
		assertApprox(s0.gradientAtHeight(0), 0);
		assertApprox(s0.gradientAtHeight(1), -3.464);
		assertApprox(s0.gradientAtHeight(2), POSITIVE_INFINITY);
		assertApprox(s0.gradientAtHeight(3), 3.464);
		assertApprox(s0.gradientAtHeight(4), 0);
		assertApprox(s0.gradientAtHeight(5), NaN);
		assertApprox(ConcaveSpheroid3d.create(0, 0, 1).gradientAtHeight(0), 0);
		assertApprox(ConcaveSpheroid3d.create(0, 0, 1).gradientAtHeight(1), POSITIVE_INFINITY);
		assertApprox(ConcaveSpheroid3d.create(0, 0, 1).gradientAtHeight(2), 0);
		assertApprox(ConcaveSpheroid3d.create(1, 0, 0).gradientAtHeight(0), NaN);
		assertApprox(ConcaveSpheroid3d.create(1, 1, 0).gradientAtHeight(0), 0);
		assertApprox(ConcaveSpheroid3d.create(1, 0, 1).gradientAtHeight(0), 0);
		assertApprox(ConcaveSpheroid3d.create(1, 0, 1).gradientAtHeight(1), POSITIVE_INFINITY);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(s0.volume(), 62.257);
		assertApprox(ConcaveSpheroid3d.volume(3, 1, 2), 62.257);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(s0.heightFromVolume(-1), NaN);
		assertApprox(s0.heightFromVolume(0), 0);
		assertApprox(s0.heightFromVolume(25), 1.517);
		assertApprox(s0.heightFromVolume(s0.volume() / 2), 2);
		assertApprox(s0.heightFromVolume(50), 3.378);
		assertApprox(s0.heightFromVolume(s0.volume()), 4);
		assertApprox(s0.heightFromVolume(63), NaN);
	}

	@Test
	public void shouldCalculateVolumeFromHeight() {
		assertApprox(s0.volumeFromHeight(-1), 0);
		assertApprox(s0.volumeFromHeight(0), 0);
		assertApprox(s0.volumeFromHeight(1), 18.006);
		assertApprox(s0.volumeFromHeight(2), 31.129);
		assertApprox(s0.volumeFromHeight(3), 44.251);
		assertApprox(s0.volumeFromHeight(4), 62.257);
		assertApprox(s0.volumeFromHeight(5), 62.257);
	}

	@Test
	public void shouldCalulateRadiusFromHeight() {
		assertApprox(s0.radiusFromHeight(-1), NaN);
		assertApprox(s0.radiusFromHeight(0), 3);
		assertApprox(s0.radiusFromHeight(1), 2.134);
		assertApprox(s0.radiusFromHeight(2), 2);
		assertApprox(s0.radiusFromHeight(3), 2.134);
		assertApprox(s0.radiusFromHeight(4), 3);
		assertApprox(s0.radiusFromHeight(5), NaN);
	}

}
