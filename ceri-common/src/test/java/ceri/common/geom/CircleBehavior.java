package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CircleBehavior {
	private final Circle c0 = Circle.create(4);
	private final Circle c1 = Circle.create(1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, Circle.create(4));
		assertNotEquals(c0, Circle.create(4.1));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		assertException(() -> Circle.create(-0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Circle.create(0), is(Circle.NULL));
		assertTrue(Circle.NULL.isNull());
		assertFalse(c0.isNull());
	}

	@Test
	public void shouldCalculateCircumference() {
		assertApprox(c0.circumference(), 25.133);
		assertApprox(c1.circumference(), 6.283);
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(c0.area(), 50.265);
		assertApprox(c1.area(), 3.142);
	}

}
