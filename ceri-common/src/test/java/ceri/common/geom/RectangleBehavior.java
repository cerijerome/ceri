package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class RectangleBehavior {
	private final Rectangle rt0 = Rectangle.of(0, 0, 200, 100);
	private final Rectangle rt1 = Rectangle.of(-50, 50, 100, 40);
	private final Rectangle rt2 = Rectangle.of(-40, -100, 0, 400);
	private final Rectangle rt3 = Rectangle.of(-20, -200, 100, 100);

	@Test
	public void shouldNotBreachEqualsContract() {
		var r = Rectangle.of(0, 0, 20, 40);
		var eq0 = Rectangle.of(Point2d.of(0, 0), Size2d.of(20, 40));
		var ne0 = Rectangle.of(1, 0, 20, 40);
		var ne1 = Rectangle.of(0, -1, 20, 40);
		var ne2 = Rectangle.of(0, 0, 19, 40);
		var ne3 = Rectangle.of(0, 0, 20, 41);
		var ne4 = Rectangle.of(0, 0, 0, 0);
		var ne5 = Rectangle.square(Point2d.ZERO, 20);
		TestUtil.exerciseEquals(r, eq0);
		Assert.notEqualAll(r, ne0, ne1, ne2, ne3, ne4, ne5);
		Assert.equal(r.equals(Point2d.ZERO, Size2d.of(20, 40)), true);
		Assert.equal(r.equals(Point2d.ZERO, Size2d.of(21, 40)), false);
		Assert.equal(r.equals(Point2d.ZERO, Size2d.of(20, 41)), false);
	}

	@Test
	public void shouldCalculateArea() {
		Assert.equal(Rectangle.of(100, -200, 0, 0).area(), 0.0);
		Assert.equal(Rectangle.of(10, -20, 50, 20).area(), 1000.0);
	}

	@Test
	public void shouldExposeDimensions() {
		Rectangle r = Rectangle.of(100, -20, 50, 10);
		Assert.equal(r.position(), Point2d.of(100, -20));
		Assert.equal(r.size(), Size2d.of(50, 10));
		Assert.equal(r.corner(), Point2d.of(150, -10));
	}

	@Test
	public void shouldCalculateOverlap() {
		GeomAssert.approx(rt0.overlap(rt0), 0, 0, 200, 100);
		GeomAssert.approx(rt0.overlap(rt1), 0, 50, 50, 40);
		GeomAssert.approx(rt0.overlap(rt2), 0, 0, 0, 0);
		GeomAssert.approx(rt1.overlap(rt0), 0, 50, 50, 40);
		GeomAssert.approx(rt1.overlap(rt2), -40, 50, 0, 40);
		GeomAssert.approx(rt2.overlap(rt1), -40, 50, 0, 40);
		GeomAssert.approx(rt3.overlap(rt1), 0, 0, 0, 0);
	}
}
