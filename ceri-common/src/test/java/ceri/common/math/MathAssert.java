package ceri.common.math;

import ceri.common.test.Assert;

/**
 * Math package assertions.
 */
public class MathAssert {
	private MathAssert() {}

	/**
	 * Fails if the fraction does not equal numerator and denominator.
	 */
	public static void fraction(Fraction fraction, long numerator, long denominator) {
		Assert.equal(fraction.numerator(), numerator);
		Assert.equal(fraction.denominator(), denominator);
	}

	/**
	 * Fails if the mixed fraction does not equal whole number, numerator and denominator.
	 */
	public static void fraction(MixedFraction fraction, long whole, long numerator,
		long denominator) {
		Assert.equal(fraction.whole(), whole);
		Assert.equal(fraction.fraction().equals(numerator, denominator), true);
	}
}
