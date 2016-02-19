package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Cylinder3dBehavior {
	private final Cylinder3d c0 = Cylinder3d.create(2, 8);
	private final Cylinder3d c1 = Cylinder3d.create(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, Cylinder3d.create(2, 8));
		assertNotEquals(c0, Cylinder3d.create(1.9, 8));
		assertNotEquals(c0, Cylinder3d.create(2, 8.1));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> Cylinder3d.create(-0.1, 2));
		assertException(() -> Cylinder3d.create(4, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Cylinder3d.create(0, 0), is(Cylinder3d.NULL));
		assertThat(Cylinder3d.NULL.height(), is(0.0));
		assertThat(Cylinder3d.NULL.radius(), is(0.0));
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		assertApprox(c0.heightFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromHeight(h);
			assertApprox(c0.heightFromVolume(v), h);
		}
		assertApprox(c0.heightFromVolume(101), Double.NaN);
		assertApprox(Cylinder3d.NULL.heightFromVolume(0), 0);
		assertApprox(Cylinder3d.NULL.heightFromVolume(1), Double.NaN);
		assertApprox(Cylinder3d.create(0, 1).heightFromVolume(1), Double.NaN);
		assertApprox(Cylinder3d.create(1, 0).heightFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldReturnRadiusFromHeight() {
		assertApprox(c0.radiusFromHeight(-1), Double.NaN);
		assertApprox(c0.radiusFromHeight(0), 2);
		assertApprox(c0.radiusFromHeight(4), 2);
		assertApprox(c0.radiusFromHeight(8), 2);
		assertApprox(c0.radiusFromHeight(9), Double.NaN);
	}

	@Test
	public void shouldCalculateGradientAtHeight() {
		assertApprox(c0.gradientAtHeight(0), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtHeight(8), Double.NEGATIVE_INFINITY);
		assertApprox(c0.gradientAtHeight(-1), Double.NaN);
		assertApprox(c0.gradientAtHeight(9), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		assertApprox(c0.volumeFromHeight(-1), 0);
		assertApprox(c0.volumeFromHeight(0), 0);
		assertApprox(c0.volumeFromHeight(4), 50.265);
		assertApprox(c0.volumeFromHeight(8), 100.531);
		assertApprox(c0.volumeFromHeight(9), 100.531);
		assertApprox(Cylinder3d.NULL.volumeFromHeight(0), 0);
		assertApprox(Cylinder3d.NULL.volumeFromHeight(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		assertApprox(c0.volume(), 100.531);
		assertApprox(c1.volume(), 50.265);
		assertApprox(Cylinder3d.NULL.volume(), 0);
	}

}
