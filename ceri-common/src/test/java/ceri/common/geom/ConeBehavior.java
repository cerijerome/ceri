package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ConeBehavior {
	private final Cone c0 = Cone.create(2, 8);
	private final Cone c1 = Cone.create(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, Cone.create(2, 8));
		assertNotEquals(c0, Cone.create(1.9, 8));
		assertNotEquals(c0, Cone.create(2, 8.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> Cone.create(-0.1, 2));
		assertException(() -> Cone.create(4, -0.1));
	}

	@Test
	public void shouldDefineNullCone() {
		assertThat(Cone.create(0, 2), is(Cone.NULL));
		assertThat(Cone.create(4, 0), is(Cone.NULL));
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(c0.gradient(), 4);
		assertApprox(c1.gradient(), 0.25);
		assertApprox(Cone.NULL.gradient(), 0);
		assertApprox(c0.angle(), 1.326);
		assertApprox(c1.angle(), 0.245);
		assertApprox(Cone.NULL.angle(), 0);
	}

	@Test
	public void shouldCalculateHeightFromRadius() {
		assertApprox(c0.hFromR(0), 0);
		assertApprox(c0.hFromR(1), 4);
		assertApprox(c0.hFromR(2), 8);
		assertApprox(c1.hFromR(0), 0);
		assertApprox(c1.hFromR(2), 0.5);
		assertApprox(c1.hFromR(4), 1);
		assertApprox(Cone.NULL.hFromR(1), 0);
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		assertApprox(c0.rFromH(0), 0);
		assertApprox(c0.rFromH(4), 1);
		assertApprox(c0.rFromH(8), 2);
		assertApprox(c1.rFromH(0), 0);
		assertApprox(c1.rFromH(0.5), 2);
		assertApprox(c1.rFromH(1), 4);
		assertApprox(Cone.NULL.rFromH(1), 0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertTrue(Double.isNaN(c0.hFromVolume(Double.NaN)));
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeBetweenH(0, h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(Cone.NULL.hFromVolume(1), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeBetweenH(-1, 1), 0.065);
		assertApprox(c0.volumeBetweenH(0, 1), 0.065);
		assertApprox(c0.volumeBetweenH(4, 5), 3.992);
		assertApprox(c0.volumeBetweenH(6, 7), 8.312);
		assertApprox(c0.volumeBetweenH(7, 8), 11.061);
		assertApprox(c0.volumeBetweenH(0, 8), 33.510);
		assertApprox(c0.volumeBetweenH(0, 9), 33.510);
		assertApprox(Cone.NULL.volumeBetweenH(0, 1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 33.510);
		assertApprox(c1.volume(), 16.755);
		assertApprox(Cone.NULL.volume(), 0);
	}

}
