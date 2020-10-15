package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.NaN;
import org.junit.Test;

public class ReverseFunctionBehavior {
	private static final ReverseFunction f0 =
		ReverseFunction.create(0, Math.PI / 2, 100, Math::sin);
	private static final ReverseFunction f1 =
		ReverseFunction.create(-0.99, 0.99, 100, x -> x * x * x);

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
	public void shouldHandleZeroLookupEntries() {
		ReverseFunction f = ReverseFunction.builder().build();
		assertApprox(f.x(-1), NaN);
		assertApprox(f.x(0), NaN);
		assertApprox(f.x(1), NaN);
	}

	@Test
	public void shouldHandleSingleLookupEntry() {
		ReverseFunction f = ReverseFunction.builder().add(1, 1).build();
		assertApprox(f.x(0), 1);
		assertApprox(f.x(1), 1);
		assertApprox(f.x(2), 1);
	}

	@Test
	public void shouldExtrapolateValuesOutsideTheRange() {
		assertApprox(f0.x(-0.001), -0.001);
		assertApprox(f0.x(1.001), 1.698);
		assertApprox(f1.x(-1), -1);
		assertApprox(f1.x(0), 0);
		assertApprox(f1.x(1), 1);
	}

	@Test
	public void shouldLookUpXFromY() {
		assertApprox(f0.x(0), 0);
		assertApprox(f0.x(0.5), Math.PI / 6);
		assertApprox(f0.x(Math.sqrt(3) / 2), Math.PI / 3);
		assertApprox(f0.x(1), Math.PI / 2);
		assertApprox(f1.x(-0.729), -0.9);
		assertApprox(f1.x(-0.125), -0.5);
		assertApprox(f1.x(0), 0);
		assertApprox(f1.x(0.125), 0.5);
		assertApprox(f1.x(0.729), 0.9);
	}

}
