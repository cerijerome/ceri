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

public class ConicalFrustumBehavior {
	private final ConicalFrustum c0 = ConicalFrustum.create(1, 2, 4);
	private final ConicalFrustum c1 = ConicalFrustum.create(1, 4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, ConicalFrustum.create(1, 2, 4));
		assertNotEquals(c0, ConicalFrustum.create(0.9, 2, 4));
		assertNotEquals(c0, ConicalFrustum.create(1, 2.1, 4));
		assertNotEquals(c0, ConicalFrustum.create(1, 2, 3.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> ConicalFrustum.create(-0.1, 2, 4));
		assertException(() -> ConicalFrustum.create(0, -1, 4));
		assertException(() -> ConicalFrustum.create(1, 2, -1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(ConicalFrustum.create(1, 0, 4), is(ConicalFrustum.NULL));
		assertThat(ConicalFrustum.create(1, 2, 0), is(ConicalFrustum.NULL));
		assertTrue(ConicalFrustum.NULL.isNull());
		assertFalse(c0.isNull());
	}

	@Test
	public void shouldCalculateHeightFromRadius() {
		assertApprox(c0.hFromR(-1), 0);
		assertApprox(c0.hFromR(0), 0);
		assertApprox(c0.hFromR(1), 0);
		assertApprox(c0.hFromR(1.5), 2);
		assertApprox(c0.hFromR(2), 4);
		assertApprox(c0.hFromR(3), 0);
		assertApprox(ConicalFrustum.NULL.hFromR(1), 0);
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		assertApprox(c0.rFromH(-1), 0);
		assertApprox(c0.rFromH(0), 1);
		assertApprox(c0.rFromH(2), 1.5);
		assertApprox(c0.rFromH(4), 2);
		assertApprox(c0.rFromH(5), 0);
		assertApprox(ConicalFrustum.NULL.rFromH(1), 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertApprox(c0.gradient(), 4);
		assertApprox(c1.gradient(), 0.333);
		assertApprox(ConicalFrustum.NULL.gradient(), 0);
		assertApprox(c0.angle(), 1.326);
		assertApprox(c1.angle(), 0.322);
		assertApprox(ConicalFrustum.NULL.angle(), 0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertTrue(Double.isNaN(c0.hFromVolume(Double.NaN)));
		for (double h = 0; h <= 4; h += 0.5) {
			double v = c0.volumeBetweenH(0, h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(ConicalFrustum.NULL.hFromVolume(1), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeBetweenH(-1, 1), 3.992);
		assertApprox(c0.volumeBetweenH(0, 1), 3.992);
		assertApprox(c0.volumeBetweenH(1, 2), 5.956);
		assertApprox(c0.volumeBetweenH(2, 4), 19.373);
		assertApprox(c0.volumeBetweenH(0, 4), 29.322);
		assertApprox(c0.volumeBetweenH(0, 5), 29.322);
		assertApprox(ConicalFrustum.NULL.volumeBetweenH(0, 1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 29.322);
		assertApprox(c1.volume(), 21.991);
		assertApprox(ConicalFrustum.NULL.volume(), 0);
	}

}
