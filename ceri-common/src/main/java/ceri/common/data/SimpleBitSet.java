package ceri.common.data;

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.math.MathUtil;

/**
 * A bit set with access up to 64 bits.
 */
@SuppressWarnings("serial")
public class SimpleBitSet extends BitSet {

	public static SimpleBitSet from(int... bits) {
		return from(ArrayUtil.intList(bits));
	}

	public static SimpleBitSet from(Collection<Integer> bits) {
		SimpleBitSet bitSet = ofInt(0);
		bits.forEach(bitSet::set);
		return bitSet;
	}

	public static SimpleBitSet of() {
		return new SimpleBitSet();
	}

	public static SimpleBitSet ofByte(int value) {
		return of(MathUtil.ubyte(value), Byte.SIZE);
	}

	public static SimpleBitSet ofShort(int value) {
		return of(MathUtil.ushort(value), Short.SIZE);
	}

	public static SimpleBitSet ofInt(int value) {
		return of(MathUtil.uint(value), Integer.SIZE);
	}

	public static SimpleBitSet ofLong(long value) {
		return of(value, Long.SIZE);
	}

	public static SimpleBitSet of(long value, int bits) {
		SimpleBitSet bitSet = new SimpleBitSet();
		for (int i = 0; i < bits; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	private SimpleBitSet() {
		super(Integer.SIZE);
	}

	public int setBits(boolean value, int... bitIndexes) {
		int nextIndex = 0;
		for (int i : bitIndexes) {
			set(i, value);
			nextIndex = i + 1;
		}
		return nextIndex;
	}

	public int setValue(int bitIndex, int value, int bits) {
		return setValue(bitIndex, MathUtil.uint(value), bits);
	}

	public int setValue(int bitIndex, long value, int bits) {
		bits = Math.min(bits, size() - bitIndex);
		for (int i = 0; i < bits; i++)
			set(bitIndex + i, (1 << i & value) != 0);
		return bitIndex + bits;
	}

	public long getValue(int bitIndex, int bits) {
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

	public short ubyteValue() {
		return MathUtil.ubyte(longValue());
	}

	public short shortValue() {
		return (short) longValue();
	}

	public int ushortValue() {
		return MathUtil.ushort(longValue());
	}

	public int intValue() {
		return (int) longValue();
	}

	public long uintValue() {
		return MathUtil.uint(longValue());
	}

	public long longValue() {
		long[] array = toLongArray();
		return array.length == 0 ? 0L : array[0];
	}
}
