package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class CylinderBehavior {
	private final Cylinder c0 = Cylinder.of(2, 8);
	private final Cylinder c1 = Cylinder.of(4, 1);

	@Test
	public void shouldNotBreachEqualsContract() {
		Testing.exerciseEquals(c0, Cylinder.of(2, 8));
		Assert.notEqual(c0, Cylinder.of(1.9, 8));
		Assert.notEqual(c0, Cylinder.of(2, 8.1));
	}

	@Test
	public void shouldDefineNull() {
		Assert.equal(Cylinder.of(0, 0), Cylinder.ZERO);
		GeomAssert.approx(Cylinder.ZERO, 0, 0);
	}

	@Test
	public void shouldCalculateHeightFromVolume() {
		Assert.approx(c0.hFromVolume(-1), Double.NaN);
		for (double h = 0; h <= 8; h += 0.5) {
			double v = c0.volumeFromH(h);
			Assert.approx(c0.hFromVolume(v), h);
		}
		Assert.approx(c0.hFromVolume(101), Double.NaN);
		Assert.approx(Cylinder.ZERO.hFromVolume(0), 0);
		Assert.approx(Cylinder.ZERO.hFromVolume(1), Double.NaN);
		Assert.approx(Cylinder.of(0, 1).hFromVolume(1), Double.NaN);
		Assert.approx(Cylinder.of(1, 0).hFromVolume(1), Double.NaN);
	}

	@Test
	public void shouldReturnRadiusFromHeight() {
		Assert.approx(c0.radiusFromH(-1), Double.NaN);
		Assert.approx(c0.radiusFromH(0), 2);
		Assert.approx(c0.radiusFromH(4), 2);
		Assert.approx(c0.radiusFromH(8), 2);
		Assert.approx(c0.radiusFromH(9), Double.NaN);
	}

	@Test
	public void shouldCalculateGradientAtHeight() {
		Assert.approx(c0.gradientAtH(0), Double.NEGATIVE_INFINITY);
		Assert.approx(c0.gradientAtH(8), Double.NEGATIVE_INFINITY);
		Assert.approx(c0.gradientAtH(-1), Double.NaN);
		Assert.approx(c0.gradientAtH(9), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialVolume() {
		Assert.approx(c0.volumeFromH(-1), 0);
		Assert.approx(c0.volumeFromH(0), 0);
		Assert.approx(c0.volumeFromH(4), 50.265);
		Assert.approx(c0.volumeFromH(8), 100.531);
		Assert.approx(c0.volumeFromH(9), 100.531);
		Assert.approx(Cylinder.ZERO.volumeFromH(0), 0);
		Assert.approx(Cylinder.ZERO.volumeFromH(1), 0);
	}

	@Test
	public void shouldCalculateVolume() {
		Assert.approx(c0.volume(), 100.531);
		Assert.approx(c1.volume(), 50.265);
		Assert.approx(Cylinder.ZERO.volume(), 0);
	}
}
