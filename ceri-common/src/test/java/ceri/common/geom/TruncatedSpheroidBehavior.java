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

public class TruncatedSpheroidBehavior {
	private final TruncatedSpheroid c0 = TruncatedSpheroid.create(2, 3, -2, 3);
	private final TruncatedSpheroid c1 = TruncatedSpheroid.create(4, 1, 0, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, TruncatedSpheroid.create(2, 3, -2, 3));
		assertNotEquals(c0, TruncatedSpheroid.create(1.9, 3, -2, 3));
		assertNotEquals(c0, TruncatedSpheroid.create(2, 3.1, -2, 3));
		assertNotEquals(c0, TruncatedSpheroid.create(2, 3, -1.9, 3));
		assertNotEquals(c0, TruncatedSpheroid.create(2, 3, -2, 2.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> TruncatedSpheroid.create(-0.1, 2, -1, 1));
		assertException(() -> TruncatedSpheroid.create(1, -0.1, -1, 1));
		assertException(() -> TruncatedSpheroid.create(1, 2, -2.1, 1));
		assertException(() -> TruncatedSpheroid.create(1, 2, -1, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(TruncatedSpheroid.create(0, 3, -2, 1), is(TruncatedSpheroid.NULL));
		assertThat(TruncatedSpheroid.create(2, 0, -2, 1), is(TruncatedSpheroid.NULL));
		assertThat(TruncatedSpheroid.create(2, 3, -2, 0), is(TruncatedSpheroid.NULL));
		assertTrue(TruncatedSpheroid.NULL.isNull());
		assertFalse(c0.isNull());
	}

	@Test
	public void shouldCalculateHeightFromRadius() {
		assertApprox(c0.height(), 3);
		assertApprox(c0.hFromR(-1));
		assertApprox(c0.hFromR(0));
		assertApprox(c0.hFromR(1.491), 0);
		assertApprox(c0.hFromR(1.8857), 1, 3);
		assertApprox(c0.hFromR(2), 2.0);
		assertApprox(c0.hFromR(3));
		assertApprox(TruncatedSpheroid.NULL.hFromR(1));
	}

	@Test
	public void shouldCalculateRadiusFromHeight() {
		assertApprox(c0.rFromH(-1), 0);
		assertApprox(c0.rFromH(0), 1.491);
		assertApprox(c0.rFromH(1), 1.886);
		assertApprox(c0.rFromH(2), 2);
		assertApprox(c0.rFromH(3), 1.886);
		assertApprox(c0.rFromH(4), 0);
		assertApprox(TruncatedSpheroid.NULL.rFromH(1), 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertTrue(Double.isNaN(c0.gradientFromH(-1)));
		assertApprox(c0.gradientFromH(0), 1.677);
		assertApprox(c0.gradientFromH(1), 4.243);
		assertThat(c0.gradientFromH(2), is(Double.POSITIVE_INFINITY));
		assertApprox(c0.gradientFromH(3), -4.243);
		assertTrue(Double.isNaN(c0.gradientFromH(4)));
		assertApprox(TruncatedSpheroid.NULL.gradientFromH(1), 0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertTrue(Double.isNaN(c0.hFromVolume(Double.NaN)));
		for (double h = 0; h <= 3; h += 0.5) {
			double v = c0.volumeBetweenH(0, h);
			assertApprox(c0.hFromVolume(v), h);
		}
		assertApprox(TruncatedSpheroid.NULL.hFromVolume(1), 0);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeBetweenH(-1, 1), 9.308);
		assertApprox(c0.volumeBetweenH(0, 1), 9.308);
		assertApprox(c0.volumeBetweenH(1, 2), 12.101);
		assertApprox(c0.volumeBetweenH(2, 3), 12.101);
		assertApprox(c0.volumeBetweenH(0, 3), 33.51);
		assertApprox(c0.volumeBetweenH(0, 4), 33.51);
		assertApprox(TruncatedSpheroid.NULL.volumeBetweenH(0, 1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 33.51);
		assertApprox(c1.volume(), 33.51);
		assertApprox(TruncatedSpheroid.NULL.volume(), 0);
	}

}
