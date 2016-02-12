package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CylinderBehavior {
	private final Cylinder c0 = new Cylinder(2, 8);
	private final Cylinder c1 = new Cylinder(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, new Cylinder(2, 8));
		assertNotEquals(c0, new Cylinder(1.9, 8));
		assertNotEquals(c0, new Cylinder(2, 8.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> new Cylinder(0, 2));
		assertException(() -> new Cylinder(4, 0));
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertTrue(Double.isNaN(c0.hFromVolume(Double.NaN)));
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeBetweenH(0, h);
			assertApprox(c0.hFromVolume(v), h);
		}
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertTrue(Double.isNaN(c0.volumeBetweenH(-0.1, 0.1)));
		assertApprox(c0.volumeBetweenH(0, 1), 12.566);
		assertApprox(c0.volumeBetweenH(7, 8), 12.566);
		assertApprox(c0.volumeBetweenH(0, 8), 100.531);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 100.531);
		assertApprox(c1.volume(), 50.265);
	}

}
