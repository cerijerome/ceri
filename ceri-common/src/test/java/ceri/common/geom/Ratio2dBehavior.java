package ceri.common.geom;

import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class Ratio2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Ratio2d r0 = Ratio2d.of(5, 10);
		Ratio2d r1 = Ratio2d.of(5, 10);
		Ratio2d r2 = Ratio2d.of(4.999, 10);
		Ratio2d r3 = Ratio2d.of(5, 10.001);
		Ratio2d r4 = Ratio2d.uniform(5);
		exerciseEquals(r0, r1);
		assertNotEquals(r0, r2);
		assertNotEquals(r0, r3);
		assertNotEquals(r0, r4);
	}

}
