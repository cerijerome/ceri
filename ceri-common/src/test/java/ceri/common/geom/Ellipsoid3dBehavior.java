package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Ellipsoid3dBehavior {
	private final Ellipsoid3d e0 = Ellipsoid3d.of(4, 2, 1);
	private final Ellipsoid3d e1 = Ellipsoid3d.of(1, 2, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(e0, Ellipsoid3d.of(4, 2, 1));
		assertNotEquals(e0, Ellipsoid3d.of(4.1, 2, 1));
		assertNotEquals(e0, Ellipsoid3d.of(4, 1.9, 1));
		assertNotEquals(e0, Ellipsoid3d.of(4, 2, 1.1));
	}

	@Test
	public void shouldAllowOnlyNonNegativeAxes() {
		assertThrown(() -> Ellipsoid3d.of(-0.1, 2, 1));
		assertThrown(() -> Ellipsoid3d.of(4, -0.1, 1));
		assertThrown(() -> Ellipsoid3d.of(4, 2, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(Ellipsoid3d.of(0, 0, 0), Ellipsoid3d.ZERO);
		assertNotEquals(Ellipsoid3d.of(1, 0, 0), is(Ellipsoid3d.ZERO));
		assertNotEquals(Ellipsoid3d.of(0, 1, 0), is(Ellipsoid3d.ZERO));
		assertNotEquals(Ellipsoid3d.of(0, 0, 1), is(Ellipsoid3d.ZERO));
	}

	@Test
	public void shouldCalculateDistanceFromPartialVolume() {
		assertApprox(e0.xFromVolume(-1), Double.NaN);
		for (double x = -4; x <= 4; x += 0.5) {
			double v = e0.volumeToX(x);
			assertApprox(e0.xFromVolume(v), x);
		}
		assertApprox(e0.xFromVolume(34), Double.NaN);
		for (double y = -2; y <= 2; y += 0.5) {
			double v = e0.volumeToY(y);
			assertApprox(e0.yFromVolume(v), y);
		}
		for (double z = -1; z <= 1; z += 0.5) {
			double v = e0.volumeToZ(z);
			assertApprox(e0.zFromVolume(v), z);
		}
		assertApprox(Ellipsoid3d.ZERO.xFromVolume(0), 0);
		assertApprox(Ellipsoid3d.ZERO.xFromVolume(1), Double.NaN);
		assertApprox(Ellipsoid3d.ZERO.yFromVolume(0), 0);
		assertApprox(Ellipsoid3d.ZERO.zFromVolume(0), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(e0.volumeToX(-4.1), 0);
		assertApprox(e0.volumeToX(-4), 0);
		assertApprox(e0.volumeToX(-2), 5.236);
		assertApprox(e0.volumeToX(0), 16.755);
		assertApprox(e0.volumeToX(2), 28.274);
		assertApprox(e0.volumeToX(4), 33.510);
		assertApprox(e0.volumeToX(4.1), 33.510);
		assertApprox(e0.volumeToY(2), 33.51);
		assertApprox(e0.volumeToZ(1), 33.51);
		assertApprox(Ellipsoid3d.ZERO.volumeToX(0), 0);
		assertApprox(Ellipsoid3d.ZERO.volumeToX(1), 0);
		assertApprox(Ellipsoid3d.ZERO.volumeToY(0), 0);
		assertApprox(Ellipsoid3d.ZERO.volumeToY(1), 0);
		assertApprox(Ellipsoid3d.ZERO.volumeToZ(0), 0);
		assertApprox(Ellipsoid3d.ZERO.volumeToZ(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(e0.volume(), 33.510);
		assertApprox(e1.volume(), 33.510);
	}

}
