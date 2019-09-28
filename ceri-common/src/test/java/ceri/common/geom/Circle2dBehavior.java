package ceri.common.geom;

import static ceri.common.geom.GeometryTestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Circle2dBehavior {
	private final Circle2d c0 = Circle2d.of(4);
	private final Circle2d c1 = Circle2d.of(1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, Circle2d.of(4));
		assertNotEquals(c0, Circle2d.of(4.1));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		TestUtil.assertThrown(() -> Circle2d.of(-0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Circle2d.of(0), is(Circle2d.NULL));
	}

	@Test
	public void shouldPointFromGradient() {
		assertApprox(c0.pointFromGradient(0), 0, 4);
		assertApprox(c0.pointFromGradient(Double.POSITIVE_INFINITY), -4, 0);
		assertApprox(c0.pointFromGradient(Double.NEGATIVE_INFINITY), 4, 0);
		assertApprox(c0.pointFromGradient(1), -2.828, 2.828);
		assertApprox(c0.pointFromGradient(-2), 3.578, 1.789);
		assertThat(Circle2d.NULL.pointFromGradient(0), is(Point2d.ZERO));
	}

	@Test
	public void shouldCalculateGradientAtX() {
		assertApprox(c0.gradientAtX(0), 0);
		assertApprox(c0.gradientAtX(2), -0.577);
		assertApprox(c0.gradientAtX(-2), 0.577);
		assertThat(c0.gradientAtX(-4), is(Double.POSITIVE_INFINITY));
		assertThat(c0.gradientAtX(4), is(Double.NEGATIVE_INFINITY));
		assertThat(c0.gradientAtX(5), is(Double.NaN));
		assertThat(c0.gradientAtX(-5), is(Double.NaN));
		assertThat(Circle2d.NULL.gradientAtX(0), is(Double.NaN));
		assertThat(Circle2d.NULL.gradientAtX(1), is(Double.NaN));
	}

	@Test
	public void shouldCalculateGradientAtY() {
		assertThat(c0.gradientAtY(0), is(Double.NEGATIVE_INFINITY));
		assertApprox(c0.gradientAtY(2), -1.732);
		assertApprox(c0.gradientAtY(-2), 1.732);
		assertApprox(c0.gradientAtY(-4), 0);
		assertApprox(c0.gradientAtY(4), 0);
		assertThat(c0.gradientAtY(5), is(Double.NaN));
		assertThat(c0.gradientAtY(-5), is(Double.NaN));
		assertThat(Circle2d.NULL.gradientAtY(0), is(Double.NaN));
		assertThat(Circle2d.NULL.gradientAtY(1), is(Double.NaN));
	}

	@Test
	public void shouldCalculateXFromY() {
		assertApprox(c0.xFromY(4), 0);
		assertApprox(c0.xFromY(-4), 0);
		assertApprox(c0.xFromY(0), 4);
		assertApprox(c0.xFromY(-0.0), 4);
		assertApprox(c0.xFromY(2), 3.464);
		assertApprox(c0.xFromY(3.464), 2);
		assertThat(c0.xFromY(5), is(Double.NaN));
		assertThat(c0.xFromY(-5), is(Double.NaN));
		assertThat(Circle2d.NULL.xFromY(0), is(0.0));
		assertThat(Circle2d.NULL.xFromY(1), is(Double.NaN));
	}

	@Test
	public void shouldCalculateYFromX() {
		assertApprox(c0.yFromX(4), 0);
		assertApprox(c0.yFromX(-4), 0);
		assertApprox(c0.yFromX(0), 4);
		assertApprox(c0.yFromX(-0.0), 4);
		assertApprox(c0.yFromX(2), 3.464);
		assertApprox(c0.yFromX(3.464), 2);
		assertThat(c0.yFromX(5), is(Double.NaN));
		assertThat(c0.yFromX(-5), is(Double.NaN));
		assertThat(Circle2d.NULL.yFromX(0), is(0.0));
		assertThat(Circle2d.NULL.yFromX(1), is(Double.NaN));
	}

	@Test
	public void shouldCalculateRadiusFromArea() {
		assertApprox(Circle2d.radiusFromArea(50.265), 4);
		assertApprox(Circle2d.radiusFromArea(3.142), 1);
		assertThat(Circle2d.radiusFromArea(0), is(0.0));
		assertThat(Circle2d.radiusFromArea(-1), is(Double.NaN));
	}

	@Test
	public void shouldCalculateCircumference() {
		assertApprox(c0.circumference(), 25.133);
		assertApprox(c1.circumference(), 6.283);
		assertApprox(Circle2d.circumference(4), 25.133);
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(c0.area(), 50.265);
		assertApprox(c1.area(), 3.142);
		assertApprox(Circle2d.area(1), 3.142);
	}

	@Test
	public void shouldCalculateAreaToX() {
		assertApprox(c0.areaToX(-5), 0);
		assertApprox(c0.areaToX(-4), 0);
		assertApprox(c0.areaToX(-2), 9.827);
		assertApprox(c0.areaToX(0), 25.133);
		assertApprox(c0.areaToX(2), 40.439);
		assertApprox(c0.areaToX(4), 50.265);
		assertApprox(c0.areaToX(5), 50.265);
		assertApprox(Circle2d.NULL.areaToX(0), 0);
		assertApprox(Circle2d.NULL.areaToX(1), 0);
	}

	@Test
	public void shouldCalculateAreaToY() {
		assertApprox(c0.areaToY(-5), 0);
		assertApprox(c0.areaToY(-4), 0);
		assertApprox(c0.areaToY(-2), 9.827);
		assertApprox(c0.areaToY(0), 25.133);
		assertApprox(c0.areaToY(2), 40.439);
		assertApprox(c0.areaToY(4), 50.265);
		assertApprox(c0.areaToY(5), 50.265);
		assertApprox(Circle2d.NULL.areaToY(0), 0);
		assertApprox(Circle2d.NULL.areaToY(1), 0);
	}

}
