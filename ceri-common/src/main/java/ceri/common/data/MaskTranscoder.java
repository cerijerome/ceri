package ceri.common.data;

import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Extracts and calculates masked values within a long value.
 */
public class MaskTranscoder {
	public static final MaskTranscoder NULL = mask(-1);
	private final long mask; // value mask; before shift for decoding
	private final int shiftBits;

	public static MaskTranscoder mask(long mask) {
		return mask(mask, 0);
	}

	public static MaskTranscoder mask(long mask, int shiftBits) {
		return new MaskTranscoder(mask, shiftBits);
	}

	public static MaskTranscoder bits(int startBit, int bitCount) {
		return mask(ByteUtil.mask(startBit, bitCount), startBit);
	}

	public static MaskTranscoder bits(int bitCount) {
		return bits(0, bitCount);
	}

	public static MaskTranscoder shiftBits(int startBit, int bitCount) {
		return mask(ByteUtil.mask(startBit, bitCount), startBit);
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
		return (int) setValue(current, mask, shiftBits, value);
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
		return (current & mask) >> shiftBits;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(mask, shiftBits);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MaskTranscoder)) return false;
		MaskTranscoder other = (MaskTranscoder) obj;
		if (mask != other.mask) return false;
		if (shiftBits != other.shiftBits) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, mask, shiftBits).toString();
	}

}
