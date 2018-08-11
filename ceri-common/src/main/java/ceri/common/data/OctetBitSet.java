package ceri.common.data;

import java.util.BitSet;

public class OctetBitSet extends BitSet {
	private static final long serialVersionUID = -4005476760615651068L;

	public static OctetBitSet create() {
		return of(0);
	}

	public static OctetBitSet of(int value) {
		return of((byte) value);
	}
	
	public static OctetBitSet of(byte value) {
		OctetBitSet bitSet = new OctetBitSet();
		for (int i = 0; i < Byte.SIZE; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	private OctetBitSet() {
		super(Byte.SIZE);
	}

	public int setBit(int bitIndex, boolean value) {
		set(bitIndex, value);
		return bitIndex + 1;
	}
	
	public int setBits(int bitIndex, int value, int bits) {
		bits = Math.min(bits, size() - bitIndex);
		int mask = 1;
		for (int i = 0; i < bits; i++) {
			set(bitIndex + i, (value & mask) != 0);
			mask <<= 1;
		}
		return bitIndex + bits;
	}
	
	public int getBits(int bitIndex, int bits) {
		bits = Math.min(bits, size() - bitIndex);
		int mask = 1;
		int value = 0;
		for (int i = 0; i < bits; i++) {
			if (get(bitIndex + i)) value |= mask;
			mask <<= 1;
		}
		return value;
	}
	
	public int intValue() {
		return value() & 0xff;
	}
	
	public byte value() {
		return toByteArray()[0];
	}

}
