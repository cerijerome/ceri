package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class Line2dBehavior {
	private final Line2d l0 = Line2d.of(Point2d.of(-1, 2), Point2d.of(2, -1));
	private final Line2d l1 = Line2d.of(Point2d.of(1, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(l0, Line2d.of(-1, 2, 2, -1));
		assertNotEquals(l0, Line2d.of(-1.1, 2, 2, -1));
		assertNotEquals(l0, Line2d.of(-1, 1.9, 2, -1));
		assertNotEquals(l0, Line2d.of(-1, 2, 2.1, -1));
		assertNotEquals(l0, Line2d.of(-1, 2, 2, -0.9));
		assertNotEquals(l0, Line2d.of(Point2d.ZERO, Point2d.ZERO));
		exerciseEquals(l1, Line2d.of(1, 3));
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
		assertEquals(l0.reflect(l0.from), l0.from);
		assertEquals(l0.reflect(l0.to), l0.to);
		assertEquals(l1.reflect(Point2d.ZERO), Point2d.ZERO);
		assertEquals(Line2d.X_UNIT.reflect(Point2d.of(-100, -100)), Point2d.of(-100, 100));
		assertEquals(Line2d.X_UNIT.reflect(Point2d.of(100, 100)), Point2d.of(100, -100));
		assertEquals(Line2d.Y_UNIT.reflect(Point2d.of(-100, -100)), Point2d.of(100, -100));
		assertEquals(Line2d.Y_UNIT.reflect(Point2d.of(100, 100)), Point2d.of(-100, 100));
	}

	@Test
	public void shouldScale() {
		assertEquals(l0.scale(Ratio2d.uniform(1.5)), Line2d.of(-1.5, 3, 3, -1.5));
		assertEquals(l0.scale(Ratio2d.ONE), l0);
		assertEquals(l0.scale(Ratio2d.ZERO), Line2d.ZERO);
	}

	@Test
	public void shouldCalculateVector() {
		assertEquals(l0.vector, Point2d.of(3, -3));
		assertEquals(l1.vector, Point2d.of(1, 3));
	}

}
