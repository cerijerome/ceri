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
		return of(0);
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

	private static IntBitSet of(int value, int bits) {
		IntBitSet bitSet = new IntBitSet();
		for (int i = 0; i < bits; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	private IntBitSet() {
		super(Integer.SIZE);
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

	public Set<Integer> bits() {
		return StreamUtil.toSet(stream().boxed());
	}

	public int value() {
		return (int) longValue();
	}

	public long unsignedValue() {
		return longValue() & ByteUtil.INT_MASK;
	}

	private long longValue() {
		long[] array = toLongArray();
		return array.length == 0 ? 0L : array[0];
	}

}
