package ceri.common.geom;

import static ceri.common.geom.Point2d.X_UNIT;
import static ceri.common.geom.Point2d.Y_UNIT;
import static ceri.common.math.MathUtil.PI_BY_2;
import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Math.PI;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		assertThat(Polar2d.ZERO.rotate(0), is(Polar2d.ZERO));
		assertThat(Polar2d.ZERO.rotate(10), is(Polar2d.of(0, 10)));
		assertThat(Polar2d.from(Point2d.X_UNIT).rotate(0), is(Polar2d.from(X_UNIT)));
		assertThat(Polar2d.of(1, 0).rotate(PI_BY_2), is(Polar2d.of(1, PI_BY_2)));
	}

	@Test
	public void shouldReverse() {
		assertThat(Polar2d.ZERO.reverse(), is(Polar2d.ZERO));
		assertThat(Polar2d.from(X_UNIT).reverse(), is(Polar2d.of(1, 0)));
		assertThat(Polar2d.from(Y_UNIT).reverse(), is(Polar2d.of(1, -PI_BY_2)));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		TestUtil.assertThrown(() -> Cone3d.create(-0.1, 2));
		assertThat(Polar2d.from(Point2d.ZERO), is(Polar2d.ZERO));
	}

	@Test
	public void shouldConvertPoints() {
		assertApprox(p0.asPoint().x, 1);
		assertApprox(p0.asPoint().y, 1.732);
		assertThat(p1.r, is(5.0));
		assertThat(p1.asPoint(), is(Point2d.of(4, 3)));

	}

}
