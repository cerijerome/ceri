package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Fluent;
import ceri.common.util.BasicUtil;

/**
 * Interface that writes ints sequentially. Type T must be the sub-class type; this allows fluent
 * method calls without redefining all the methods with covariant return types.
 * <p>
 * For bulk efficiency, consider overriding these methods that process one int at a time, or make a
 * sub-array copy:
 *
 * <pre>
 * T skip(int length); [1-int]
 * T fill(int length, int value); [1-int]
 * T writeFrom(int[] dest, int offset, int length); [1-int]
 * T writeFrom(IntProvider provider, int offset, int length); [1-int]
 * </pre>
 *
 * @see ceri.common.data.IntReceiver.Writer
 */
public interface IntWriter<T extends IntWriter<T>> extends Fluent<T> {

	/**
	 * Writes an int. May throw unchecked exception if no capacity to write.
	 */
	T writeInt(int value);

	/**
	 * Skips ints. By default this writes ints with value 0.
	 */
	default T skip(int length) {
		return fill(length, 0);
	}

	/**
	 * Writes int 1 for true, 0 for false.
	 */
	default T writeBool(boolean value) {
		return writeInt(value ? 1 : 0);
	}

	/**
	 * Writes native-order ints.
	 */
	default T writeLong(long value) {
		return writeLong(value, BIG_ENDIAN);
	}

	/**
	 * Writes endian ints.
	 */
	default T writeLong(long value, boolean msb) {
		int[] ints = msb ? IntUtil.longToMsb(value) : IntUtil.longToLsb(value);
		return writeInts(ints);
	}

	/**
	 * Writes native-order ints.
	 */
	default T writeFloat(float value) {
		return writeInt(Float.floatToIntBits(value));
	}

	/**
	 * Writes native-order ints.
	 */
	default T writeDouble(double value) {
		return writeLong(Double.doubleToLongBits(value));
	}

	/**
	 * Writes endian ints.
	 */
	default T writeDouble(double value, boolean msb) {
		return writeLong(Double.doubleToLongBits(value), msb);
	}

	/**
	 * Writes the string Unicode code points.
	 */
	default T writeString(String s) {
		int[] ints = s.codePoints().toArray();
		return writeInts(ints);
	}

	/**
	 * Fill ints with same value. Default implementation fills one int at a time; efficiency may be
	 * improved by overriding.
	 */
	default T fill(int length, int value) {
		while (length-- > 0)
			writeInt(value);
		return BasicUtil.uncheckedCast(this);
	}

	/**
	 * Writes ints from array.
	 */
	default T writeInts(int... array) {
		return writeFrom(array, 0);
	}

	/**
	 * Writes ints from array.
	 */
	default T writeFrom(int[] array, int offset) {
		return writeFrom(array, offset, array.length - offset);
	}

	/**
	 * Writes ints from array. Default implementation writes one int at a time; efficiency may be
	 * improved by overriding.
	 */
	default T writeFrom(int[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		for (int i = 0; i < length; i++)
			writeInt(array[offset + i]);
		return BasicUtil.uncheckedCast(this);
	}

	/**
	 * Writes ints from int provider.
	 */
	default T writeFrom(IntProvider provider) {
		return writeFrom(provider, 0);
	}

	/**
	 * Writes ints from int provider.
	 */
	default T writeFrom(IntProvider provider, int offset) {
		return writeFrom(provider, offset, provider.length() - offset);
	}

	/**
	 * Writes ints from int provider. Default implementation writes one int at a time; efficiency
	 * may be improved by overriding.
	 */
	default T writeFrom(IntProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(provider.length(), offset, length);
		for (int i = 0; i < length; i++)
			writeInt(provider.getInt(offset + i));
		return BasicUtil.uncheckedCast(this);
	}

}
