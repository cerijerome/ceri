package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class Point2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var p0 = Point2d.of(5, 10);
		var p1 = Point2d.of(5, 10);
		var p2 = Point2d.of(4.999, 10);
		var p3 = Point2d.of(5, 10.001);
		TestUtil.exerciseEquals(p0, p1);
		Assert.notEqualAll(p0, p2, p3);
	}

	@Test
	public void shouldReverse() {
		Assert.equal(Point2d.ZERO.reverse(), Point2d.ZERO);
		Assert.equal(Point2d.of(9.99, -7.77).reverse(), Point2d.of(-9.99, 7.77));
	}

	@Test
	public void shouldCalculateToAndFrom() {
		Assert.equal(Point2d.of(10, 5).to(Point2d.of(0, -1)), Point2d.of(-10, -6));
		Assert.equal(Point2d.of(-10, -6).from(Point2d.of(0, -1)), Point2d.of(-10, -5));
	}

	@Test
	public void shouldTranslateCoordinates() {
		var p0 = Point2d.of(5, -10);
		var p1 = Point2d.of(1, 1);
		var p2 = Point2d.of(0, 0);
		var p3 = Point2d.of(-5, 10);
		Assert.equal(p0.translate(p1), Point2d.of(6, -9));
		Assert.equal(p0.translate(p2), Point2d.of(5, -10));
		Assert.equal(p0.translate(p3), Point2d.of(0, 0));
	}

	@Test
	public void shouldScaleCoordinates() {
		var p0 = Point2d.of(-5, 10);
		var r0 = Ratio2d.of(2, 0.5);
		var r1 = Ratio2d.uniform(0.1);
		Assert.equal(p0.scale(r0), Point2d.of(-10, 5));
		Assert.equal(p0.scale(r1), Point2d.of(-0.5, 1));
		Assert.equal(p0.scale(Ratio2d.UNIT), p0);
		Assert.equal(p0.scale(Ratio2d.ZERO), Point2d.ZERO);
	}

	@Test
	public void shouldCalculateAngle() {
		Assert.equal(Point2d.ZERO.angle(), Double.NaN);
		Assert.equal(Point2d.X_UNIT.angle(), 0.0);
		Assert.approx(Point2d.Y_UNIT.angle(), Math.PI / 2);
		Assert.approx(Point2d.of(1, 1).angle(), Math.PI / 4);
	}

	@Test
	public void shouldCalculateQuadrance() {
		Assert.equal(Point2d.ZERO.quadrance(), 0.0);
		Assert.equal(Point2d.X_UNIT.quadrance(), 1.0);
		Assert.equal(Point2d.Y_UNIT.quadrance(), 1.0);
		Assert.equal(Point2d.of(1, 1).quadrance(), 2.0);
	}
}
