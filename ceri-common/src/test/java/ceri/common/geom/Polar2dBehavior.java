package ceri.common.geom;

import static ceri.common.geom.Point2d.X_UNIT;
import static ceri.common.geom.Point2d.Y_UNIT;
import static ceri.common.math.Maths.PI_BY_2;
import static java.lang.Math.PI;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class Polar2dBehavior {
	private final Polar2d p0 = Polar2d.of(2, PI / 3);
	private final Polar2d p1 = Polar2d.from(Point2d.of(4, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		Testing.exerciseEquals(p0, Polar2d.of(2, PI / 3));
		Assert.notEqual(p0, Polar2d.of(2.1, PI / 3));
		Assert.notEqual(p0, Polar2d.of(2, PI / 3.1));
		Assert.notEqual(p0, Polar2d.of(0, PI / 3));
		Assert.notEqual(p0, Polar2d.of(2, 0));
		Assert.notEqual(p0, Polar2d.of(0, 0));
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
		Assert.equal(Polar2d.ZERO.rotate(0), Polar2d.ZERO);
		Assert.equal(Polar2d.ZERO.rotate(10), Polar2d.of(0, 10));
		Assert.equal(Polar2d.from(Point2d.X_UNIT).rotate(0), Polar2d.from(X_UNIT));
		Assert.equal(Polar2d.of(1, 0).rotate(PI_BY_2), Polar2d.of(1, PI_BY_2));
	}

	@Test
	public void shouldReverse() {
		Assert.equal(Polar2d.ZERO.reverse(), Polar2d.ZERO);
		Assert.equal(Polar2d.from(X_UNIT).reverse(), Polar2d.of(1, 0));
		Assert.equal(Polar2d.from(Y_UNIT).reverse(), Polar2d.of(1, -PI_BY_2));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		Assert.equal(Polar2d.from(Point2d.ZERO), Polar2d.ZERO);
	}

	@Test
	public void shouldConvertPoints() {
		Assert.approx(p0.point().x(), 1);
		Assert.approx(p0.point().y(), 1.732);
		Assert.equal(p1.r(), 5.0);
		Assert.equal(p1.point(), Point2d.of(4, 3));
	}
}
