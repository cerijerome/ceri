package ceri.common.geom;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class EllipseBehavior {
	private final Ellipse e0 = Ellipse.of(4, 2);
	private final Ellipse e1 = Ellipse.of(1, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(e0, Ellipse.of(4, 2));
		assertNotEquals(e0, Ellipse.of(4.1, 2));
		assertNotEquals(e0, Ellipse.of(4, 1.9));
	}

	@Test
	public void shouldDefineNull() {
		GeomAssert.approx(Ellipse.ZERO, 0, 0);
	}

	@Test
	public void shouldCalculateGradient() {
		assertEquals(Double.isNaN(e0.gradientAtX(-5)), true);
		assertEquals(e0.gradientAtX(-4), Double.POSITIVE_INFINITY);
		Assert.approx(e0.gradientAtX(-2), 0.289);
		Assert.approx(e0.gradientAtX(0), 0);
		Assert.approx(e0.gradientAtX(2), -0.289);
		assertEquals(e0.gradientAtX(4), Double.NEGATIVE_INFINITY);
		assertEquals(Double.isNaN(e0.gradientAtX(5)), true);
		assertEquals(Double.isNaN(e0.gradientAtY(-3)), true);
		Assert.approx(e0.gradientAtY(-2), 0);
		Assert.approx(e0.gradientAtY(-1), 0.866);
		assertEquals(e0.gradientAtY(0), Double.NEGATIVE_INFINITY);
		Assert.approx(e0.gradientAtY(1), -0.866);
		Assert.approx(e0.gradientAtY(2), 0);
		assertEquals(Double.isNaN(e0.gradientAtY(3)), true);
		assertEquals(Ellipse.ZERO.gradientAtX(0), Double.NaN);
		assertEquals(Ellipse.ZERO.gradientAtX(1), Double.NaN);
		assertEquals(Ellipse.ZERO.gradientAtY(0), Double.NaN);
		assertEquals(Ellipse.ZERO.gradientAtY(1), Double.NaN);
		assertEquals(Ellipse.of(0, 1).gradientAtX(0), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse.of(0, 1).gradientAtY(0), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse.of(0, 1).gradientAtY(0.5), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse.of(0, 1).gradientAtY(1), 0.0);
		assertEquals(Ellipse.of(1, 0).gradientAtX(0), 0.0);
		assertEquals(Ellipse.of(1, 0).gradientAtY(0), 0.0);
		assertEquals(Ellipse.of(1, 0).gradientAtX(-0.5), 0.0);
		assertEquals(Ellipse.of(1, 0).gradientAtX(-1), Double.POSITIVE_INFINITY);
	}

	@Test
	public void shouldCalculatePointFromGradient() {
		GeomAssert.approx(e0.pointFromGradient(0), 0, 2);
		GeomAssert.approx(e0.pointFromGradient(Double.POSITIVE_INFINITY), -4, 0);
		GeomAssert.approx(e0.pointFromGradient(Double.NEGATIVE_INFINITY), 4, 0);
		GeomAssert.approx(e0.pointFromGradient(-0.1), 0.784, 1.961);
		GeomAssert.approx(e0.pointFromGradient(-1), 3.578, 0.894);
		GeomAssert.approx(e0.pointFromGradient(-100), 4, 0.01);
		GeomAssert.approx(e0.pointFromGradient(0.1), -0.784, 1.961);
		GeomAssert.approx(e0.pointFromGradient(1), -3.578, 0.894);
		GeomAssert.approx(e0.pointFromGradient(100), -4, 0.01);
		GeomAssert.approx(Ellipse.ZERO.pointFromGradient(0), 0, 0);
		GeomAssert.approx(Ellipse.ZERO.pointFromGradient(1), 0, 0);
		GeomAssert.approx(Ellipse.of(1, 0).pointFromGradient(1), -1, 0);
		GeomAssert.approx(Ellipse.of(1, 0).pointFromGradient(-1), 1, 0);
		GeomAssert.approx(Ellipse.of(0, 1).pointFromGradient(1), 0, 1);
		GeomAssert.approx(Ellipse.of(0, 1).pointFromGradient(-1), 0, 1);
	}

	@Test
	public void shouldCalculatePerimeter() {
		Assert.approx(Ellipse.of(1, 1).perimeter(), 2 * Math.PI);
		Assert.approx(Ellipse.of(1000000, 1000000).perimeter(), 2 * Math.PI * 1000000);
		Assert.approx(e0.perimeter(), 19.377);
		Assert.approx(e1.perimeter(), 17.157);
		Assert.approx(Ellipse.ZERO.perimeter(), 0);
		Assert.approx(Ellipse.of(0, 1).perimeter(), 4);
		Assert.approx(Ellipse.of(1, 0).perimeter(), 4);
	}

	@Test
	public void shouldCalculateXAndYCoordinates() {
		assertEquals(Double.isNaN(e0.yFromX(4.00001)), true);
		assertEquals(Double.isNaN(e0.yFromX(-4.00001)), true);
		assertEquals(Double.isNaN(e0.xFromY(2.00001)), true);
		assertEquals(Double.isNaN(e0.xFromY(-2.00001)), true);
		Assert.approx(e0.yFromX(-4), 0);
		Assert.approx(e0.yFromX(0), 2);
		Assert.approx(e0.yFromX(4), 0);
		Assert.approx(e0.yFromX(-2), 1.732);
		Assert.approx(e0.yFromX(2), 1.732);
		Assert.approx(e0.xFromY(-2), 0);
		Assert.approx(e0.xFromY(0), 4);
		Assert.approx(e0.xFromY(2), 0);
		Assert.approx(e0.xFromY(1.732), 2);
		Assert.approx(e0.xFromY(-2), 0);
		assertEquals(Ellipse.ZERO.yFromX(1), Double.NaN);
		assertEquals(Ellipse.ZERO.xFromY(1), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialArea() {
		Assert.approx(e0.areaToX(-4.1), 0);
		Assert.approx(e0.areaToX(-4), 0);
		Assert.approx(e0.areaToX(-2), 4.913);
		Assert.approx(e0.areaToX(0), 12.566);
		Assert.approx(e0.areaToX(2), 20.219);
		Assert.approx(e0.areaToX(4), 25.133);
		Assert.approx(e0.areaToX(4.1), 25.133);
		Assert.approx(e0.areaToY(-3), 0);
		Assert.approx(e0.areaToY(-2), 0);
		Assert.approx(e0.areaToY(-1), 4.913);
		Assert.approx(e0.areaToY(0), 12.566);
		Assert.approx(e0.areaToY(1), 20.219);
		Assert.approx(e0.areaToY(2), 25.133);
		Assert.approx(e0.areaToY(3), 25.133);
		Assert.approx(Ellipse.ZERO.areaToX(-1), 0);
		Assert.approx(Ellipse.ZERO.areaToX(0), 0);
		Assert.approx(Ellipse.ZERO.areaToY(1), 0);
		Assert.approx(Ellipse.ZERO.areaToY(0), 0);
		Assert.approx(Ellipse.of(0, 1).areaToX(0), 0);
		Assert.approx(Ellipse.of(0, 1).areaToY(0), 0);
		Assert.approx(Ellipse.of(1, 0).areaToX(0), 0);
		Assert.approx(Ellipse.of(1, 0).areaToY(0), 0);
	}

	@Test
	public void shouldCalculateArea() {
		Assert.approx(e0.area(), 25.133);
		Assert.approx(e1.area(), 12.566);
	}
}
