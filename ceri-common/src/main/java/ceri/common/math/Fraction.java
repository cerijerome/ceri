package ceri.common.math;

import static ceri.common.math.MathUtil.gcd;
import static ceri.common.math.MathUtil.lcm;
import static java.lang.Math.abs;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.negateExact;
import static java.lang.Math.subtractExact;
import java.util.Objects;

/**
 * Holds a numerator and denominator. This is a visual representation, so reduction is not
 * automatic. A zero denominator is allowed to exist, but may fail arithmetic operations. May need
 * some optimization.
 */
public class Fraction {
	public static final Fraction ZERO = new Fraction(0, 1);
	public static final Fraction ONE = new Fraction(1, 1);
	public final long numerator;
	public final long denominator; // always > 0
	public final double value;

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

	private Fraction(long numerator, long denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
		value = (double) numerator / denominator;
	}

	public boolean isZero() {
		return numerator == 0;
	}

	public boolean isNegative() {
		return numerator < 0;
	}

	public boolean isWhole() {
		return denominator == 1;
	}

	public boolean isProper() {
		return numerator != Long.MIN_VALUE && abs(numerator) < denominator;
	}

	public Fraction negate() {
		if (numerator == 0) return this;
		return of(negateExact(numerator), denominator);
	}

	public Fraction invert() {
		return of(denominator, numerator);
	}

	public long whole() {
		return numerator / denominator;
	}

	public Fraction proper() {
		if (isProper()) return this;
		return of(subtractExact(numerator, multiplyExact(whole(), denominator)), denominator);
	}

	public Fraction add(Fraction fraction) {
		if (fraction.isZero()) return this;
		if (isZero()) return fraction;
		if (denominator == fraction.denominator)
			return of(addExact(numerator, fraction.numerator), denominator);
		long lcm = lcm(denominator, fraction.denominator);
		long lhs = multiplyExact(numerator, lcm / denominator);
		long rhs = multiplyExact(fraction.numerator, lcm / fraction.denominator);
		return of(addExact(lhs, rhs), lcm);
	}

	public Fraction multiply(Fraction fraction) {
		if (this == ZERO || fraction == ZERO) return ZERO;
		if (this == ONE) return fraction;
		if (fraction == ONE) return this;
		long numerator = multiplyExact(this.numerator, fraction.numerator);
		long denominator = multiplyExact(this.denominator, fraction.denominator);
		return of(numerator, denominator);
	}

	public Fraction divide(Fraction fraction) {
		if (fraction.isZero()) throw new ArithmeticException("Divide by 0");
		if (this == ZERO) return ZERO;
		if (fraction == ONE) return this;
		return multiply(fraction.invert());
	}

	@Override
	public int hashCode() {
		return Objects.hash(numerator, denominator);
	}

	public boolean equals(long numerator, long denominator) {
		return this.numerator == numerator && this.denominator == denominator;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Fraction)) return false;
		Fraction other = (Fraction) obj;
		if (numerator != other.numerator) return false;
		if (denominator != other.denominator) return false;
		return true;
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}

}
