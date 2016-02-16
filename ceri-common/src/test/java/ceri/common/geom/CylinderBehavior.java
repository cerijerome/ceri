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

public class CylinderBehavior {
	private final Cylinder c0 = Cylinder.create(2, 8);
	private final Cylinder c1 = Cylinder.create(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, Cylinder.create(2, 8));
		assertNotEquals(c0, Cylinder.create(1.9, 8));
		assertNotEquals(c0, Cylinder.create(2, 8.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> Cylinder.create(-0.1, 2));
		assertException(() -> Cylinder.create(4, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Cylinder.create(0, 2), is(Cylinder.NULL));
		assertThat(Cylinder.create(4, 0), is(Cylinder.NULL));
		assertTrue(Cylinder.NULL.isNull());
		assertFalse(c0.isNull());
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertTrue(Double.isNaN(c0.hFromVolume(Double.NaN)));
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeBetweenH(0, h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(Cylinder.NULL.hFromVolume(1), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeBetweenH(-1, 1), 12.566);
		assertApprox(c0.volumeBetweenH(0, 1), 12.566);
		assertApprox(c0.volumeBetweenH(7, 8), 12.566);
		assertApprox(c0.volumeBetweenH(0, 8), 100.531);
		assertApprox(c0.volumeBetweenH(0, 9), 100.531);
		assertApprox(Cylinder.NULL.volumeBetweenH(0, 1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 100.531);
		assertApprox(c1.volume(), 50.265);
		assertApprox(Cylinder.NULL.volume(), 0);
	}

}
