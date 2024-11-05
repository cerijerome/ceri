package ceri.common.math;

import static ceri.common.exception.ExceptionUtil.illegalArg;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.negateExact;

public record MixedFraction(long whole, Fraction fraction) {
	public static final MixedFraction ZERO = new MixedFraction(0, Fraction.ZERO);
	public static final MixedFraction ONE = new MixedFraction(1, Fraction.ZERO);

	public static MixedFraction of(long whole) {
		return of(whole, Fraction.ZERO);
	}

	public static MixedFraction of(long whole, long numerator, long denominator) {
		return of(whole, Fraction.of(numerator, denominator));
	}

	public static MixedFraction of(long whole, Fraction fraction) {
		return of(Fraction.of(whole, 1).add(fraction));
	}

	public static MixedFraction of(Fraction fraction) {
		long whole = fraction.whole();
		Fraction proper = fraction.proper();
		if (whole == 0 && proper.isZero()) return ZERO;
		if (whole == 1 && proper.isZero()) return ONE;
		return new MixedFraction(whole, proper);
	}

	public MixedFraction {
		int wholeSgn = Long.signum(whole);
		int fractionSgn = Long.signum(fraction.numerator());
		if (wholeSgn != 0 && fractionSgn != 0 && wholeSgn == -fractionSgn)
			throw illegalArg("Whole and fraction must be the same sign: %s, %s", whole, fraction);
	}

	public double value() {
		return whole() + fraction().value();
	}

	public Fraction asFraction() {
		if (whole() == 0) return fraction();
		long numerator =
			addExact(multiplyExact(whole(), fraction().denominator()), fraction().numerator());
		return Fraction.of(numerator, fraction().denominator());
	}

	public boolean isZero() {
		return whole() == 0 && fraction().isZero();
	}

	public boolean isNegative() {
		return whole < 0 || fraction.isNegative();
	}

	public boolean isWhole() {
		return fraction.isZero();
	}

	public boolean isProper() {
		return whole == 0;
	}

	public MixedFraction negate() {
		if (this == ZERO) return this;
		return of(negateExact(whole), fraction.negate());
	}

	public MixedFraction add(MixedFraction other) {
		if (other.isZero()) return this;
		if (isZero()) return other;
		return of(addExact(whole, other.whole), fraction.add(other.fraction));
	}

	public MixedFraction multiply(MixedFraction other) {
		if (this == ZERO || other == ZERO) return ZERO;
		if (this == ONE) return other;
		if (other == ONE) return this;
		return of(asFraction().multiply(other.asFraction()));
	}

	public MixedFraction divide(MixedFraction other) {
		if (other.isZero()) throw new ArithmeticException("Divide by 0");
		if (this == ZERO) return ZERO;
		if (other == ONE) return this;
		return of(asFraction().multiply(other.asFraction().invert()));
	}

	@Override
	public String toString() {
		if (fraction().isZero()) return String.valueOf(whole());
		if (isProper()) return fraction().toString();
		if (!isNegative()) return whole() + "_" + fraction();
		return whole() + "_" + fraction().negate();
	}
}
