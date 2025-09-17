package ceri.common.data;

import ceri.common.array.ArrayUtil;
import ceri.common.math.Maths;
import ceri.common.stream.IntStream;
import ceri.common.stream.LongStream;
import ceri.common.stream.Streams;
import ceri.common.util.Validate;

/**
 * Interface that provides sequential access to ints. Reads are of known length, or require a given
 * length. For bulk efficiency, consider overriding these methods that process one int at a time, or
 * make a sub-array copy:
 *
 * <pre>
 * IntReader skip(int length); [1-int]
 * int readInto(int[] dest, int offset, int length); [1-int]
 * int readInto(IntReceiver receiver, int offset, int length); [1-int]
 * </pre>
 *
 * @see ceri.common.data.IntProvider.Reader
 */
public interface IntReader {

	/**
	 * Returns the next int value. May throw unchecked exception if no ints remain.
	 */
	int readInt();

	/**
	 * Skip a number of ints. Default implementation skips one int at a time; efficiency may be
	 * improved by overriding.
	 */
	default IntReader skip(int length) {
		while (length-- > 0)
			readInt();
		return this;
	}

	/**
	 * Returns the unsigned int value.
	 */
	default long readUint() {
		return Maths.uint(readInt());
	}

	/**
	 * Returns true if int is non-zero.
	 */
	default boolean readBool() {
		return readInt() != 0;
	}

	/**
	 * Returns the value from native-order ints.
	 */
	default long readLong() {
		return readLong(ByteUtil.IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value from endian ints.
	 */
	default long readLong(boolean msb) {
		int[] ints = readInts(IntUtil.LONG_INTS);
		return msb ? IntUtil.longFromMsb(ints) : IntUtil.longFromLsb(ints);
	}

	/**
	 * Returns the value from native-order ints.
	 */
	default float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Returns the value from native-order ints.
	 */
	default double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Returns the value from endian ints.
	 */
	default double readDouble(boolean msb) {
		return Double.longBitsToDouble(readLong(msb));
	}

	/**
	 * Creates a string from Unicode code points.
	 */
	default String readString(int length) {
		int[] ints = readInts(length);
		return new String(ints, 0, ints.length);
	}

	/**
	 * Reads a copied array of ints.
	 */
	default int[] readInts(int length) {
		if (length == 0) return ArrayUtil.ints.empty;
		int[] copy = new int[length];
		readInto(copy);
		return copy;
	}

	/**
	 * Reads ints into array. Returns the array offset after reading.
	 */
	default int readInto(int[] array) {
		return readInto(array, 0);
	}

	/**
	 * Reads ints into array. Returns the array offset after reading.
	 */
	default int readInto(int[] array, int offset) {
		return readInto(array, offset, array.length - offset);
	}

	/**
	 * Reads ints into array. Returns the array offset after reading. Default implementation reads
	 * one int at a time; efficiency may be improved by overriding.
	 */
	default int readInto(int[] array, int offset, int length) {
		Validate.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = readInt();
		return offset;
	}

	/**
	 * Reads int into int receiver. Returns the receiver offset after reading.
	 */
	default int readInto(IntReceiver receiver) {
		return readInto(receiver, 0);
	}

	/**
	 * Reads ints into int receiver. Returns the receiver offset after reading.
	 */
	default int readInto(IntReceiver receiver, int offset) {
		return readInto(receiver, offset, receiver.length() - offset);
	}

	/**
	 * Reads ints into int receiver. Returns the receiver offset after reading. Default
	 * implementation reads one int at a time; efficiency may be improved by overriding.
	 */
	default int readInto(IntReceiver receiver, int offset, int length) {
		Validate.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setInt(offset++, readInt());
		return offset;
	}

	/**
	 * Provides ints as a stream, starting at offset, for given length.
	 */
	default IntStream<RuntimeException> stream(int length) {
		return Streams.slice(0, length).map(_ -> readInt());
	}

	/**
	 * Provides unsigned ints as a stream, starting at offset, for given length.
	 */
	default LongStream<RuntimeException> ustream(int length) {
		return Streams.slice(0, length).mapToLong(_ -> readUint());
	}
}
