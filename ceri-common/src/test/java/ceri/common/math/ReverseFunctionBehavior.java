package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class ReverseFunctionBehavior {
	private static final ReverseFunction f0 = ReverseFunction
		.create(0, Math.PI / 2, 100, Math::sin);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(f0, ReverseFunction.create(0, Math.PI / 2, 100, Math::sin));
		assertEquals(f0, ReverseFunction.builder().add(f0.values).build());
		assertNotEquals(f0, ReverseFunction.create(0.1, Math.PI / 2, 100, Math::sin));
		assertNotEquals(f0, ReverseFunction.create(0, Math.PI / 1.9, 100, Math::sin));
		assertNotEquals(f0, ReverseFunction.create(0, Math.PI / 2, 101, Math::sin));
		assertNotEquals(f0, ReverseFunction.create(0, Math.PI / 2, 100, Math::sinh));
	}

	@Test
	public void shouldExtrapolateValuesOutsideTheRange() {
		assertApprox(f0.x(-0.001), -0.001);
		assertApprox(f0.x(1.001), 1.571);
	}

	@Test
	public void shouldLookUpXFromY() {
		assertApprox(f0.x(0), 0);
		assertApprox(f0.x(0.5), Math.PI / 6);
		assertApprox(f0.x(Math.sqrt(3) / 2), Math.PI / 3);
		assertApprox(f0.x(1), Math.PI / 2);
	}

}
