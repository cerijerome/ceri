package ceri.common.math;

import java.math.BigInteger;
import ceri.common.util.HashCoder;

public class Fraction {
	public final long numerator;
	public final long denominator;
	public final double value;

	public static Fraction of(long numerator, long denominator) {
		return new Fraction(numerator, denominator);
	}

	private static Fraction of(BigInteger numerator, BigInteger denominator) {
		return of(numerator.longValueExact(), denominator.longValueExact());
	}
	
	private Fraction(long numerator, long denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
		value = (double) numerator / denominator;
	}

	public boolean isNegative() {
		return value < 0.0; 
	}

	public boolean isWhole() {
		return numerator % denominator == 0;
	}

	public boolean isProper() {
		return Math.abs(numerator) < Math.abs(denominator);
	}

	public Fraction add(Fraction fraction) {
		if (numerator == 0) return fraction;
		if (fraction.numerator == 0) return this;
		BigInteger numerator = multiply(this.numerator, fraction.denominator)
			.add(multiply(fraction.numerator, this.denominator));
		BigInteger denominator = multiply(this.denominator, fraction.denominator);
		Fraction reduced = reduced(numerator, denominator);
		return reduced == null ? of(numerator, denominator) : reduced;
	}

	public Fraction multiply(Fraction fraction) {
		if (numerator == 1 && denominator == 1) return fraction;
		if (fraction.numerator == 1 && fraction.denominator == 1) return this;
		BigInteger numerator = multiply(this.numerator, fraction.numerator);
		BigInteger denominator = multiply(this.denominator, fraction.denominator);
		Fraction reduced = reduced(numerator, denominator);
		return reduced == null ? of(numerator, denominator) : reduced;
	}

	public Fraction reduce() {
		if (isWhole()) return of(numerator / denominator, 1);
		BigInteger numerator = BigInteger.valueOf(this.numerator);
		BigInteger denominator = BigInteger.valueOf(this.denominator);
		Fraction reduced = reduced(numerator, denominator);
		return reduced == null ? this : reduced;
	}

	private Fraction reduced(BigInteger numerator, BigInteger denominator) {
		BigInteger gcd = numerator.gcd(denominator);
		int signum = denominator.signum();
		if (gcd.longValueExact() == 1 && signum != -1) return null;
		if (signum == -1) {
			numerator = numerator.negate();
			denominator = denominator.negate();
		}
		return of(numerator.divide(gcd), denominator.divide(gcd));
	}

	public Fraction multiply(long value) {
		if (value == 1) return this;
		return multiply(of(value, 1));
	}

	public Fraction negate() {
		if (numerator == 0) return this;
		return of(Math.negateExact(numerator), denominator);
	}

	public Fraction divide(Fraction fraction) {
		return multiply(fraction.invert());
	}

	public Fraction divide(long value) {
		return multiply(of(1, value));
	}

	public Fraction invert() {
		if (numerator == denominator) return this;
		return of(denominator, numerator);
	}

	public long whole() {
		return (long) value;
	}

	public Fraction proper() {
		if (numerator < denominator) return this;
		return of(numerator - (whole() * denominator), denominator);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(numerator, denominator);
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

	private static BigInteger multiply(long lhs, long rhs) {
		return BigInteger.valueOf(lhs).multiply(BigInteger.valueOf(rhs));
	}

}
