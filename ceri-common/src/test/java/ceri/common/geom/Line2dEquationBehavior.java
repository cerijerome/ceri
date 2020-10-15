package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertNaN;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class Line2dEquationBehavior {
	private final LineEquation2d l0 = LineEquation2d.of(4, -1, 3);
	private final LineEquation2d l1 = LineEquation2d.of(1, 0, -1);
	private final LineEquation2d l2 = LineEquation2d.of(0, -1, 1);

	@Test
	public void shouldCalculateDistanceToPoint() {
		assertApprox(LineEquation2d.X_AXIS.distanceTo(Point2d.X_UNIT), 0);
		assertApprox(LineEquation2d.X_AXIS.distanceTo(0, 1), 1);
		assertApprox(LineEquation2d.of(1, -1, 0).distanceTo(0, 0), 0);
		assertApprox(LineEquation2d.of(1, -1, 0).distanceTo(1, 2), 0.707);
		assertNaN(LineEquation2d.NULL.distanceTo(Point2d.ZERO));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(l0, LineEquation2d.of(4, -1, 3));
		assertNotEquals(l0, LineEquation2d.of(3.9, -1, 3));
		assertNotEquals(l0, LineEquation2d.of(4, -0.9, 3));
		assertNotEquals(l0, LineEquation2d.of(4, -1, 3.1));
	}

	@Test
	public void shouldCalculateAngle() {
		assertApprox(l0.angle(), 1.326);
		assertApprox(LineEquation2d.of(4, 1, 3).angle(), -1.326);
		assertApprox(LineEquation2d.of(1, 4, 3).angle(), -0.245);
		assertApprox(l1.angle(), 1.571);
		assertApprox(l2.angle(), 0);
		assertThat(LineEquation2d.NULL.angle(), is(Double.NaN));
	}

	@Test
	public void shouldCalculateGradient() {
		assertThat(l0.gradient(), is(4.0));
		assertThat(LineEquation2d.of(1, 4, 3).gradient(), is(-0.25));
		assertThat(l1.gradient(), is(Double.POSITIVE_INFINITY));
		assertThat(l2.gradient(), is(0.0));
		assertThat(LineEquation2d.NULL.gradient(), is(Double.NaN));
	}

	@Test
	public void shouldNormalizeToUnitXOrY() {
		assertThat(l0.normalize(), is(LineEquation2d.of(1, -0.25, 0.75)));
		assertThat(LineEquation2d.of(0, 0, 0).normalize(), is(LineEquation2d.of(0, 0, 0)));
		assertThat(LineEquation2d.of(-1, 4, 2).normalize(), is(LineEquation2d.of(1, -4, -2)));
		assertThat(LineEquation2d.of(1, 4, 2).normalize(), is(LineEquation2d.of(1, 4, 2)));
		assertThat(LineEquation2d.of(4, -1, 0).normalize(), is(LineEquation2d.of(1, -0.25, 0)));
		assertThat(LineEquation2d.of(2, -2, 2).normalize(), is(LineEquation2d.of(1, -1, 1)));
		assertThat(LineEquation2d.of(-2, 2, 2).normalize(), is(LineEquation2d.of(1, -1, -1)));
		assertThat(LineEquation2d.of(0, -2, 2).normalize(), is(LineEquation2d.of(0, 1, -1)));
		assertThat(LineEquation2d.of(-3, 0, 6).normalize(), is(LineEquation2d.of(1, 0, -2)));
	}

	@Test
	public void shouldReflectPoints() {
		assertThat(l0.reflect(-4, 4), is(Point2d.of(4, 2)));
		assertThat(LineEquation2d.NULL.reflect(Point2d.X_UNIT), is(Point2d.X_UNIT));
		assertThat(LineEquation2d.NULL.reflect(Point2d.Y_UNIT), is(Point2d.Y_UNIT));
		assertThat(LineEquation2d.NULL.reflect(0, 0), is(Point2d.ZERO));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(LineEquation2d.of(0, 0, 0), is(LineEquation2d.NULL));
		assertThat(LineEquation2d.of(0, 0, 1), is(LineEquation2d.NULL));
		assertThat(LineEquation2d.of(Line2d.of(1, 1, 1, 1)), is(LineEquation2d.NULL));
		assertTrue(LineEquation2d.NULL.isNull());
	}

	@Test
	public void shouldHaveNaturalStringRepresentation() {
		assertThat(LineEquation2d.of(0, 0, 0).toString(), is("0 = 0"));
		assertThat(LineEquation2d.of(1, 0, 0).toString(), is("x = 0"));
		assertThat(LineEquation2d.of(-1, 0, 0).toString(), is("-x = 0"));
		assertThat(LineEquation2d.of(0, 1, 0).toString(), is("y = 0"));
		assertThat(LineEquation2d.of(0, -1, 0).toString(), is("-y = 0"));
		assertThat(LineEquation2d.of(-2, -2, -2).toString(), is("-2x - 2y - 2 = 0"));
		assertThat(LineEquation2d.of(1, 0, -1).toString(), is("x - 1 = 0"));
	}

}
