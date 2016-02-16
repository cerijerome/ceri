package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EllipsoidBehavior {
	private final Ellipsoid e0 = Ellipsoid.create(4, 2, 1);
	private final Ellipsoid e1 = Ellipsoid.create(1, 2, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(e0, Ellipsoid.create(4, 2, 1));
		assertNotEquals(e0, Ellipsoid.create(4.1, 2, 1));
		assertNotEquals(e0, Ellipsoid.create(4, 1.9, 1));
		assertNotEquals(e0, Ellipsoid.create(4, 2, 1.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> Ellipsoid.create(-0.1, 2, 1));
		assertException(() -> Ellipsoid.create(4, -0.1, 1));
		assertException(() -> Ellipsoid.create(4, 2, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Ellipsoid.create(0, 2, 1), is(Ellipsoid.NULL));
		assertThat(Ellipsoid.create(4, 0, 1), is(Ellipsoid.NULL));
		assertThat(Ellipsoid.create(4, 2, 0), is(Ellipsoid.NULL));
		assertTrue(Ellipsoid.NULL.isNull());
		assertFalse(e0.isNull());
	}

	@Test
	public void shouldCalculateDistanceFromPartialVolume() {
		assertTrue(Double.isNaN(e0.xFromVolume(Double.NaN)));
		for (double x = -4; x <= 4; x += 0.5) {
			double v = e0.volumeBetweenX(-4, x);
			assertApprox(e0.xFromVolume(v), x);
		}
		for (double y = -2; y <= 2; y += 0.5) {
			double v = e0.volumeBetweenY(-2, y);
			assertApprox(e0.yFromVolume(v), y);
		}
		for (double z = -1; z <= 1; z += 0.5) {
			double v = e0.volumeBetweenZ(-1, z);
			assertApprox(e0.zFromVolume(v), z);
		}
		assertApprox(Ellipsoid.NULL.xFromVolume(1), 0);
		assertApprox(Ellipsoid.NULL.yFromVolume(1), 0);
		assertApprox(Ellipsoid.NULL.zFromVolume(1), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertTrue(Double.isNaN(e0.volumeBetweenX(-4.1, 0)));
		assertTrue(Double.isNaN(e0.volumeBetweenX(-2, 4.1)));
		assertTrue(Double.isNaN(e0.volumeBetweenY(-2.1, 2)));
		assertTrue(Double.isNaN(e0.volumeBetweenY(-1, 2.1)));
		assertTrue(Double.isNaN(e0.volumeBetweenZ(-1.1, 0)));
		assertTrue(Double.isNaN(e0.volumeBetweenZ(1, 1.1)));
		assertTrue(Double.isNaN(e0.volumeBetweenX(0, Double.NaN)));
		assertApprox(e0.volumeBetweenX(-1, 1), 12.305);
		assertApprox(e0.volumeBetweenX(-1.1, 1), 12.890);
		assertApprox(e0.volumeBetweenX(-1, 1.1), 12.890);
		assertApprox(e0.volumeBetweenY(0, 1), 11.519);
		assertApprox(e0.volumeBetweenY(-0.1, 1), 12.775);
		assertApprox(e0.volumeBetweenY(-1, 0.1), 12.775);
		assertApprox(e0.volumeBetweenZ(0.2, 0.8), 10.857);
		assertApprox(e0.volumeBetweenZ(0.1, 0.8), 13.312);
		assertApprox(e0.volumeBetweenZ(-0.8, -0.1), 13.312);
		assertApprox(Ellipsoid.NULL.volumeBetweenX(0, 1), 0);
		assertApprox(Ellipsoid.NULL.volumeBetweenY(0, 1), 0);
		assertApprox(Ellipsoid.NULL.volumeBetweenZ(0, 1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(e0.volume(), 33.510);
		assertApprox(e1.volume(), 33.510);
	}

}
