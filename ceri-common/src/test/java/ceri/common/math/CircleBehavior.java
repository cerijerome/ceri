package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class CircleBehavior {
	private final Circle c0 = new Circle(4);
	private final Circle c1 = new Circle(1);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(c0, new Circle(4));
		assertNotEquals(c0, new Circle(4.1));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		assertException(() -> new Circle(0));
		assertException(() -> new Circle(-1));
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
