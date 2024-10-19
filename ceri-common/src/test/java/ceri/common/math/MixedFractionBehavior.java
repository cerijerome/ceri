package ceri.common.math;

import static ceri.common.math.FractionBehavior.assertFraction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class MixedFractionBehavior {

	@Test
	public void shouldFailToCreateWithMixedSigns() {
		assertIllegalArg(() -> new MixedFraction(-1, Fraction.of(1, 3)));
		assertIllegalArg(() -> new MixedFraction(1, Fraction.of(-1, 3)));
		assertIllegalArg(() -> new MixedFraction(1, Fraction.of(1, -3)));
		assertIllegalArg(() -> new MixedFraction(-1, Fraction.of(-1, -3)));
	}

	@Test
	public void shouldCalculateValue() {
		assertEquals(MixedFraction.of(3, 4, 5).value(), 3.8);
		assertEquals(MixedFraction.of(-3, 4, 5).value(), -2.2);
		assertEquals(MixedFraction.of(3, 4, -5).value(), 2.2);
	}

	@Test
	public void shouldHaveStringRepresentation() {
		assertEquals(MixedFraction.of(0, 0, 1).toString(), "0");
		assertEquals(MixedFraction.of(1).toString(), "1");
		assertEquals(MixedFraction.of(0, 1, 2).toString(), "1/2");
		assertEquals(MixedFraction.of(1, 1, 2).toString(), "1_1/2");
		assertEquals(MixedFraction.of(-1, -1, 2).toString(), "-1_1/2");
	}

	@Test
	public void shouldConvertToPureFraction() {
		assertFraction(MixedFraction.of(10).asFraction(), 10, 1);
		assertFraction(MixedFraction.of(10, 1, 3).asFraction(), 31, 3);
		assertFraction(MixedFraction.of(0, 1, 3).asFraction(), 1, 3);
	}

	@Test
	public void shouldNegate() {
		assertMixedFraction(MixedFraction.of(0).negate(), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(2, 1, 2).negate(), -2, -1, 2);
		assertMixedFraction(MixedFraction.of(-2, -1, 2).negate(), 2, 1, 2);
		assertMixedFraction(MixedFraction.of(0, -1, 2).negate(), 0, 1, 2);
	}

	@Test
	public void shouldAddMixedFractions() {
		assertMixedFraction(MixedFraction.of(0).add(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(1).add(MixedFraction.of(0, 0, 1)), 1, 0, 1);
		assertMixedFraction(MixedFraction.of(0).add(MixedFraction.of(0, 1, 2)), 0, 1, 2);
		assertMixedFraction(MixedFraction.of(3, 2, 3).add(MixedFraction.of(-1, -1, 4)), 2, 5, 12);
	}

	@Test
	public void shouldMultiplyMixedFractions() {
		assertMixedFraction(MixedFraction.of(0).multiply(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(1).multiply(MixedFraction.of(0, 0, 1)), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(0).multiply(MixedFraction.of(1, 1, 2)), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(1).multiply(MixedFraction.of(1, 1, 2)), 1, 1, 2);
		assertMixedFraction(MixedFraction.of(3, 2, 3).multiply(MixedFraction.of(-1, -1, 4)), -4, -7,
			12);
		assertMixedFraction(MixedFraction.of(3, 2, 3).multiply(MixedFraction.ONE), 3, 2, 3);
	}

	@Test
	public void shouldDivideMixedFractions() {
		assertMixedFraction(MixedFraction.of(0).divide(MixedFraction.of(1, 0, 1)), 0, 0, 1);
		assertMixedFraction(MixedFraction.of(1).divide(MixedFraction.of(1, 1, 2)), 0, 2, 3);
		assertMixedFraction(MixedFraction.of(3, 2, 3).divide(MixedFraction.of(1, 0, 1)), 3, 2, 3);
		assertThrown(() -> MixedFraction.of(0).divide(MixedFraction.of(0, 0, 1)));
		assertMixedFraction(MixedFraction.of(3, 2, 3).divide(MixedFraction.of(-1, -1, 4)), -2, -14,
			15);
	}

	@Test
	public void shouldDetermineIfZero() {
		assertTrue(MixedFraction.of(0).isZero());
		assertTrue(MixedFraction.of(0, 0, 1).isZero());
		assertFalse(MixedFraction.of(0, 1, 100).isZero());
		assertFalse(MixedFraction.of(1, 0, 1).isZero());
	}

	@Test
	public void shouldDetermineIfNegative() {
		assertFalse(MixedFraction.of(0).isNegative());
		assertTrue(MixedFraction.of(0, -1, 2).isNegative());
		assertTrue(MixedFraction.of(-1, 1, 2).isNegative());
		assertTrue(MixedFraction.of(-1, -1, 2).isNegative());
	}

	@Test
	public void shouldDetermineIfWholeNumber() {
		assertTrue(MixedFraction.of(0).isWhole());
		assertTrue(MixedFraction.of(-1, 0, 1).isWhole());
		assertFalse(MixedFraction.of(1, -1, 2).isNegative());
	}

	@Test
	public void shouldDetermineIfProperFraction() {
		assertTrue(MixedFraction.of(0).isProper());
		assertTrue(MixedFraction.of(0, 1, 2).isProper());
		assertFalse(MixedFraction.of(0, 3, 2).isProper());
		assertFalse(MixedFraction.of(1, 0, 1).isProper());
	}

	@Test
	public void shouldFailToCreateInvalidFraction() {
		assertThrown(() -> MixedFraction.of(0, 0, 0));
		assertThrown(() -> MixedFraction.of(1, 1, 0));
		assertThrown(() -> MixedFraction.of(0, -1, Long.MIN_VALUE));
		assertThrown(() -> MixedFraction.of(0, 1, Long.MIN_VALUE));
		assertThrown(() -> MixedFraction.of(Long.MIN_VALUE, -1, 1));
	}

	@Test
	public void shouldReduceDuringConstruction() {
		assertMixedFraction(MixedFraction.of(7, 41, 13), 10, 2, 13);
		assertMixedFraction(MixedFraction.of(10, 2, 13), 10, 2, 13);
		assertMixedFraction(MixedFraction.of(0, 132, 13), 10, 2, 13);
		assertMixedFraction(MixedFraction.of(Long.MIN_VALUE, 1, 1), Long.MIN_VALUE + 1, 0, 1);
		assertMixedFraction(MixedFraction.of(0, Long.MIN_VALUE, 1), Long.MIN_VALUE, 0, 1);
	}

	public static void assertMixedFraction(MixedFraction fraction, long whole, long numerator,
		long denominator) {
		assertEquals(fraction.whole(), whole);
		assertEquals(fraction.fraction().equals(numerator, denominator), true);
	}

}
