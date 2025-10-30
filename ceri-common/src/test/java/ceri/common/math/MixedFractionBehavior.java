package ceri.common.math;

import org.junit.Test;
import ceri.common.test.Assert;

public class MixedFractionBehavior {

	@Test
	public void shouldFailToCreateWithMixedSigns() {
		Assert.illegalArg(() -> new MixedFraction(-1, Fraction.of(1, 3)));
		Assert.illegalArg(() -> new MixedFraction(1, Fraction.of(-1, 3)));
		Assert.illegalArg(() -> new MixedFraction(1, Fraction.of(1, -3)));
		Assert.illegalArg(() -> new MixedFraction(-1, Fraction.of(-1, -3)));
	}

	@Test
	public void shouldCalculateValue() {
		Assert.equal(MixedFraction.of(3, 4, 5).value(), 3.8);
		Assert.equal(MixedFraction.of(-3, 4, 5).value(), -2.2);
		Assert.equal(MixedFraction.of(3, 4, -5).value(), 2.2);
	}

	@Test
	public void shouldHaveStringRepresentation() {
		Assert.equal(MixedFraction.of(0, 0, 1).toString(), "0");
		Assert.equal(MixedFraction.of(1).toString(), "1");
		Assert.equal(MixedFraction.of(0, 1, 2).toString(), "1/2");
		Assert.equal(MixedFraction.of(1, 1, 2).toString(), "1_1/2");
		Assert.equal(MixedFraction.of(-1, -1, 2).toString(), "-1_1/2");
	}

	@Test
	public void shouldConvertToPureFraction() {
		MathAssert.fraction(MixedFraction.of(10).asFraction(), 10, 1);
		MathAssert.fraction(MixedFraction.of(10, 1, 3).asFraction(), 31, 3);
		MathAssert.fraction(MixedFraction.of(0, 1, 3).asFraction(), 1, 3);
	}

	@Test
	public void shouldNegate() {
		MathAssert.fraction(MixedFraction.of(0).negate(), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(2, 1, 2).negate(), -2, -1, 2);
		MathAssert.fraction(MixedFraction.of(-2, -1, 2).negate(), 2, 1, 2);
		MathAssert.fraction(MixedFraction.of(0, -1, 2).negate(), 0, 1, 2);
	}

	@Test
	public void shouldAddMixedFractions() {
		MathAssert.fraction(MixedFraction.of(0).add(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(1).add(MixedFraction.of(0, 0, 1)), 1, 0, 1);
		MathAssert.fraction(MixedFraction.of(0).add(MixedFraction.of(0, 1, 2)), 0, 1, 2);
		MathAssert.fraction(MixedFraction.of(3, 2, 3).add(MixedFraction.of(-1, -1, 4)), 2, 5, 12);
	}

	@Test
	public void shouldMultiplyMixedFractions() {
		MathAssert.fraction(MixedFraction.of(0).multiply(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(1).multiply(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(0).multiply(MixedFraction.of(1, 1, 2)), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(1).multiply(MixedFraction.of(1, 1, 2)), 1, 1, 2);
		MathAssert.fraction(MixedFraction.of(3, 2, 3).multiply(MixedFraction.of(-1, -1, 4)), -4, -7,
			12);
		MathAssert.fraction(MixedFraction.of(3, 2, 3).multiply(MixedFraction.ONE), 3, 2, 3);
	}

	@Test
	public void shouldDivideMixedFractions() {
		MathAssert.fraction(MixedFraction.of(0).divide(MixedFraction.of(1, 0, 1)), 0, 0, 1);
		MathAssert.fraction(MixedFraction.of(1).divide(MixedFraction.of(1, 1, 2)), 0, 2, 3);
		MathAssert.fraction(MixedFraction.of(3, 2, 3).divide(MixedFraction.of(1, 0, 1)), 3, 2, 3);
		Assert.thrown(() -> MixedFraction.of(0).divide(MixedFraction.of(0, 0, 1)));
		MathAssert.fraction(MixedFraction.of(3, 2, 3).divide(MixedFraction.of(-1, -1, 4)), -2, -14,
			15);
	}

	@Test
	public void shouldDetermineIfZero() {
		Assert.yes(MixedFraction.of(0).isZero());
		Assert.yes(MixedFraction.of(0, 0, 1).isZero());
		Assert.no(MixedFraction.of(0, 1, 100).isZero());
		Assert.no(MixedFraction.of(1, 0, 1).isZero());
	}

	@Test
	public void shouldDetermineIfNegative() {
		Assert.no(MixedFraction.of(0).isNegative());
		Assert.yes(MixedFraction.of(0, -1, 2).isNegative());
		Assert.yes(MixedFraction.of(-1, 1, 2).isNegative());
		Assert.yes(MixedFraction.of(-1, -1, 2).isNegative());
	}

	@Test
	public void shouldDetermineIfWholeNumber() {
		Assert.yes(MixedFraction.of(0).isWhole());
		Assert.yes(MixedFraction.of(-1, 0, 1).isWhole());
		Assert.no(MixedFraction.of(1, -1, 2).isNegative());
	}

	@Test
	public void shouldDetermineIfProperFraction() {
		Assert.yes(MixedFraction.of(0).isProper());
		Assert.yes(MixedFraction.of(0, 1, 2).isProper());
		Assert.no(MixedFraction.of(0, 3, 2).isProper());
		Assert.no(MixedFraction.of(1, 0, 1).isProper());
	}

	@Test
	public void shouldFailToCreateInvalidFraction() {
		Assert.thrown(() -> MixedFraction.of(0, 0, 0));
		Assert.thrown(() -> MixedFraction.of(1, 1, 0));
		Assert.thrown(() -> MixedFraction.of(0, -1, Long.MIN_VALUE));
		Assert.thrown(() -> MixedFraction.of(0, 1, Long.MIN_VALUE));
		Assert.thrown(() -> MixedFraction.of(Long.MIN_VALUE, -1, 1));
	}

	@Test
	public void shouldReduceDuringConstruction() {
		MathAssert.fraction(MixedFraction.of(7, 41, 13), 10, 2, 13);
		MathAssert.fraction(MixedFraction.of(10, 2, 13), 10, 2, 13);
		MathAssert.fraction(MixedFraction.of(0, 132, 13), 10, 2, 13);
		MathAssert.fraction(MixedFraction.of(Long.MIN_VALUE, 1, 1), Long.MIN_VALUE + 1, 0, 1);
		MathAssert.fraction(MixedFraction.of(0, Long.MIN_VALUE, 1), Long.MIN_VALUE, 0, 1);
	}
}
