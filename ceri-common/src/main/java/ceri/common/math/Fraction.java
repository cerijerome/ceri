package ceri.common.math;

import static ceri.common.math.MathUtil.gcd;
import static ceri.common.math.MathUtil.lcm;
import static ceri.common.validation.ValidationUtil.validateMin;
import static java.lang.Math.abs;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.negateExact;
import static java.lang.Math.subtractExact;

/**
 * Holds a numerator and denominator. This is a visual representation, so reduction is not
 * automatic.
 */
public record Fraction(long numerator, long denominator) {
	public static final Fraction ZERO = new Fraction(0, 1);
	public static final Fraction ONE = new Fraction(1, 1);

	public Fraction {
		validateMin(denominator, 1);
	}

	public static Fraction of(long numerator) {
		return of(numerator, 1);
	}

	public static Fraction of(long numerator, long denominator) {
		if (denominator == 0) throw new IllegalArgumentException("Zero denominator");
		if (numerator == 0) return ZERO;
		if (numerator == denominator) return ONE;
		if (denominator < 0) {
			numerator = Math.negateExact(numerator);
			denominator = Math.negateExact(denominator);
		}
		long gcd = gcd(numerator, denominator);
		return new Fraction(numerator / gcd, denominator / gcd);
	}

	public double value() {
		return (double) numerator() / denominator();
	}

	public boolean isZero() {
		return numerator() == 0;
	}

	public boolean isNegative() {
		return numerator() < 0;
	}

	public boolean isWhole() {
		return denominator() == 1;
	}

	public boolean isProper() {
		return numerator() != Long.MIN_VALUE && abs(numerator()) < denominator();
	}

	public Fraction negate() {
		if (numerator() == 0) return this;
		return of(negateExact(numerator()), denominator());
	}

	public Fraction invert() {
		return of(denominator(), numerator());
	}

	public long whole() {
		return numerator() / denominator();
	}

	public Fraction proper() {
		if (isProper()) return this;
		return of(subtractExact(numerator(), multiplyExact(whole(), denominator())), denominator());
	}

	public Fraction add(Fraction fraction) {
		if (fraction.isZero()) return this;
		if (isZero()) return fraction;
		if (denominator() == fraction.denominator())
			return of(addExact(numerator(), fraction.numerator()), denominator());
		long lcm = lcm(denominator(), fraction.denominator());
		long lhs = multiplyExact(numerator(), lcm / denominator());
		long rhs = multiplyExact(fraction.numerator(), lcm / fraction.denominator());
		return of(addExact(lhs, rhs), lcm);
	}

	public Fraction multiply(Fraction fraction) {
		if (this == ZERO || fraction == ZERO) return ZERO;
		if (this == ONE) return fraction;
		if (fraction == ONE) return this;
		long numerator = multiplyExact(this.numerator(), fraction.numerator());
		long denominator = multiplyExact(this.denominator(), fraction.denominator());
		return of(numerator, denominator);
	}

	public Fraction divide(Fraction fraction) {
		if (fraction.isZero()) throw new ArithmeticException("Divide by 0");
		if (this == ZERO) return ZERO;
		if (fraction == ONE) return this;
		return multiply(fraction.invert());
	}

	public boolean equals(long numerator, long denominator) {
		return numerator() == numerator && denominator() == denominator;
	}

	@Override
	public String toString() {
		return numerator() + "/" + denominator();
	}
}
