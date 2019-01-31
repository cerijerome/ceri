package ceri.common.data;

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import ceri.common.collection.StreamUtil;
import ceri.common.util.PrimitiveUtil;

public class IntBitSet extends BitSet {
	private static final long serialVersionUID = -4005476760615651068L;

	public static IntBitSet from(int... bits) {
		return from(PrimitiveUtil.asList(bits));
	}

	public static IntBitSet from(Collection<Integer> bits) {
		IntBitSet bitSet = of(0);
		bits.forEach(bitSet::set);
		return bitSet;
	}

	public static IntBitSet of() {
		return new IntBitSet();
	}

	public static IntBitSet ofByte(int value) {
		return of(value & 0xff, Byte.SIZE);
	}

	public static IntBitSet ofShort(int value) {
		return of(value & 0xffff, Short.SIZE);
	}

	public static IntBitSet of(int value) {
		return of(value, Integer.SIZE);
	}

	public static IntBitSet of(int value, int bits) {
		IntBitSet bitSet = new IntBitSet();
		for (int i = 0; i < bits; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	private IntBitSet() {
		super(Integer.SIZE);
	}

	public int setBits(boolean value, int...bitIndexes) {
		int nextIndex = 0;
		for (int i : bitIndexes) {
			set(i, value);
			nextIndex = i + 1;
		}
		return nextIndex;
	}

	public int setValue(int bitIndex, int value, int bits) {
		bits = Math.min(bits, size() - bitIndex);
		for (int i = 0; i < bits; i++)
			set(bitIndex + i, (1 << i & value) != 0);
		return bitIndex + bits;
	}

	public int getValue(int bitIndex, int bits) {
		bits = Math.min(bits, size() - bitIndex);
		int value = 0;
		for (int i = 0; i < bits; i++)
			if (get(bitIndex + i)) value |= (1 << i);
		return value;
	}

	public Set<Integer> bits() {
		return StreamUtil.toSet(stream().boxed());
	}

	public byte byteValue() {
		return (byte) longValue();
	}

	public short shortValue() {
		return (short) longValue();
	}

	public int value() {
		return (int) longValue();
	}

	public long unsignedValue() {
		return longValue() & ByteUtil.INT_MASK;
	}

	private long longValue() {
		return toLongArray()[0]; // Should never be zero length
	}

}
