package ceri.common.math;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class FractionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Fraction f = Fraction.of(13, 41);
		Fraction eq0 = Fraction.of(13, 41);
		Fraction eq1 = Fraction.of(13, 41).add(Fraction.ZERO);
		Fraction eq2 = Fraction.of(13, 41).multiply(Fraction.ONE);
		Fraction eq3 = Fraction.of(13, 41).divide(Fraction.ONE);
		Fraction ne0 = Fraction.of(12, 41);
		Fraction ne1 = Fraction.of(13, 42);
		exerciseEquals(f, eq0, eq1, eq2, eq3);
		assertTrue(f.equals(13, 41));
		assertFalse(f.equals(12, 41));
		assertFalse(f.equals(13, 42));
		assertAllNotEqual(f, ne0, ne1);
	}

	@Test
	public void shouldAddFractions() {
		assertFraction(Fraction.of(1, 22).add(Fraction.of(3, 77)), 13, 154);
		assertFraction(Fraction.of(-1, 22).add(Fraction.of(3, 77)), -1, 154);
		assertFraction(Fraction.of(1, 22).add(Fraction.of(-3, 77)), 1, 154);
		assertFraction(Fraction.of(-1, 22).add(Fraction.of(-3, 77)), -13, 154);
	}

	@Test
	public void shouldMultiplyFractions() {
		assertFraction(Fraction.of(0).multiply(Fraction.of(0, 1)), 0, 1);
		assertFraction(Fraction.of(0, 1).multiply(Fraction.of(1)), 0, 1);
		assertFraction(Fraction.of(1, 1).multiply(Fraction.of(0, 1)), 0, 1);
		assertFraction(Fraction.of(1, 1).multiply(Fraction.of(1, 1)), 1, 1);
		assertFraction(Fraction.of(7, 3).multiply(Fraction.of(2, 5)), 14, 15);
		assertFraction(Fraction.of(Integer.MIN_VALUE, Integer.MAX_VALUE)
			.multiply(Fraction.of(Integer.MAX_VALUE, Integer.MIN_VALUE)), 1, 1);
	}

	@Test
	public void shouldDivideFractions() {
		assertFraction(Fraction.of(0, 1).divide(Fraction.of(1, 1)), 0, 1);
		assertFraction(Fraction.of(1, 1).divide(Fraction.of(1, 1)), 1, 1);
		assertThrown(() -> Fraction.of(0, 1).divide(Fraction.of(0, 1)));
		assertFraction(Fraction.of(7, 3).divide(Fraction.of(2, 5)), 35, 6);
	}

	@Test
	public void shouldDetermineIfNegative() {
		assertTrue(Fraction.of(1, -2).isNegative());
		assertTrue(Fraction.of(-1, 2).isNegative());
		assertFalse(Fraction.of(0, -2).isNegative());
		assertFalse(Fraction.of(-1, -2).isNegative());
	}

	@Test
	public void shouldDetermineIfWhole() {
		assertTrue(Fraction.of(63, 21).isWhole());
		assertFalse(Fraction.of(Long.MAX_VALUE - 1, Long.MAX_VALUE).isWhole());
	}

	@Test
	public void shouldDetermineIfProper() {
		assertTrue(Fraction.of(0, 1).isProper());
		assertTrue(Fraction.of(Long.MAX_VALUE - 1, Long.MAX_VALUE).isProper());
		assertFalse(Fraction.of(Long.MAX_VALUE, Long.MAX_VALUE).isProper());
		assertFalse(Fraction.of(Long.MIN_VALUE, Long.MAX_VALUE).isProper());
	}

	@Test
	public void shouldNegate() {
		assertFraction(Fraction.of(0, 1).negate(), 0, 1);
		assertFraction(Fraction.of(10, 9).negate(), -10, 9);
		assertFraction(Fraction.of(-9, 10).negate(), 9, 10);
		assertThrown(() -> Fraction.of(Long.MIN_VALUE, 101).negate());
	}

	@Test
	public void shouldInvert() {
		assertFraction(Fraction.of(-9, 7).invert(), -7, 9);
		assertThrown(() -> Fraction.of(0, Long.MAX_VALUE).invert());
		assertThrown(() -> Fraction.of(Long.MIN_VALUE, 1).invert());
	}

	@Test
	public void shouldExtractProperFraction() {
		assertFraction(Fraction.of(-9, 7).proper(), -2, 7);
		assertFraction(Fraction.of(-2, 7).proper(), -2, 7);
	}

	@Test
	public void shouldFailToCreateInvalidFraction() {
		assertThrown(() -> Fraction.of(0, 0));
		assertThrown(() -> Fraction.of(1, 0));
		assertThrown(() -> Fraction.of(1, Long.MIN_VALUE));
		assertThrown(() -> Fraction.of(Long.MIN_VALUE, -1));
	}

	@Test
	public void shouldReduceDuringConstruction() {
		assertFraction(Fraction.of(99, 33), 3, 1);
		assertFraction(Fraction.of(33, 99), 1, 3);
		assertFraction(Fraction.of(-99, 33), -3, 1);
		assertFraction(Fraction.of(99, -33), -3, 1);
		assertFraction(Fraction.of(-99, -33), 3, 1);
		assertFraction(Fraction.of(33, 13), 33, 13);
		assertFraction(Fraction.of(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public static void assertFraction(Fraction fraction, long numerator, long denominator) {
		assertEquals(fraction.numerator, numerator);
		assertEquals(fraction.denominator, denominator);
	}

}
