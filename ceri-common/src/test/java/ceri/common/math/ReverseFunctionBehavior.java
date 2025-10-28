package ceri.common.math;

import static java.lang.Double.NaN;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class ReverseFunctionBehavior {
	private static final ReverseFunction f0 =
		ReverseFunction.from(0, Math.PI / 2, 100, Math::sin);
	private static final ReverseFunction f1 =
		ReverseFunction.from(-0.99, 0.99, 100, x -> x * x * x);

	@Test
	public void shouldNotBreachEqualsContract() {
		Testing.exerciseEquals(f0, ReverseFunction.from(0, Math.PI / 2, 100, Math::sin));
		Assert.equal(f0, ReverseFunction.builder().add(f0.values).build());
		Assert.notEqual(f0, ReverseFunction.from(0.1, Math.PI / 2, 100, Math::sin));
		Assert.notEqual(f0, ReverseFunction.from(0, Math.PI / 1.9, 100, Math::sin));
		Assert.notEqual(f0, ReverseFunction.from(0, Math.PI / 2, 101, Math::sin));
		Assert.notEqual(f0, ReverseFunction.from(0, Math.PI / 2, 100, Math::sinh));
	}

	@Test
	public void shouldHandleZeroLookupEntries() {
		ReverseFunction f = ReverseFunction.builder().build();
		Assert.approx(f.x(-1), NaN);
		Assert.approx(f.x(0), NaN);
		Assert.approx(f.x(1), NaN);
	}

	@Test
	public void shouldHandleSingleLookupEntry() {
		ReverseFunction f = ReverseFunction.builder().add(1, 1).build();
		Assert.approx(f.x(0), 1);
		Assert.approx(f.x(1), 1);
		Assert.approx(f.x(2), 1);
	}

	@Test
	public void shouldExtrapolateValuesOutsideTheRange() {
		Assert.approx(f0.x(-0.001), -0.001);
		Assert.approx(f0.x(1.001), 1.698);
		Assert.approx(f1.x(-1), -1);
		Assert.approx(f1.x(0), 0);
		Assert.approx(f1.x(1), 1);
	}

	@Test
	public void shouldLookUpXFromY() {
		Assert.approx(f0.x(0), 0);
		Assert.approx(f0.x(0.5), Math.PI / 6);
		Assert.approx(f0.x(Math.sqrt(3) / 2), Math.PI / 3);
		Assert.approx(f0.x(1), Math.PI / 2);
		Assert.approx(f1.x(-0.729), -0.9);
		Assert.approx(f1.x(-0.125), -0.5);
		Assert.approx(f1.x(0), 0);
		Assert.approx(f1.x(0.125), 0.5);
		Assert.approx(f1.x(0.729), 0.9);
	}

}
