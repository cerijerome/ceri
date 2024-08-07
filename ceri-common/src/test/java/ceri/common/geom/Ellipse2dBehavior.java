package ceri.common.geom;

import static ceri.common.geom.GeometryTestUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class Ellipse2dBehavior {
	private final Ellipse2d e0 = Ellipse2d.create(4, 2);
	private final Ellipse2d e1 = Ellipse2d.create(1, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(e0, Ellipse2d.create(4, 2));
		assertNotEquals(e0, Ellipse2d.create(4.1, 2));
		assertNotEquals(e0, Ellipse2d.create(4, 1.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertThrown(() -> Ellipse2d.create(-0.1, 4));
		assertThrown(() -> Ellipse2d.create(1, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(Ellipse2d.create(0, 0), Ellipse2d.NULL);
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
		assertEquals(Ellipse2d.NULL.gradientAtX(0), Double.NaN);
		assertEquals(Ellipse2d.NULL.gradientAtX(1), Double.NaN);
		assertEquals(Ellipse2d.NULL.gradientAtY(0), Double.NaN);
		assertEquals(Ellipse2d.NULL.gradientAtY(1), Double.NaN);
		assertEquals(Ellipse2d.create(0, 1).gradientAtX(0), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse2d.create(0, 1).gradientAtY(0), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse2d.create(0, 1).gradientAtY(0.5), Double.NEGATIVE_INFINITY);
		assertEquals(Ellipse2d.create(0, 1).gradientAtY(1), 0.0);
		assertEquals(Ellipse2d.create(1, 0).gradientAtX(0), 0.0);
		assertEquals(Ellipse2d.create(1, 0).gradientAtY(0), 0.0);
		assertEquals(Ellipse2d.create(1, 0).gradientAtX(-0.5), 0.0);
		assertEquals(Ellipse2d.create(1, 0).gradientAtX(-1), Double.POSITIVE_INFINITY);
	}

	@Test
	public void shouldCalculatePointFromGradient() {
		assertApprox(e0.pointFromGradient(0), 0, 2);
		assertApprox(e0.pointFromGradient(Double.POSITIVE_INFINITY), -4, 0);
		assertApprox(e0.pointFromGradient(Double.NEGATIVE_INFINITY), 4, 0);
		assertApprox(e0.pointFromGradient(-0.1), 0.784, 1.961);
		assertApprox(e0.pointFromGradient(-1), 3.578, 0.894);
		assertApprox(e0.pointFromGradient(-100), 4, 0.01);
		assertApprox(e0.pointFromGradient(0.1), -0.784, 1.961);
		assertApprox(e0.pointFromGradient(1), -3.578, 0.894);
		assertApprox(e0.pointFromGradient(100), -4, 0.01);
		assertApprox(Ellipse2d.NULL.pointFromGradient(0), 0, 0);
		assertApprox(Ellipse2d.NULL.pointFromGradient(1), 0, 0);
		assertApprox(Ellipse2d.create(1, 0).pointFromGradient(1), -1, 0);
		assertApprox(Ellipse2d.create(1, 0).pointFromGradient(-1), 1, 0);
		assertApprox(Ellipse2d.create(0, 1).pointFromGradient(1), 0, 1);
		assertApprox(Ellipse2d.create(0, 1).pointFromGradient(-1), 0, 1);
	}

	@Test
	public void shouldCalculatePerimeter() {
		assertApprox(Ellipse2d.create(1, 1).perimeter(), 2 * Math.PI);
		assertApprox(Ellipse2d.create(1000000, 1000000).perimeter(), 2 * Math.PI * 1000000);
		assertApprox(e0.perimeter(), 19.377);
		assertApprox(e1.perimeter(), 17.157);
		assertApprox(Ellipse2d.NULL.perimeter(), 0);
		assertApprox(Ellipse2d.create(0, 1).perimeter(), 4);
		assertApprox(Ellipse2d.create(1, 0).perimeter(), 4);
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
		assertEquals(Ellipse2d.NULL.yFromX(1), Double.NaN);
		assertEquals(Ellipse2d.NULL.xFromY(1), Double.NaN);
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
		assertApprox(Ellipse2d.NULL.areaToX(-1), 0);
		assertApprox(Ellipse2d.NULL.areaToX(0), 0);
		assertApprox(Ellipse2d.NULL.areaToY(1), 0);
		assertApprox(Ellipse2d.NULL.areaToY(0), 0);
		assertApprox(Ellipse2d.create(0, 1).areaToX(0), 0);
		assertApprox(Ellipse2d.create(0, 1).areaToY(0), 0);
		assertApprox(Ellipse2d.create(1, 0).areaToX(0), 0);
		assertApprox(Ellipse2d.create(1, 0).areaToY(0), 0);
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(e0.area(), 25.133);
		assertApprox(e1.area(), 12.566);
	}

}
