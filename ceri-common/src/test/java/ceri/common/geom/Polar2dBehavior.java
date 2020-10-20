package ceri.common.geom;

import static ceri.common.geom.Point2d.X_UNIT;
import static ceri.common.geom.Point2d.Y_UNIT;
import static ceri.common.math.MathUtil.PI_BY_2;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Math.PI;
import org.junit.Test;

public class Polar2dBehavior {
	private final Polar2d p0 = Polar2d.of(2, PI / 3);
	private final Polar2d p1 = Polar2d.from(Point2d.of(4, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(p0, Polar2d.of(2, PI / 3));
		assertNotEquals(p0, Polar2d.of(2.1, PI / 3));
		assertNotEquals(p0, Polar2d.of(2, PI / 3.1));
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
		assertThrown(() -> Cone3d.create(-0.1, 2));
		assertEquals(Polar2d.from(Point2d.ZERO), Polar2d.ZERO);
	}

	@Test
	public void shouldConvertPoints() {
		assertApprox(p0.asPoint().x, 1);
		assertApprox(p0.asPoint().y, 1.732);
		assertEquals(p1.r, 5.0);
		assertEquals(p1.asPoint(), Point2d.of(4, 3));

	}

}
