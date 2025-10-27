package ceri.common.geom;

import org.junit.Test;
import ceri.common.math.Maths;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class Line2dBehavior {
	private final Line2d l0 = Line2d.of(Point2d.of(-1, 2), Point2d.of(2, -1));
	private final Line2d l1 = Line2d.of(Point2d.of(1, 3));
	private final Line2d.Equation le0 = Line2d.Equation.of(4, -1, 3);
	private final Line2d.Equation le1 = Line2d.Equation.of(1, 0, -1);
	private final Line2d.Equation le2 = Line2d.Equation.of(0, -1, 1);

	@Test
	public void testAngleFromGradient() {
		Assert.approx(Line2d.angleFromGradient(Double.NaN), Double.NaN);
		Assert.approx(Line2d.angleFromGradient(0), 0);
		Assert.approx(Line2d.angleFromGradient(Double.POSITIVE_INFINITY), Maths.PI_BY_2);
		Assert.approx(Line2d.angleFromGradient(Double.NEGATIVE_INFINITY), -Maths.PI_BY_2);
		Assert.approx(Line2d.angleFromGradient(1), Math.PI / 4);
		Assert.approx(Line2d.angleFromGradient(-1), -Math.PI / 4);
	}

	@Test
	public void shouldCalculateLineEquationDistanceToPoint() {
		Assert.approx(Line2d.Equation.X_AXIS.distanceTo(Point2d.X_UNIT), 0);
		Assert.approx(Line2d.Equation.X_AXIS.distanceTo(0, 1), 1);
		Assert.approx(Line2d.Equation.of(1, -1, 0).distanceTo(0, 0), 0);
		Assert.approx(Line2d.Equation.of(1, -1, 0).distanceTo(1, 2), 0.707);
		Assert.equal(Line2d.Equation.ZERO.distanceTo(Point2d.ZERO), Double.NaN);
	}

	@Test
	public void shouldNotBreachLineEquationEqualsContract() {
		TestUtil.exerciseEquals(le0, Line2d.Equation.of(4, -1, 3));
		Assert.notEqual(le0, Line2d.Equation.of(3.9, -1, 3));
		Assert.notEqual(le0, Line2d.Equation.of(4, -0.9, 3));
		Assert.notEqual(le0, Line2d.Equation.of(4, -1, 3.1));
	}

	@Test
	public void shouldCalculateLineEquationAngle() {
		Assert.approx(le0.angle(), 1.326);
		Assert.approx(Line2d.Equation.of(4, 1, 3).angle(), -1.326);
		Assert.approx(Line2d.Equation.of(1, 4, 3).angle(), -0.245);
		Assert.approx(le1.angle(), 1.571);
		Assert.approx(le2.angle(), 0);
		Assert.equal(Line2d.Equation.ZERO.angle(), Double.NaN);
	}

	@Test
	public void shouldCalculateLineEquationGradient() {
		Assert.equal(le0.gradient(), 4.0);
		Assert.equal(Line2d.Equation.of(1, 4, 3).gradient(), -0.25);
		Assert.equal(le1.gradient(), Double.POSITIVE_INFINITY);
		Assert.equal(le2.gradient(), 0.0);
		Assert.equal(Line2d.Equation.ZERO.gradient(), Double.NaN);
	}

	@Test
	public void shouldNormalizeLineEquationToUnitXOrY() {
		GeomAssert.approx(le0.normalize(), 1, -0.25, 0.75);
		GeomAssert.approx(Line2d.Equation.of(0, 0, 0).normalize(), 0, 0, 0);
		GeomAssert.approx(Line2d.Equation.of(-1, 4, 2).normalize(), 1, -4, -2);
		GeomAssert.approx(Line2d.Equation.of(1, 4, 2).normalize(), 1, 4, 2);
		GeomAssert.approx(Line2d.Equation.of(4, -1, 0).normalize(), 1, -0.25, 0);
		GeomAssert.approx(Line2d.Equation.of(2, -2, 2).normalize(), 1, -1, 1);
		GeomAssert.approx(Line2d.Equation.of(-2, 2, 2).normalize(), 1, -1, -1);
		GeomAssert.approx(Line2d.Equation.of(0, -2, 2).normalize(), 0, 1, -1);
		GeomAssert.approx(Line2d.Equation.of(-3, 0, 6).normalize(), 1, 0, -2);
	}

	@Test
	public void shouldReflectLineEquationPoints() {
		Assert.equal(le0.reflect(-4, 4), Point2d.of(4, 2));
		Assert.equal(Line2d.Equation.ZERO.reflect(Point2d.X_UNIT), Point2d.X_UNIT);
		Assert.equal(Line2d.Equation.ZERO.reflect(Point2d.Y_UNIT), Point2d.Y_UNIT);
		Assert.equal(Line2d.Equation.ZERO.reflect(0, 0), Point2d.ZERO);
	}

	@Test
	public void shouldDefineLineEquationZero() {
		Assert.equal(Line2d.Equation.of(0, 0, 0), Line2d.Equation.ZERO);
		Assert.equal(Line2d.of(1, 1, 1, 1).equation(), Line2d.Equation.ZERO);
		Assert.equal(Line2d.Equation.ZERO.isZero(), true);
		Assert.equal(Line2d.Equation.of(0, 0, 1).isZero(), false);
	}

	@Test
	public void shouldHaveNaturalStringRepresentation() {
		Assert.string(Line2d.Equation.of(0, 0, 0), "0 = 0");
		Assert.string(Line2d.Equation.of(1, 0, 0), "x = 0");
		Assert.string(Line2d.Equation.of(-1, 0, 0), "-x = 0");
		Assert.string(Line2d.Equation.of(0, 1, 0), "y = 0");
		Assert.string(Line2d.Equation.of(0, -1, 0), "-y = 0");
		Assert.string(Line2d.Equation.of(-2, -2, -2), "-2x - 2y - 2 = 0");
		Assert.string(Line2d.Equation.of(1, 0, -1), "x - 1 = 0");
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		TestUtil.exerciseEquals(l0, Line2d.of(-1, 2, 2, -1));
		Assert.notEqual(l0, Line2d.of(-1.1, 2, 2, -1));
		Assert.notEqual(l0, Line2d.of(-1, 1.9, 2, -1));
		Assert.notEqual(l0, Line2d.of(-1, 2, 2.1, -1));
		Assert.notEqual(l0, Line2d.of(-1, 2, 2, -0.9));
		Assert.notEqual(l0, Line2d.of(Point2d.ZERO, Point2d.ZERO));
		TestUtil.exerciseEquals(l1, Line2d.of(1, 3));
		Assert.equal(l1.equals(0, 0, 1, 3), true);
		Assert.equal(l1.equals(1, 0, 1, 3), false);
		Assert.equal(l1.equals(0, 1, 1, 3), false);
		Assert.equal(l1.equals(0, 0, 2, 3), false);
		Assert.equal(l1.equals(0, 0, 1, 2), false);
	}

	@Test
	public void shouldDetermineIfZero() {
		Assert.equal(Line2d.ZERO.isZero(), true);
		Assert.equal(Line2d.of(Point2d.ZERO, Point2d.ZERO).isZero(), true);
		Assert.equal(Line2d.of(Point2d.ZERO, Point2d.X_UNIT).isZero(), false);
		Assert.equal(Line2d.of(Point2d.ZERO, Point2d.Y_UNIT).isZero(), false);
	}

	@Test
	public void shouldCalculateAngle() {
		Assert.equal(Line2d.ZERO.angle(), Double.NaN);
		Assert.equal(Line2d.of(-1, 1, -1, 1).angle(), Double.NaN);
		Assert.approx(Line2d.of(-1, 1, 1, 3).angle(), 0.785);
	}

	@Test
	public void shouldCalculateGradient() {
		Assert.equal(Line2d.ZERO.gradient(), Double.NaN);
		Assert.equal(Line2d.of(-1, 1, -1, 1).gradient(), Double.NaN);
		Assert.equal(Line2d.of(-1, 1, -1, 3).gradient(), Double.POSITIVE_INFINITY);
		Assert.equal(Line2d.of(-1, 1, -1, -1).gradient(), Double.NEGATIVE_INFINITY);
		Assert.approx(Line2d.of(-1, 1, 1, 3).gradient(), 1);
		Assert.approx(Line2d.of(1, 3, -1, 1).gradient(), 1);
	}

	@Test
	public void shouldTranslate() {
		Assert.equal(l0.translate(Point2d.ZERO), l0);
		GeomAssert.approx(l0.translate(Point2d.X_UNIT), 0, 2, 3, -1);
		GeomAssert.approx(l0.translate(Point2d.Y_UNIT), -1, 3, 2, 0);
	}

	@Test
	public void shouldReflectPoints() {
		GeomAssert.approx(l0.reflect(Point2d.ZERO), 1, 1);
		Assert.equal(l0.reflect(Point2d.of(1, 1)), Point2d.ZERO);
		Assert.equal(l0.reflect(l0.from()), l0.from());
		Assert.equal(l0.reflect(l0.to()), l0.to());
		Assert.equal(l1.reflect(Point2d.ZERO), Point2d.ZERO);
		GeomAssert.approx(Line2d.X_UNIT.reflect(Point2d.of(-100, -100)), -100, 100);
		GeomAssert.approx(Line2d.X_UNIT.reflect(Point2d.of(100, 100)), 100, -100);
		GeomAssert.approx(Line2d.Y_UNIT.reflect(Point2d.of(-100, -100)), 100, -100);
		GeomAssert.approx(Line2d.Y_UNIT.reflect(Point2d.of(100, 100)), -100, 100);
	}

	@Test
	public void shouldScale() {
		GeomAssert.approx(l0.scale(Ratio2d.uniform(1.5)), -1.5, 3, 3, -1.5);
		Assert.equal(l0.scale(Ratio2d.UNIT), l0);
		Assert.equal(l0.scale(Ratio2d.ZERO), Line2d.ZERO);
	}

	@Test
	public void shouldCalculateLength() {
		Assert.equal(Line2d.ZERO.length(), 0.0);
		Assert.equal(Line2d.X_UNIT.length(), 1.0);
		Assert.equal(Line2d.Y_UNIT.length(), 1.0);
		Assert.equal(Line2d.of(3, 4).length(), 5.0);
	}

	@Test
	public void shouldCalculateQuadrance() {
		Assert.equal(Line2d.ZERO.quadrance(), 0.0);
		Assert.equal(Line2d.X_UNIT.quadrance(), 1.0);
		Assert.equal(Line2d.Y_UNIT.quadrance(), 1.0);
		Assert.equal(Line2d.of(3, 4).quadrance(), 25.0);
	}

	@Test
	public void shouldCalculateOffset() {
		GeomAssert.approx(l0.offset(), 3, -3);
		GeomAssert.approx(l1.offset(), 1, 3);
	}

	@Test
	public void shouldCalculateDistanceToPoint() {
		Assert.equal(Line2d.ZERO.distanceTo(Point2d.ZERO), 0.0);
		Assert.equal(Line2d.ZERO.distanceTo(1, 0), 1.0);
		Assert.equal(Line2d.ZERO.distanceTo(0, 1), 1.0);
		Assert.equal(Line2d.ZERO.distanceTo(-3, -4), 5.0);
		var line = Line2d.of(Point2d.Y_UNIT, Point2d.ZERO);
		Assert.equal(line.distanceTo(0, 0), 0.0);
		Assert.equal(line.distanceTo(1, 0), 1.0);
		Assert.equal(line.distanceTo(0, 1), 0.0);
		Assert.equal(line.distanceTo(0, 3), 2.0);
		Assert.equal(line.distanceTo(0, -2), 2.0);
		Assert.equal(line.distanceTo(3, -4), 5.0);
		Assert.equal(line.distanceTo(-4, 4), 5.0);
		line = Line2d.of(Point2d.X_UNIT, Point2d.X_UNIT);
		Assert.equal(line.distanceTo(0, 0), 1.0);
		Assert.equal(line.distanceTo(1, 0), 0.0);
		Assert.approx(line.distanceTo(0, -1), 1.4142);
	}
}
