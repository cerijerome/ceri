package ceri.common.math;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class Ratio2dBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Ratio2d r0 = new Ratio2d(5, 10);
		Ratio2d r1 = new Ratio2d(5, 10);
		Ratio2d r2 = new Ratio2d(4.999, 10);
		Ratio2d r3 = new Ratio2d(5, 10.001);
		exerciseEquals(r0, r1);
		assertNotEquals(r0, r2);
		assertNotEquals(r0, r3);
	}

}
