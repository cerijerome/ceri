package ceri.common.geom;

import static ceri.common.geom.GeomAssert.approx;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
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
		assertTrue(Double.isNaN(e0.gradientAtX(-5)));
		assertEquals(e0.gradientAtX(-4), Double.POSITIVE_INFINITY);
		assertApprox(e0.gradientAtX(-2), 0.289);
		assertApprox(e0.gradientAtX(0), 0);
		assertApprox(e0.gradientAtX(2), -0.289);
		assertEquals(e0.gradientAtX(4), Double.NEGATIVE_INFINITY);
		assertTrue(Double.isNaN(e0.gradientAtX(5)));
		assertTrue(Double.isNaN(e0.gradientAtY(-3)));
		assertApprox(e0.gradientAtY(-2), 0);
		assertApprox(e0.gradientAtY(-1), 0.866);
		assertEquals(e0.gradientAtY(0), Double.NEGATIVE_INFINITY);
		assertApprox(e0.gradientAtY(1), -0.866);
		assertApprox(e0.gradientAtY(2), 0);
		assertTrue(Double.isNaN(e0.gradientAtY(3)));
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
		approx(e0.pointFromGradient(0), 0, 2);
		approx(e0.pointFromGradient(Double.POSITIVE_INFINITY), -4, 0);
		approx(e0.pointFromGradient(Double.NEGATIVE_INFINITY), 4, 0);
		approx(e0.pointFromGradient(-0.1), 0.784, 1.961);
		approx(e0.pointFromGradient(-1), 3.578, 0.894);
		approx(e0.pointFromGradient(-100), 4, 0.01);
		approx(e0.pointFromGradient(0.1), -0.784, 1.961);
		approx(e0.pointFromGradient(1), -3.578, 0.894);
		approx(e0.pointFromGradient(100), -4, 0.01);
		approx(Ellipse.ZERO.pointFromGradient(0), 0, 0);
		approx(Ellipse.ZERO.pointFromGradient(1), 0, 0);
		approx(Ellipse.of(1, 0).pointFromGradient(1), -1, 0);
		approx(Ellipse.of(1, 0).pointFromGradient(-1), 1, 0);
		approx(Ellipse.of(0, 1).pointFromGradient(1), 0, 1);
		approx(Ellipse.of(0, 1).pointFromGradient(-1), 0, 1);
	}

	@Test
	public void shouldCalculatePerimeter() {
		assertApprox(Ellipse.of(1, 1).perimeter(), 2 * Math.PI);
		assertApprox(Ellipse.of(1000000, 1000000).perimeter(), 2 * Math.PI * 1000000);
		assertApprox(e0.perimeter(), 19.377);
		assertApprox(e1.perimeter(), 17.157);
		assertApprox(Ellipse.ZERO.perimeter(), 0);
		assertApprox(Ellipse.of(0, 1).perimeter(), 4);
		assertApprox(Ellipse.of(1, 0).perimeter(), 4);
	}

	@Test
	public void shouldCalculateXAndYCoordinates() {
		assertTrue(Double.isNaN(e0.yFromX(4.00001)));
		assertTrue(Double.isNaN(e0.yFromX(-4.00001)));
		assertTrue(Double.isNaN(e0.xFromY(2.00001)));
		assertTrue(Double.isNaN(e0.xFromY(-2.00001)));
		assertApprox(e0.yFromX(-4), 0);
		assertApprox(e0.yFromX(0), 2);
		assertApprox(e0.yFromX(4), 0);
		assertApprox(e0.yFromX(-2), 1.732);
		assertApprox(e0.yFromX(2), 1.732);
		assertApprox(e0.xFromY(-2), 0);
		assertApprox(e0.xFromY(0), 4);
		assertApprox(e0.xFromY(2), 0);
		assertApprox(e0.xFromY(1.732), 2);
		assertApprox(e0.xFromY(-2), 0);
		assertEquals(Ellipse.ZERO.yFromX(1), Double.NaN);
		assertEquals(Ellipse.ZERO.xFromY(1), Double.NaN);
	}

	@Test
	public void shouldCalculatePartialArea() {
		assertApprox(e0.areaToX(-4.1), 0);
		assertApprox(e0.areaToX(-4), 0);
		assertApprox(e0.areaToX(-2), 4.913);
		assertApprox(e0.areaToX(0), 12.566);
		assertApprox(e0.areaToX(2), 20.219);
		assertApprox(e0.areaToX(4), 25.133);
		assertApprox(e0.areaToX(4.1), 25.133);
		assertApprox(e0.areaToY(-3), 0);
		assertApprox(e0.areaToY(-2), 0);
		assertApprox(e0.areaToY(-1), 4.913);
		assertApprox(e0.areaToY(0), 12.566);
		assertApprox(e0.areaToY(1), 20.219);
		assertApprox(e0.areaToY(2), 25.133);
		assertApprox(e0.areaToY(3), 25.133);
		assertApprox(Ellipse.ZERO.areaToX(-1), 0);
		assertApprox(Ellipse.ZERO.areaToX(0), 0);
		assertApprox(Ellipse.ZERO.areaToY(1), 0);
		assertApprox(Ellipse.ZERO.areaToY(0), 0);
		assertApprox(Ellipse.of(0, 1).areaToX(0), 0);
		assertApprox(Ellipse.of(0, 1).areaToY(0), 0);
		assertApprox(Ellipse.of(1, 0).areaToX(0), 0);
		assertApprox(Ellipse.of(1, 0).areaToY(0), 0);
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(e0.area(), 25.133);
		assertApprox(e1.area(), 12.566);
	}

}
