package ceri.common.math;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class FractionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var f = Fraction.of(13, 41);
		var eq0 = Fraction.of(13, 41);
		var eq1 = Fraction.of(13, 41).add(Fraction.ZERO);
		var eq2 = Fraction.of(13, 41).multiply(Fraction.ONE);
		var eq3 = Fraction.of(13, 41).divide(Fraction.ONE);
		var ne0 = Fraction.of(12, 41);
		var ne1 = Fraction.of(13, 42);
		Testing.exerciseEquals(f, eq0, eq1, eq2, eq3);
		Assert.yes(f.equals(13, 41));
		Assert.no(f.equals(12, 41));
		Assert.no(f.equals(13, 42));
		Assert.notEqualAll(f, ne0, ne1);
	}

	@Test
	public void shouldAddFractions() {
		MathAssert.fraction(Fraction.of(1, 22).add(Fraction.of(3, 77)), 13, 154);
		MathAssert.fraction(Fraction.of(-1, 22).add(Fraction.of(3, 77)), -1, 154);
		MathAssert.fraction(Fraction.of(1, 22).add(Fraction.of(-3, 77)), 1, 154);
		MathAssert.fraction(Fraction.of(-1, 22).add(Fraction.of(-3, 77)), -13, 154);
	}

	@Test
	public void shouldMultiplyFractions() {
		MathAssert.fraction(Fraction.of(0).multiply(Fraction.of(0, 1)), 0, 1);
		MathAssert.fraction(Fraction.of(0, 1).multiply(Fraction.of(1)), 0, 1);
		MathAssert.fraction(Fraction.of(1, 1).multiply(Fraction.of(0, 1)), 0, 1);
		MathAssert.fraction(Fraction.of(1, 1).multiply(Fraction.of(1, 1)), 1, 1);
		MathAssert.fraction(Fraction.of(7, 3).multiply(Fraction.of(2, 5)), 14, 15);
		MathAssert.fraction(Fraction.of(Integer.MIN_VALUE, Integer.MAX_VALUE)
			.multiply(Fraction.of(Integer.MAX_VALUE, Integer.MIN_VALUE)), 1, 1);
	}

	@Test
	public void shouldDivideFractions() {
		MathAssert.fraction(Fraction.of(0, 1).divide(Fraction.of(1, 1)), 0, 1);
		MathAssert.fraction(Fraction.of(1, 1).divide(Fraction.of(1, 1)), 1, 1);
		Assert.thrown(() -> Fraction.of(0, 1).divide(Fraction.of(0, 1)));
		MathAssert.fraction(Fraction.of(7, 3).divide(Fraction.of(2, 5)), 35, 6);
	}

	@Test
	public void shouldDetermineIfNegative() {
		Assert.yes(Fraction.of(1, -2).isNegative());
		Assert.yes(Fraction.of(-1, 2).isNegative());
		Assert.no(Fraction.of(0, -2).isNegative());
		Assert.no(Fraction.of(-1, -2).isNegative());
	}

	@Test
	public void shouldDetermineIfWhole() {
		Assert.yes(Fraction.of(63, 21).isWhole());
		Assert.no(Fraction.of(Long.MAX_VALUE - 1, Long.MAX_VALUE).isWhole());
	}

	@Test
	public void shouldDetermineIfProper() {
		Assert.yes(Fraction.of(0, 1).isProper());
		Assert.yes(Fraction.of(Long.MAX_VALUE - 1, Long.MAX_VALUE).isProper());
		Assert.no(Fraction.of(Long.MAX_VALUE, Long.MAX_VALUE).isProper());
		Assert.no(Fraction.of(Long.MIN_VALUE, Long.MAX_VALUE).isProper());
	}

	@Test
	public void shouldNegate() {
		MathAssert.fraction(Fraction.of(0, 1).negate(), 0, 1);
		MathAssert.fraction(Fraction.of(10, 9).negate(), -10, 9);
		MathAssert.fraction(Fraction.of(-9, 10).negate(), 9, 10);
		Assert.thrown(() -> Fraction.of(Long.MIN_VALUE, 101).negate());
	}

	@Test
	public void shouldInvert() {
		MathAssert.fraction(Fraction.of(-9, 7).invert(), -7, 9);
		Assert.thrown(() -> Fraction.of(0, Long.MAX_VALUE).invert());
		Assert.thrown(() -> Fraction.of(Long.MIN_VALUE, 1).invert());
	}

	@Test
	public void shouldExtractProperFraction() {
		MathAssert.fraction(Fraction.of(-9, 7).proper(), -2, 7);
		MathAssert.fraction(Fraction.of(-2, 7).proper(), -2, 7);
	}

	@Test
	public void shouldFailToCreateInvalidFraction() {
		Assert.thrown(() -> Fraction.of(0, 0));
		Assert.thrown(() -> Fraction.of(1, 0));
		Assert.thrown(() -> Fraction.of(1, Long.MIN_VALUE));
		Assert.thrown(() -> Fraction.of(Long.MIN_VALUE, -1));
	}

	@Test
	public void shouldReduceDuringConstruction() {
		MathAssert.fraction(Fraction.of(99, 33), 3, 1);
		MathAssert.fraction(Fraction.of(33, 99), 1, 3);
		MathAssert.fraction(Fraction.of(-99, 33), -3, 1);
		MathAssert.fraction(Fraction.of(99, -33), -3, 1);
		MathAssert.fraction(Fraction.of(-99, -33), 3, 1);
		MathAssert.fraction(Fraction.of(33, 13), 33, 13);
		MathAssert.fraction(Fraction.of(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE, Long.MAX_VALUE);
	}
}
