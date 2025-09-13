package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.exerciseEquals(l0, LineEquation2d.of(4, -1, 3));
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
		assertEquals(LineEquation2d.NULL.angle(), Double.NaN);
	}

	@Test
	public void shouldCalculateGradient() {
		assertEquals(l0.gradient(), 4.0);
		assertEquals(LineEquation2d.of(1, 4, 3).gradient(), -0.25);
		assertEquals(l1.gradient(), Double.POSITIVE_INFINITY);
		assertEquals(l2.gradient(), 0.0);
		assertEquals(LineEquation2d.NULL.gradient(), Double.NaN);
	}

	@Test
	public void shouldNormalizeToUnitXOrY() {
		assertEquals(l0.normalize(), LineEquation2d.of(1, -0.25, 0.75));
		assertEquals(LineEquation2d.of(0, 0, 0).normalize(), LineEquation2d.of(0, 0, 0));
		assertEquals(LineEquation2d.of(-1, 4, 2).normalize(), LineEquation2d.of(1, -4, -2));
		assertEquals(LineEquation2d.of(1, 4, 2).normalize(), LineEquation2d.of(1, 4, 2));
		assertEquals(LineEquation2d.of(4, -1, 0).normalize(), LineEquation2d.of(1, -0.25, 0));
		assertEquals(LineEquation2d.of(2, -2, 2).normalize(), LineEquation2d.of(1, -1, 1));
		assertEquals(LineEquation2d.of(-2, 2, 2).normalize(), LineEquation2d.of(1, -1, -1));
		assertEquals(LineEquation2d.of(0, -2, 2).normalize(), LineEquation2d.of(0, 1, -1));
		assertEquals(LineEquation2d.of(-3, 0, 6).normalize(), LineEquation2d.of(1, 0, -2));
	}

	@Test
	public void shouldReflectPoints() {
		assertEquals(l0.reflect(-4, 4), Point2d.of(4, 2));
		assertEquals(LineEquation2d.NULL.reflect(Point2d.X_UNIT), Point2d.X_UNIT);
		assertEquals(LineEquation2d.NULL.reflect(Point2d.Y_UNIT), Point2d.Y_UNIT);
		assertEquals(LineEquation2d.NULL.reflect(0, 0), Point2d.ZERO);
	}

	@Test
	public void shouldDefineNull() {
		assertEquals(LineEquation2d.of(0, 0, 0), LineEquation2d.NULL);
		assertEquals(LineEquation2d.of(0, 0, 1), LineEquation2d.NULL);
		assertEquals(LineEquation2d.of(Line2d.of(1, 1, 1, 1)), LineEquation2d.NULL);
		assertTrue(LineEquation2d.NULL.isNull());
	}

	@Test
	public void shouldHaveNaturalStringRepresentation() {
		assertEquals(LineEquation2d.of(0, 0, 0).toString(), "0 = 0");
		assertEquals(LineEquation2d.of(1, 0, 0).toString(), "x = 0");
		assertEquals(LineEquation2d.of(-1, 0, 0).toString(), "-x = 0");
		assertEquals(LineEquation2d.of(0, 1, 0).toString(), "y = 0");
		assertEquals(LineEquation2d.of(0, -1, 0).toString(), "-y = 0");
		assertEquals(LineEquation2d.of(-2, -2, -2).toString(), "-2x - 2y - 2 = 0");
		assertEquals(LineEquation2d.of(1, 0, -1).toString(), "x - 1 = 0");
	}

}
