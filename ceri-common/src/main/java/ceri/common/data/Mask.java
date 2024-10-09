package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.MathUtil;

/**
 * Extracts and calculates masked values within a long value. The specified mask is absolute.
 */
public record Mask(int shift, long mask) {
	public static final Mask NULL = new Mask(0, -1L);

	/**
	 * Absolute mask bits and bit shift. Masked value is right-shifted the given number of bits.
	 */
	public static Mask ofInt(int shiftBits, int mask) {
		return of(shiftBits, MathUtil.uint(mask));
	}

	/**
	 * Absolute mask bits and bit shift. Masked value is right-shifted the given number of bits.
	 */
	public static Mask of(int shiftBits, long mask) {
		validateMin(shiftBits, 0);
		return new Mask(shiftBits, mask);
	}

	/**
	 * Mask bits and shift. Masked value is right-shifted given number of bits.
	 */
	public static Mask ofBits(int shift, int count) {
		validateMin(count, 1);
		return of(shift, ByteUtil.mask(shift, count));
	}

	/**
	 * Return the standalone masked value.
	 */
	public long encode(long value) {
		return encode(0, value);
	}

	/**
	 * Return the standalone masked value.
	 */
	public int encodeInt(long value) {
		return encodeInt(0, value);
	}

	/**
	 * Return the masked value combined with current value.
	 */
	public long encode(long current, long value) {
		return setValue(current, mask, shift, value);
	}

	/**
	 * Return the masked value combined with current value.
	 */
	public int encodeInt(long current, long value) {
		return (int) encode(current, value);
	}

	/**
	 * Extract the unmasked value from the current value.
	 */
	public long decode(long current) {
		return getValue(current, mask, shift);
	}

	/**
	 * Extract the unmasked value from the current value.
	 */
	public int decodeInt(long current) {
		return (int) getValue(current, mask, shift);
	}

	private static long setValue(long current, long mask, int shiftBits, long value) {
		return (current & ~mask) | ((value << shiftBits) & mask);
	}

	private static long getValue(long current, long mask, int shiftBits) {
		return (current & mask) >>> shiftBits;
	}

	@Override
	public String toString() {
		if (shift == 0) return "0x" + Long.toHexString(mask);
		return "0x" + Long.toHexString(mask) + ">>" + shift;
	}
}
