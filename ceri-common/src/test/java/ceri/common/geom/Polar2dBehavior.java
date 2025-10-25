package ceri.common.geom;

import static ceri.common.geom.Point2d.X_UNIT;
import static ceri.common.geom.Point2d.Y_UNIT;
import static ceri.common.math.Maths.PI_BY_2;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import static java.lang.Math.PI;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class Polar2dBehavior {
	private final Polar2d p0 = Polar2d.of(2, PI / 3);
	private final Polar2d p1 = Polar2d.from(Point2d.of(4, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(p0, Polar2d.of(2, PI / 3));
		assertNotEquals(p0, Polar2d.of(2.1, PI / 3));
		assertNotEquals(p0, Polar2d.of(2, PI / 3.1));
		assertNotEquals(p0, Polar2d.of(0, PI / 3));
		assertNotEquals(p0, Polar2d.of(2, 0));
		assertNotEquals(p0, Polar2d.of(0, 0));
	}

	@Test
	public void shouldNormalize() {
		GeomAssert.approx(Polar2d.ZERO.normalize(), 0, 0);
		GeomAssert.approx(Polar2d.of(1, Math.PI).normalize(), 1, Math.PI);
		GeomAssert.approx(Polar2d.of(1, -Math.PI).normalize(), 1, Math.PI);
		GeomAssert.approx(Polar2d.of(1, Math.PI * 2.5).normalize(), 1, Math.PI * 0.5);
		GeomAssert.approx(Polar2d.of(1, -Math.PI * 2.5).normalize(), 1, Math.PI * 1.5);
	}

	@Test
	public void shouldRotate() {
		assertEquals(Polar2d.ZERO.rotate(0), Polar2d.ZERO);
		assertEquals(Polar2d.ZERO.rotate(10), Polar2d.of(0, 10));
		assertEquals(Polar2d.from(Point2d.X_UNIT).rotate(0), Polar2d.from(X_UNIT));
		assertEquals(Polar2d.of(1, 0).rotate(PI_BY_2), Polar2d.of(1, PI_BY_2));
	}

	@Test
	public void shouldReverse() {
		assertEquals(Polar2d.ZERO.reverse(), Polar2d.ZERO);
		assertEquals(Polar2d.from(X_UNIT).reverse(), Polar2d.of(1, 0));
		assertEquals(Polar2d.from(Y_UNIT).reverse(), Polar2d.of(1, -PI_BY_2));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		assertEquals(Polar2d.from(Point2d.ZERO), Polar2d.ZERO);
	}

	@Test
	public void shouldConvertPoints() {
		Assert.approx(p0.point().x(), 1);
		Assert.approx(p0.point().y(), 1.732);
		assertEquals(p1.r(), 5.0);
		assertEquals(p1.point(), Point2d.of(4, 3));
	}
}
