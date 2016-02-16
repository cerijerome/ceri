package ceri.common.geom;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import ceri.common.geom.Ratio2d;

public class Ratio2dBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Ratio2d r0 = new Ratio2d(5, 10);
		Ratio2d r1 = new Ratio2d(5, 10);
		Ratio2d r2 = new Ratio2d(4.999, 10);
		Ratio2d r3 = new Ratio2d(5, 10.001);
		Ratio2d r4 = Ratio2d.create(5);
		exerciseEquals(r0, r1);
		assertNotEquals(r0, r2);
		assertNotEquals(r0, r3);
		assertNotEquals(r0, r4);
	}

}
