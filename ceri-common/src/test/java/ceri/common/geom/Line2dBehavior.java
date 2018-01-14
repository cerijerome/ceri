package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
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
		assertThat(Line2d.ZERO.angle(), is(Double.NaN));
		assertThat(Line2d.of(-1, 1, -1, 1).angle(), is(Double.NaN));
		assertApprox(Line2d.of(-1, 1, 1, 3).angle(), 0.785);
	}

	@Test
	public void shouldCalculateGradient() {
		assertThat(Line2d.ZERO.gradient(), is(Double.NaN));
		assertThat(Line2d.of(-1, 1, -1, 1).gradient(), is(Double.NaN));
		assertThat(Line2d.of(-1, 1, -1, 3).gradient(), is(Double.POSITIVE_INFINITY));
		assertThat(Line2d.of(-1, 1, -1, -1).gradient(), is(Double.NEGATIVE_INFINITY));
		assertApprox(Line2d.of(-1, 1, 1, 3).gradient(), 1);
		assertApprox(Line2d.of(1, 3, -1, 1).gradient(), 1);
	}

	@Test
	public void shouldTranslate() {
		assertThat(l0.translate(Point2d.ZERO), is(l0));
		assertThat(l0.translate(Point2d.X_UNIT), is(Line2d.of(0, 2, 3, -1)));
		assertThat(l0.translate(Point2d.Y_UNIT), is(Line2d.of(-1, 3, 2, 0)));
	}

	@Test
	public void shouldReflectPoints() {
		assertThat(l0.reflect(Point2d.ZERO), is(Point2d.of(1, 1)));
		assertThat(l0.reflect(Point2d.of(1, 1)), is(Point2d.ZERO));
		assertThat(l0.reflect(l0.from), is(l0.from));
		assertThat(l0.reflect(l0.to), is(l0.to));
		assertThat(l1.reflect(Point2d.ZERO), is(Point2d.ZERO));
		assertThat(Line2d.X_UNIT.reflect(Point2d.of(-100, -100)), is(Point2d.of(-100, 100)));
		assertThat(Line2d.X_UNIT.reflect(Point2d.of(100, 100)), is(Point2d.of(100, -100)));
		assertThat(Line2d.Y_UNIT.reflect(Point2d.of(-100, -100)), is(Point2d.of(100, -100)));
		assertThat(Line2d.Y_UNIT.reflect(Point2d.of(100, 100)), is(Point2d.of(-100, 100)));
	}

	@Test
	public void shouldScale() {
		assertThat(l0.scale(Ratio2d.uniform(1.5)), is(Line2d.of(-1.5, 3, 3, -1.5)));
		assertThat(l0.scale(Ratio2d.ONE), is(l0));
		assertThat(l0.scale(Ratio2d.ZERO), is(Line2d.ZERO));
	}

	@Test
	public void shouldCalculateVector() {
		assertThat(l0.vector, is(Point2d.of(3, -3)));
		assertThat(l1.vector, is(Point2d.of(1, 3)));
	}

}
