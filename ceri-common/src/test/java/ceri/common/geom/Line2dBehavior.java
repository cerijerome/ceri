package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Line2dBehavior {
	private final Line2d l0 = Line2d.of(Point2d.of(-1, 2), Point2d.of(2, -1));
	private final Line2d l1 = Line2d.of(Point2d.of(1, 3));
	private final Line2d.Equation le0 = Line2d.Equation.of(4, -1, 3);
	private final Line2d.Equation le1 = Line2d.Equation.of(1, 0, -1);
	private final Line2d.Equation le2 = Line2d.Equation.of(0, -1, 1);

	@Test
	public void shouldCalculateLineEquationDistanceToPoint() {
		assertApprox(Line2d.Equation.X_AXIS.distanceTo(Point2d.X_UNIT), 0);
		assertApprox(Line2d.Equation.X_AXIS.distanceTo(0, 1), 1);
		assertApprox(Line2d.Equation.of(1, -1, 0).distanceTo(0, 0), 0);
		assertApprox(Line2d.Equation.of(1, -1, 0).distanceTo(1, 2), 0.707);
		assertNaN(Line2d.Equation.NULL.distanceTo(Point2d.ZERO));
	}

	@Test
	public void shouldNotBreachLineEquationEqualsContract() {
		TestUtil.exerciseEquals(le0, Line2d.Equation.of(4, -1, 3));
		assertNotEquals(le0, Line2d.Equation.of(3.9, -1, 3));
		assertNotEquals(le0, Line2d.Equation.of(4, -0.9, 3));
		assertNotEquals(le0, Line2d.Equation.of(4, -1, 3.1));
	}

	@Test
	public void shouldCalculateLineEquationAngle() {
		assertApprox(le0.angle(), 1.326);
		assertApprox(Line2d.Equation.of(4, 1, 3).angle(), -1.326);
		assertApprox(Line2d.Equation.of(1, 4, 3).angle(), -0.245);
		assertApprox(le1.angle(), 1.571);
		assertApprox(le2.angle(), 0);
		assertEquals(Line2d.Equation.NULL.angle(), Double.NaN);
	}

	@Test
	public void shouldCalculateLineEquationGradient() {
		assertEquals(le0.gradient(), 4.0);
		assertEquals(Line2d.Equation.of(1, 4, 3).gradient(), -0.25);
		assertEquals(le1.gradient(), Double.POSITIVE_INFINITY);
		assertEquals(le2.gradient(), 0.0);
		assertEquals(Line2d.Equation.NULL.gradient(), Double.NaN);
	}

	@Test
	public void shouldNormalizeLineEquationToUnitXOrY() {
		assertEquals(le0.normalize(), Line2d.Equation.of(1, -0.25, 0.75));
		assertEquals(Line2d.Equation.of(0, 0, 0).normalize(), Line2d.Equation.of(0, 0, 0));
		assertEquals(Line2d.Equation.of(-1, 4, 2).normalize(), Line2d.Equation.of(1, -4, -2));
		assertEquals(Line2d.Equation.of(1, 4, 2).normalize(), Line2d.Equation.of(1, 4, 2));
		assertEquals(Line2d.Equation.of(4, -1, 0).normalize(), Line2d.Equation.of(1, -0.25, 0));
		assertEquals(Line2d.Equation.of(2, -2, 2).normalize(), Line2d.Equation.of(1, -1, 1));
		assertEquals(Line2d.Equation.of(-2, 2, 2).normalize(), Line2d.Equation.of(1, -1, -1));
		assertEquals(Line2d.Equation.of(0, -2, 2).normalize(), Line2d.Equation.of(0, 1, -1));
		assertEquals(Line2d.Equation.of(-3, 0, 6).normalize(), Line2d.Equation.of(1, 0, -2));
	}

	@Test
	public void shouldReflectLineEquationPoints() {
		assertEquals(le0.reflect(-4, 4), Point2d.of(4, 2));
		assertEquals(Line2d.Equation.NULL.reflect(Point2d.X_UNIT), Point2d.X_UNIT);
		assertEquals(Line2d.Equation.NULL.reflect(Point2d.Y_UNIT), Point2d.Y_UNIT);
		assertEquals(Line2d.Equation.NULL.reflect(0, 0), Point2d.ZERO);
	}

	@Test
	public void shouldDefineLineEquationNull() {
		assertEquals(Line2d.Equation.of(0, 0, 0), Line2d.Equation.NULL);
		assertEquals(Line2d.Equation.of(0, 0, 1), Line2d.Equation.NULL);
		assertEquals(Line2d.of(1, 1, 1, 1).equation(), Line2d.Equation.NULL);
		assertTrue(Line2d.Equation.NULL.isNull());
	}

	@Test
	public void shouldHaveNaturalStringRepresentation() {
		assertEquals(Line2d.Equation.of(0, 0, 0).toString(), "0 = 0");
		assertEquals(Line2d.Equation.of(1, 0, 0).toString(), "x = 0");
		assertEquals(Line2d.Equation.of(-1, 0, 0).toString(), "-x = 0");
		assertEquals(Line2d.Equation.of(0, 1, 0).toString(), "y = 0");
		assertEquals(Line2d.Equation.of(0, -1, 0).toString(), "-y = 0");
		assertEquals(Line2d.Equation.of(-2, -2, -2).toString(), "-2x - 2y - 2 = 0");
		assertEquals(Line2d.Equation.of(1, 0, -1).toString(), "x - 1 = 0");
	}
	
	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(l0, Line2d.of(-1, 2, 2, -1));
		assertNotEquals(l0, Line2d.of(-1.1, 2, 2, -1));
		assertNotEquals(l0, Line2d.of(-1, 1.9, 2, -1));
		assertNotEquals(l0, Line2d.of(-1, 2, 2.1, -1));
		assertNotEquals(l0, Line2d.of(-1, 2, 2, -0.9));
		assertNotEquals(l0, Line2d.of(Point2d.ZERO, Point2d.ZERO));
		TestUtil.exerciseEquals(l1, Line2d.of(1, 3));
	}

	@Test
	public void shouldCalculateAngle() {
		assertEquals(Line2d.ZERO.angle(), Double.NaN);
		assertEquals(Line2d.of(-1, 1, -1, 1).angle(), Double.NaN);
		assertApprox(Line2d.of(-1, 1, 1, 3).angle(), 0.785);
	}

	@Test
	public void shouldCalculateGradient() {
		assertEquals(Line2d.ZERO.gradient(), Double.NaN);
		assertEquals(Line2d.of(-1, 1, -1, 1).gradient(), Double.NaN);
		assertEquals(Line2d.of(-1, 1, -1, 3).gradient(), Double.POSITIVE_INFINITY);
		assertEquals(Line2d.of(-1, 1, -1, -1).gradient(), Double.NEGATIVE_INFINITY);
		assertApprox(Line2d.of(-1, 1, 1, 3).gradient(), 1);
		assertApprox(Line2d.of(1, 3, -1, 1).gradient(), 1);
	}

	@Test
	public void shouldTranslate() {
		assertEquals(l0.translate(Point2d.ZERO), l0);
		assertEquals(l0.translate(Point2d.X_UNIT), Line2d.of(0, 2, 3, -1));
		assertEquals(l0.translate(Point2d.Y_UNIT), Line2d.of(-1, 3, 2, 0));
	}

	@Test
	public void shouldReflectPoints() {
		assertEquals(l0.reflect(Point2d.ZERO), Point2d.of(1, 1));
		assertEquals(l0.reflect(Point2d.of(1, 1)), Point2d.ZERO);
		assertEquals(l0.reflect(l0.from()), l0.from());
		assertEquals(l0.reflect(l0.to()), l0.to());
		assertEquals(l1.reflect(Point2d.ZERO), Point2d.ZERO);
		assertEquals(Line2d.X_UNIT.reflect(Point2d.of(-100, -100)), Point2d.of(-100, 100));
		assertEquals(Line2d.X_UNIT.reflect(Point2d.of(100, 100)), Point2d.of(100, -100));
		assertEquals(Line2d.Y_UNIT.reflect(Point2d.of(-100, -100)), Point2d.of(100, -100));
		assertEquals(Line2d.Y_UNIT.reflect(Point2d.of(100, 100)), Point2d.of(-100, 100));
	}

	@Test
	public void shouldScale() {
		assertEquals(l0.scale(Ratio2d.uniform(1.5)), Line2d.of(-1.5, 3, 3, -1.5));
		assertEquals(l0.scale(Ratio2d.UNIT), l0);
		assertEquals(l0.scale(Ratio2d.ZERO), Line2d.ZERO);
	}

	@Test
	public void shouldCalculateVector() {
		assertEquals(l0.vector(), Point2d.of(3, -3));
		assertEquals(l1.vector(), Point2d.of(1, 3));
	}

	@Test
	public void shouldCalculateDistanceToPoint() {
		assertEquals(Line2d.ZERO.distanceTo(0, 0), 0.0);
		assertEquals(Line2d.ZERO.distanceTo(1, 0), 1.0);
		assertEquals(Line2d.ZERO.distanceTo(0, 1), 1.0);
		assertEquals(Line2d.ZERO.distanceTo(-3, -4), 5.0);
		var line = Line2d.of(Point2d.Y_UNIT, Point2d.ZERO);
		assertEquals(line.distanceTo(0, 0), 0.0);
		assertEquals(line.distanceTo(1, 0), 1.0);
		assertEquals(line.distanceTo(0, 1), 0.0);
		assertEquals(line.distanceTo(0, 3), 2.0);
		assertEquals(line.distanceTo(0, -2), 2.0);
		assertEquals(line.distanceTo(3, -4), 5.0);
		assertEquals(line.distanceTo(-4, 4), 5.0);
		line = Line2d.of(Point2d.X_UNIT, Point2d.X_UNIT);
		assertEquals(line.distanceTo(0, 0), 1.0);
		assertEquals(line.distanceTo(1, 0), 0.0);
		assertApprox(line.distanceTo(0, -1), 1.4142);
	}
}
