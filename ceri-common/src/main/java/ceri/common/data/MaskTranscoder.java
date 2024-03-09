package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Objects;
import ceri.common.math.MathUtil;
import ceri.common.text.ToString;

/**
 * Extracts and calculates masked values within a long value.
 */
public class MaskTranscoder {
	public static final MaskTranscoder NULL = mask(-1L, 0);
	private final long mask; // value mask; before shift for decoding
	private final int shiftBits;

	/**
	 * Mask bits and shift. Masked value is right-shifted given number of bits.
	 */
	public static MaskTranscoder mask(int mask, int shiftBits) {
		return mask(MathUtil.uint(mask), shiftBits);
	}

	/**
	 * Mask bits and shift. Masked value is right-shifted given number of bits.
	 */
	public static MaskTranscoder mask(long mask, int shiftBits) {
		return new MaskTranscoder(mask, shiftBits);
	}

	/**
	 * Mask bits and shift. Masked value is right-shifted given number of bits. TODO: rename to
	 * 'bits'
	 */
	public static MaskTranscoder xbits(int bitCount, int shiftBits) {
		validateMin(bitCount, 1);
		return mask(ByteUtil.mask(shiftBits, bitCount), shiftBits);
	}

	private MaskTranscoder(long mask, int shiftBits) {
		this.mask = mask;
		this.shiftBits = shiftBits;
	}

	/**
	 * Return the standalone masked value.
	 */
	public long encode(long value) {
		return encode(value, 0);
	}

	/**
	 * Return the standalone masked value.
	 */
	public int encodeInt(long value) {
		return encodeInt(value, 0);
	}

	/**
	 * Return the masked value combined with current value.
	 */
	public long encode(long value, long current) {
		return setValue(current, mask, shiftBits, value);
	}

	/**
	 * Return the masked value combined with current value.
	 */
	public int encodeInt(long value, long current) {
		return (int) encode(value, current);
	}

	/**
	 * Extract the unmasked value from the current value.
	 */
	public long decode(long current) {
		return getValue(current, mask, shiftBits);
	}

	/**
	 * Extract the unmasked value from the current value.
	 */
	public int decodeInt(long current) {
		return (int) getValue(current, mask, shiftBits);
	}

	private static long setValue(long current, long mask, int shiftBits, long value) {
		return (current & ~mask) | (value << shiftBits & mask);
	}

	private static long getValue(long current, long mask, int shiftBits) {
		return (current & mask) >>> shiftBits;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mask, shiftBits);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MaskTranscoder other)) return false;
		if (mask != other.mask) return false;
		if (shiftBits != other.shiftBits) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, mask, shiftBits);
	}

}
