package ceri.common.data;

import ceri.common.array.ArrayUtil;
import ceri.common.stream.LongStream;
import ceri.common.stream.Streams;
import ceri.common.util.Validate;

/**
 * Interface that provides sequential access to longs. Reads are of known length, or require a given
 * length. For bulk efficiency, consider overriding these methods that process one long at a time,
 * or make a sub-array copy:
 *
 * <pre>
 * LongReader skip(int length); [1-long]
 * int readInto(long[] dest, int offset, int length); [1-long]
 * int readInto(LongReceiver receiver, int offset, int length); [1-long]
 * </pre>
 *
 * @see ceri.common.data.LongProvider.Reader
 */
public interface LongReader {

	/**
	 * Returns the next long value. May throw unchecked exception if no longs remain.
	 */
	long readLong();

	/**
	 * Skip a number of longs. Default implementation skips one long at a time; efficiency may be
	 * improved by overriding.
	 */
	default LongReader skip(int length) {
		while (length-- > 0)
			readLong();
		return this;
	}

	/**
	 * Returns the value from long.
	 */
	default double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Reads a copied array of longs.
	 */
	default long[] readLongs(int length) {
		if (length == 0) return ArrayUtil.longs.empty;
		long[] copy = new long[length];
		readInto(copy);
		return copy;
	}

	/**
	 * Reads longs into array. Returns the array offset after reading.
	 */
	default int readInto(long[] array) {
		return readInto(array, 0);
	}

	/**
	 * Reads longs into array. Returns the array offset after reading.
	 */
	default int readInto(long[] array, int offset) {
		return readInto(array, offset, array.length - offset);
	}

	/**
	 * Reads longs into array. Returns the array offset after reading. Default implementation reads
	 * one long at a time; efficiency may be improved by overriding.
	 */
	default int readInto(long[] array, int offset, int length) {
		Validate.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = readLong();
		return offset;
	}

	/**
	 * Reads long into long receiver. Returns the receiver offset after reading.
	 */
	default int readInto(LongReceiver receiver) {
		return readInto(receiver, 0);
	}

	/**
	 * Reads longs into long receiver. Returns the receiver offset after reading.
	 */
	default int readInto(LongReceiver receiver, int offset) {
		return readInto(receiver, offset, receiver.length() - offset);
	}

	/**
	 * Reads longs into long receiver. Returns the receiver offset after reading. Default
	 * implementation reads one long at a time; efficiency may be improved by overriding.
	 */
	default int readInto(LongReceiver receiver, int offset, int length) {
		Validate.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setLong(offset++, readLong());
		return offset;
	}

	/**
	 * Provides longs as a stream, starting at offset, for given length.
	 */
	default LongStream<RuntimeException> stream(int length) {
		return Streams.slice(0, length).mapToLong(_ -> readLong());
	}
}
