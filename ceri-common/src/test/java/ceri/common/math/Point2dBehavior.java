package ceri.common.math;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class Point2dBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Point2d p0 = new Point2d(5, 10);
		Point2d p1 = new Point2d(5, 10);
		Point2d p2 = new Point2d(4.999, 10);
		Point2d p3 = new Point2d(5, 10.001);
		exerciseEquals(p0, p1);
		assertNotEquals(p0, p2);
		assertNotEquals(p0, p3);
	}

}
