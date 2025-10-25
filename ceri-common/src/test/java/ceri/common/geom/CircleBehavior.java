package ceri.common.geom;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class CircleBehavior {
	private final Circle c0 = Circle.of(4);
	private final Circle c1 = Circle.of(1);

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(c0, Circle.of(4));
		assertNotEquals(c0, Circle.of(4.1));
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(Circle.of(0), Circle.ZERO);
	}

	@Test
	public void shouldPointFromGradient() {
		GeomAssert.approx(c0.pointFromGradient(0), 0, 4);
		GeomAssert.approx(c0.pointFromGradient(Double.POSITIVE_INFINITY), -4, 0);
		GeomAssert.approx(c0.pointFromGradient(Double.NEGATIVE_INFINITY), 4, 0);
		GeomAssert.approx(c0.pointFromGradient(1), -2.828, 2.828);
		GeomAssert.approx(c0.pointFromGradient(-2), 3.578, 1.789);
		assertEquals(Circle.ZERO.pointFromGradient(0), Point2d.ZERO);
	}

	@Test
	public void shouldCalculateGradientAtX() {
		Assert.approx(c0.gradientAtX(0), 0);
		Assert.approx(c0.gradientAtX(2), -0.577);
		Assert.approx(c0.gradientAtX(-2), 0.577);
		assertEquals(c0.gradientAtX(-4), Double.POSITIVE_INFINITY);
		assertEquals(c0.gradientAtX(4), Double.NEGATIVE_INFINITY);
		assertEquals(c0.gradientAtX(5), Double.NaN);
		assertEquals(c0.gradientAtX(-5), Double.NaN);
		assertEquals(Circle.ZERO.gradientAtX(0), Double.NaN);
		assertEquals(Circle.ZERO.gradientAtX(1), Double.NaN);
	}

	@Test
	public void shouldCalculateGradientAtY() {
		assertEquals(c0.gradientAtY(0), Double.NEGATIVE_INFINITY);
		Assert.approx(c0.gradientAtY(2), -1.732);
		Assert.approx(c0.gradientAtY(-2), 1.732);
		Assert.approx(c0.gradientAtY(-4), 0);
		Assert.approx(c0.gradientAtY(4), 0);
		assertEquals(c0.gradientAtY(5), Double.NaN);
		assertEquals(c0.gradientAtY(-5), Double.NaN);
		assertEquals(Circle.ZERO.gradientAtY(0), Double.NaN);
		assertEquals(Circle.ZERO.gradientAtY(1), Double.NaN);
	}

	@Test
	public void shouldCalculateXFromY() {
		Assert.approx(c0.xFromY(4), 0);
		Assert.approx(c0.xFromY(-4), 0);
		Assert.approx(c0.xFromY(0), 4);
		Assert.approx(c0.xFromY(-0.0), 4);
		Assert.approx(c0.xFromY(2), 3.464);
		Assert.approx(c0.xFromY(3.464), 2);
		assertEquals(c0.xFromY(5), Double.NaN);
		assertEquals(c0.xFromY(-5), Double.NaN);
		assertEquals(Circle.ZERO.xFromY(0), 0.0);
		assertEquals(Circle.ZERO.xFromY(1), Double.NaN);
	}

	@Test
	public void shouldCalculateYFromX() {
		Assert.approx(c0.yFromX(4), 0);
		Assert.approx(c0.yFromX(-4), 0);
		Assert.approx(c0.yFromX(0), 4);
		Assert.approx(c0.yFromX(-0.0), 4);
		Assert.approx(c0.yFromX(2), 3.464);
		Assert.approx(c0.yFromX(3.464), 2);
		assertEquals(c0.yFromX(5), Double.NaN);
		assertEquals(c0.yFromX(-5), Double.NaN);
		assertEquals(Circle.ZERO.yFromX(0), 0.0);
		assertEquals(Circle.ZERO.yFromX(1), Double.NaN);
	}

	@Test
	public void shouldCalculateRadiusFromArea() {
		Assert.approx(Circle.radiusFromArea(50.265), 4);
		Assert.approx(Circle.radiusFromArea(3.142), 1);
		assertEquals(Circle.radiusFromArea(0), 0.0);
		assertEquals(Circle.radiusFromArea(-1), Double.NaN);
	}

	@Test
	public void shouldCalculateCircumference() {
		Assert.approx(c0.circumference(), 25.133);
		Assert.approx(c1.circumference(), 6.283);
		Assert.approx(Circle.circumference(4), 25.133);
	}

	@Test
	public void shouldCalculateArea() {
		Assert.approx(c0.area(), 50.265);
		Assert.approx(c1.area(), 3.142);
		Assert.approx(Circle.area(1), 3.142);
	}

	@Test
	public void shouldCalculateAreaToX() {
		Assert.approx(c0.areaToX(-5), 0);
		Assert.approx(c0.areaToX(-4), 0);
		Assert.approx(c0.areaToX(-2), 9.827);
		Assert.approx(c0.areaToX(0), 25.133);
		Assert.approx(c0.areaToX(2), 40.439);
		Assert.approx(c0.areaToX(4), 50.265);
		Assert.approx(c0.areaToX(5), 50.265);
		Assert.approx(Circle.ZERO.areaToX(0), 0);
		Assert.approx(Circle.ZERO.areaToX(1), 0);
	}

	@Test
	public void shouldCalculateAreaToY() {
		Assert.approx(c0.areaToY(-5), 0);
		Assert.approx(c0.areaToY(-4), 0);
		Assert.approx(c0.areaToY(-2), 9.827);
		Assert.approx(c0.areaToY(0), 25.133);
		Assert.approx(c0.areaToY(2), 40.439);
		Assert.approx(c0.areaToY(4), 50.265);
		Assert.approx(c0.areaToY(5), 50.265);
		Assert.approx(Circle.ZERO.areaToY(0), 0);
		Assert.approx(Circle.ZERO.areaToY(1), 0);
	}
}
