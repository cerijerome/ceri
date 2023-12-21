package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import static ceri.common.data.IntUtil.LONG_INTS;
import java.util.PrimitiveIterator;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.Iterators;
import ceri.common.function.Fluent;
import ceri.common.math.MathUtil;

/**
 * Interface that provides positional access to ints. For bulk efficiency, consider overriding the
 * following methods that process one int at a time, or copy arrays.
 *
 * <pre>
 * IntProvider slice(int offset, int length); [copy]
 * int copyTo(int index, int[] dest, int destOffset, int length); [1-int]
 * int copyTo(int index, IntReceiver dest, int destOffset, int length); [1-int]
 * int writeTo(int offset, OutputStream out, int length) throws IOException; [1-int]
 * boolean isEqualTo(int index, int[] array, int offset, int length); [1-int]
 * boolean isEqualTo(int index, IntProvider provider, int offset, int length); [1-int]
 * </pre>
 *
 * @see ceri.common.data.IntArray.Immutable
 */
public interface IntProvider extends Iterable<Integer> {

	static IntProvider empty() {
		return IntArray.Immutable.EMPTY;
	}

	static IntProvider of(int... ints) {
		return IntArray.Immutable.wrap(ints);
	}

	/**
	 * {@link Navigator} and {@link IntReader} wrapper for a {@link IntProvider}. This provides
	 * sequential access to ints, and relative/absolute positioning for the next read.
	 * <p/>
	 * IntReader interface is complemented with methods that use remaining ints instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Reader extends Navigator<Reader> implements IntReader, Fluent<Reader> {
		private final IntProvider provider;
		private final int start;

		private Reader(IntProvider provider, int offset, int length) {
			super(length);
			this.provider = provider;
			this.start = offset;
		}

		/* IntReader overrides and additions */

		@Override
		public int readInt() {
			return provider.getInt(inc(1));
		}

		/**
		 * Creates a string from Unicode code points.
		 */
		public String readString() {
			return readString(remaining());
		}

		/**
		 * Reads an array of the remaining ints.
		 */
		public int[] readInts() {
			return readInts(remaining());
		}

		@Override
		public int[] readInts(int length) {
			return provider.copy(inc(length), length);
		}

		@Override
		public int readInto(int[] dest, int offset, int length) {
			return provider.copyTo(inc(length), dest, offset, length);
		}

		@Override
		public int readInto(IntReceiver receiver, int offset, int length) {
			return provider.copyTo(inc(length), receiver, offset, length);
		}

		/**
		 * Provides unsigned ints as a stream.
		 */
		public IntStream stream() {
			return stream(remaining());
		}

		@Override
		public IntStream stream(int length) {
			return provider.stream(inc(length), length);
		}

		/**
		 * Provides unsigned ints as a stream.
		 */
		public LongStream ustream() {
			return ustream(remaining());
		}

		@Override
		public LongStream ustream(int length) {
			return provider.ustream(inc(length), length);
		}

		/* Other methods */

		@Override
		public Reader skip(int length) {
			return super.skip(length);
		}

		/**
		 * Returns a view of the IntProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public IntProvider provider() {
			return provider(remaining());
		}

		/**
		 * Returns a view of the IntProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public IntProvider provider(int length) {
			return provider.slice(inc(length), length);
		}

		/**
		 * Creates a new reader for remaining ints without incrementing the offset.
		 */
		public Reader slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent ints without incrementing the offset.
		 */
		public Reader slice(int length) {
			ArrayUtil.validateSlice(length(), offset(), length);
			return new Reader(provider, start + offset(), length);
		}

		/**
		 * Returns the current position and increments the offset by length.
		 */
		private int inc(int length) {
			int position = start + offset();
			skip(length);
			return position;
		}
	}

	/**
	 * Iterates over integers.
	 */
	@Override
	default PrimitiveIterator.OfInt iterator() {
		return Iterators.intIndexed(length(), this::getInt);
	}

	/**
	 * The number of provided ints.
	 */
	int length();

	/**
	 * Determines if the length is 0.
	 */
	default boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Returns the int at given index.
	 */
	int getInt(int index);

	/**
	 * Returns the unsigned int value at given index.
	 */
	default long getUint(int index) {
		return MathUtil.uint(getInt(index));
	}

	/**
	 * Returns true if int is non-zero at given index.
	 */
	default boolean getBool(int index) {
		return getInt(index) != 0;
	}

	/**
	 * Returns the value from native-order ints at given index.
	 */
	default long getLong(int index) {
		return getLong(index, BIG_ENDIAN);
	}

	/**
	 * Returns the value from little-endian ints at given index.
	 */
	default long getLong(int index, boolean msb) {
		int[] ints = copy(index, LONG_INTS);
		return msb ? IntUtil.longFromMsb(ints) : IntUtil.longFromLsb(ints);
	}

	/**
	 * Returns the value from native-order ints at given index.
	 */
	default float getFloat(int index) {
		return Float.intBitsToFloat(getInt(index));
	}

	/**
	 * Returns the value from native-order ints at given index.
	 */
	default double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}

	/**
	 * Returns the value from endian ints at given index.
	 */
	default double getDouble(int index, boolean msb) {
		return Double.longBitsToDouble(getLong(index, msb));
	}

	/**
	 * Creates a string from Unicode code points.
	 */
	default String getString(int index) {
		return getString(index, length() - index);
	}

	/**
	 * Creates a string from Unicode code points.
	 */
	default String getString(int index, int length) {
		int[] copy = copy(index, length);
		return new String(copy, 0, length);
	}

	/**
	 * Creates an int provider view from index.
	 */
	default IntProvider slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates an int provider sub-view. A negative length will right-justify the view. Returns the
	 * current provider for zero index and same length. Default implementation makes a copy of ints;
	 * efficiency may be improved by overriding this method.
	 */
	default IntProvider slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Returns a copy of provided ints from index.
	 */
	default int[] copy(int index) {
		return copy(index, length() - index);
	}

	/**
	 * Returns a copy of provided ints from index.
	 */
	default int[] copy(int index, int length) {
		if (length == 0) return ArrayUtil.EMPTY_INT;
		ArrayUtil.validateSlice(length(), index, length);
		int[] copy = new int[length];
		copyTo(index, copy, 0, length);
		return copy;
	}

	/**
	 * Copies ints from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, int[] array) {
		return copyTo(index, array, 0);
	}

	/**
	 * Copies ints from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, int[] array, int offset) {
		return copyTo(index, array, offset, array.length - offset);
	}

	/**
	 * Copies ints from index to the array. Returns the index after copying. Default implementation
	 * writes one int at a time; efficiency may be improved by overriding this method.
	 */
	default int copyTo(int index, int[] array, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = getInt(index++);
		return index;
	}

	/**
	 * Copies ints from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, IntReceiver receiver) {
		return copyTo(index, receiver, 0);
	}

	/**
	 * Copies ints from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, IntReceiver receiver, int offset) {
		return copyTo(index, receiver, offset, receiver.length() - offset);
	}

	/**
	 * Copies ints from index to the receiver. Returns the index after copying. Default
	 * implementation writes one int at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyTo(int index, IntReceiver receiver, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setInt(offset++, getInt(index++));
		return index;
	}

	/**
	 * Provides signed ints from index as a stream.
	 */
	default IntStream stream(int index) {
		return stream(index, length() - index);
	}

	/**
	 * Provides signed ints from index as a stream.
	 */
	default IntStream stream(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return IntStream.range(index, index + length).map(i -> getInt(i));
	}

	/**
	 * Provides unsigned ints from index as a stream.
	 */
	default LongStream ustream(int index) {
		return ustream(index, length() - index);
	}

	/**
	 * Provides unsigned ints from index as a stream.
	 */
	default LongStream ustream(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return IntStream.range(index, index + length).mapToLong(i -> getUint(i));
	}

	/**
	 * Returns true if ints are equal to array ints.
	 */
	default boolean isEqualTo(int index, int... array) {
		return isEqualTo(index, array, 0);
	}

	/**
	 * Returns true if ints from index are equal to array ints.
	 */
	default boolean isEqualTo(int index, int[] array, int offset) {
		return isEqualTo(index, array, offset, array.length - offset);
	}

	/**
	 * Returns true if ints from index are equal to array ints.
	 */
	default boolean isEqualTo(int index, int[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
		while (length-- > 0)
			if (getInt(index++) != array[offset++]) return false;
		return true;
	}

	/**
	 * Returns true if ints from index are equal to provider ints.
	 */
	default boolean isEqualTo(int index, IntProvider provider) {
		return isEqualTo(index, provider, 0);
	}

	/**
	 * Returns true if ints from index are equal to provider ints.
	 */
	default boolean isEqualTo(int index, IntProvider provider, int offset) {
		return isEqualTo(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns true if ints from index are equal to provider ints.
	 */
	default boolean isEqualTo(int index, IntProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return false;
		while (length-- > 0)
			if (getInt(index++) != provider.getInt(offset++)) return false;
		return true;
	}

	/**
	 * Returns the first index that matches array ints. Returns -1 if no match.
	 */
	default int indexOf(int index, int... array) {
		return indexOf(index, array, 0);
	}

	/**
	 * Returns the first index that matches array ints. Returns -1 if no match.
	 */
	default int indexOf(int index, int[] array, int offset) {
		return indexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the first index that matches array ints. Returns -1 if no match.
	 */
	default int indexOf(int index, int[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, array, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the first index that matches provider ints. Returns -1 if no match.
	 */
	default int indexOf(int index, IntProvider provider) {
		return indexOf(index, provider, 0);
	}

	/**
	 * Returns the first index that matches provider ints. Returns -1 if no match.
	 */
	default int indexOf(int index, IntProvider provider, int offset) {
		return indexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the first index that matches provider ints. Returns -1 if no match.
	 */
	default int indexOf(int index, IntProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, provider, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the last index that matches array ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, int... array) {
		return lastIndexOf(index, array, 0);
	}

	/**
	 * Returns the last index that matches array ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, int[] array, int offset) {
		return lastIndexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the last index that matches array ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, int[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, array, offset, length)) return i;
		return -1;
	}

	/**
	 * Returns the last index that matches provider ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, IntProvider provider) {
		return lastIndexOf(index, provider, 0);
	}

	/**
	 * Returns the last index that matches provider ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, IntProvider provider, int offset) {
		return lastIndexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the last index that matches provider ints. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, IntProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, provider, offset, length)) return i;
		return -1;
	}

	/**
	 * Provides sequential int access.
	 */
	default Reader reader(int index) {
		return reader(index, length() - index);
	}

	/**
	 * Provides sequential int access.
	 */
	default Reader reader(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Reader(this, index, length);
	}

	/**
	 * Provides a hex string representation.
	 */
	static String toHex(IntProvider provider) {
		return toHex(provider, Integer.MAX_VALUE);
	}

	/**
	 * Provides a limited hex string representation.
	 */
	static String toHex(IntProvider provider, int max) {
		return toString(provider, max, array -> ArrayUtil.toHex(array, 0, array.length));
	}

	/**
	 * Provides a string representation.
	 */
	static String toString(IntProvider provider) {
		return toString(provider, Integer.MAX_VALUE);
	}

	/**
	 * Provides a limited string representation.
	 */
	static String toString(IntProvider provider, int max) {
		return toString(provider, max, array -> ArrayUtil.toString(array, 0, array.length));
	}

	/**
	 * Provides a limited string representation.
	 */
	private static String toString(IntProvider provider, int max, Function<int[], String> fn) {
		int length = provider.length();
		var array = provider.copy(0, length <= max ? length : max - 1);
		String s = fn.apply(array);
		if (length > max) s = s.substring(0, s.length() - 1) + ", ...]";
		return s + "(" + length + ")";
	}

}
