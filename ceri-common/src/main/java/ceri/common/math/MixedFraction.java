package ceri.common.math;

import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.negateExact;
import java.util.Objects;

public class MixedFraction {
	public static final MixedFraction ZERO = new MixedFraction(0, Fraction.ZERO);
	public static final MixedFraction ONE = new MixedFraction(1, Fraction.ZERO);
	public final long whole;
	public final Fraction fraction;
	public final double value;

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

	private MixedFraction(long whole, Fraction fraction) {
		this.whole = whole;
		this.fraction = fraction;
		value = whole + fraction.value;
	}

	public Fraction asFraction() {
		if (whole == 0) return fraction;
		long numerator = addExact(multiplyExact(whole, fraction.denominator), fraction.numerator);
		return Fraction.of(numerator, fraction.denominator);
	}

	public boolean isZero() {
		return whole == 0 && fraction.isZero();
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
	public int hashCode() {
		return Objects.hash(whole, fraction);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MixedFraction other)) return false;
		if (whole != other.whole) return false;
		if (!Objects.equals(fraction, other.fraction)) return false;
		return true;
	}

	@Override
	public String toString() {
		if (fraction.isZero()) return String.valueOf(whole);
		if (isProper()) return fraction.toString();
		if (!isNegative()) return whole + "_" + fraction;
		return whole + "_" + fraction.negate();
	}

}
