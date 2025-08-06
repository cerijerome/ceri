package ceri.common.data;

import java.util.PrimitiveIterator;
import java.util.function.LongFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Iterators;
import ceri.common.function.Fluent;
import ceri.common.text.Joiner;
import ceri.common.validation.ValidationUtil;

/**
 * Interface that provides positional access to longs. For bulk efficiency, consider overriding the
 * following methods that process one long at a time, or copy arrays.
 *
 * <pre>
 * LongProvider slice(int offset, int length); [copy]
 * int copyTo(int index, long[] dest, int destOffset, int length); [1-long]
 * int copyTo(int index, LongReceiver dest, int destOffset, int length); [1-long]
 * boolean isEqualTo(int index, long[] array, int offset, int length); [1-long]
 * boolean isEqualTo(int index, LongProvider provider, int offset, int length); [1-long]
 * </pre>
 *
 * @see ceri.common.data.LongArray.Immutable
 */
public interface LongProvider extends Iterable<Long> {
	/** String formatting configuration. */
	Joiner JOINER = Joiner.of("[", ",", "]", 8);

	/**
	 * Return an unmodifiable empty instance.
	 */
	static LongProvider empty() {
		return LongArray.Immutable.EMPTY;
	}

	/**
	 * Create an unmodifiable wrapper for the given values.
	 */
	static LongProvider of(long... longs) {
		return LongArray.Immutable.wrap(longs);
	}

	/**
	 * Create an unmodifiable copy of the given values.
	 */
	static LongProvider copyOf(long... longs) {
		return LongArray.Immutable.copyOf(longs);
	}

	/**
	 * {@link Navigator} and {@link LongReader} wrapper for a {@link LongProvider}. This provides
	 * sequential access to longs, and relative/absolute positioning for the next read.
	 * <p/>
	 * LongReader interface is complemented with methods that use remaining longs instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Reader extends Navigator<Reader> implements LongReader, Fluent<Reader> {
		private final LongProvider provider;
		private final int start;

		private Reader(LongProvider provider, int offset, int length) {
			super(length);
			this.provider = provider;
			this.start = offset;
		}

		/* LongReader overrides and additions */

		@Override
		public long readLong() {
			return provider.getLong(inc(1));
		}

		/**
		 * Reads an array of the remaining longs.
		 */
		public long[] readLongs() {
			return readLongs(remaining());
		}

		@Override
		public long[] readLongs(int length) {
			return provider.copy(inc(length), length);
		}

		@Override
		public int readInto(long[] dest, int offset, int length) {
			return provider.copyTo(inc(length), dest, offset, length);
		}

		@Override
		public int readInto(LongReceiver receiver, int offset, int length) {
			return provider.copyTo(inc(length), receiver, offset, length);
		}

		/**
		 * Provides longs as a stream.
		 */
		public LongStream stream() {
			return stream(remaining());
		}

		@Override
		public LongStream stream(int length) {
			return provider.stream(inc(length), length);
		}

		/* Other methods */

		@Override
		public Reader skip(int length) {
			return super.skip(length);
		}

		/**
		 * Returns a view of the LongProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public LongProvider provider() {
			return provider(remaining());
		}

		/**
		 * Returns a view of the LongProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public LongProvider provider(int length) {
			return provider.slice(inc(length), length);
		}

		/**
		 * Creates a new reader for remaining longs without incrementing the offset.
		 */
		public Reader slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent longs without incrementing the offset.
		 */
		public Reader slice(int length) {
			ValidationUtil.validateSlice(length(), offset(), length);
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
	 * Iterates over longs.
	 */
	@Override
	default PrimitiveIterator.OfLong iterator() {
		return Iterators.longIndexed(length(), this::getLong);
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
	 * Returns the long at given index.
	 */
	long getLong(int index);

	/**
	 * Returns the value from native-order ints at given index.
	 */
	default double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}

	/**
	 * Creates an long provider view from index.
	 */
	default LongProvider slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates a long provider sub-view. A negative length will right-justify the view. Returns the
	 * current provider for zero index and same length. Default implementation makes a copy of
	 * longs; efficiency may be improved by overriding this method.
	 */
	default LongProvider slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Returns a copy of provided longs from index.
	 */
	default long[] copy(int index) {
		return copy(index, length() - index);
	}

	/**
	 * Returns a copy of provided longs from index.
	 */
	default long[] copy(int index, int length) {
		if (length == 0) return ArrayUtil.longs.empty;
		ValidationUtil.validateSlice(length(), index, length);
		long[] copy = new long[length];
		copyTo(index, copy, 0, length);
		return copy;
	}

	/**
	 * Copies longs from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, long[] array) {
		return copyTo(index, array, 0);
	}

	/**
	 * Copies longs from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, long[] array, int offset) {
		return copyTo(index, array, offset, array.length - offset);
	}

	/**
	 * Copies longs from index to the array. Returns the index after copying. Default implementation
	 * writes one long at a time; efficiency may be improved by overriding this method.
	 */
	default int copyTo(int index, long[] array, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		ValidationUtil.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = getLong(index++);
		return index;
	}

	/**
	 * Copies longs from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, LongReceiver receiver) {
		return copyTo(index, receiver, 0);
	}

	/**
	 * Copies longs from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, LongReceiver receiver, int offset) {
		return copyTo(index, receiver, offset, receiver.length() - offset);
	}

	/**
	 * Copies longs from index to the receiver. Returns the index after copying. Default
	 * implementation writes one long at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyTo(int index, LongReceiver receiver, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		ValidationUtil.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setLong(offset++, getLong(index++));
		return index;
	}

	/**
	 * Provides signed longs from index as a stream.
	 */
	default LongStream stream(int index) {
		return stream(index, length() - index);
	}

	/**
	 * Provides signed longs from index as a stream.
	 */
	default LongStream stream(int index, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		return IntStream.range(index, index + length).mapToLong(i -> getLong(i));
	}

	/**
	 * Returns true if longs are equal to array longs.
	 */
	default boolean isEqualTo(int index, long... array) {
		return isEqualTo(index, array, 0);
	}

	/**
	 * Returns true if longs from index are equal to array longs.
	 */
	default boolean isEqualTo(int index, long[] array, int offset) {
		return isEqualTo(index, array, offset, array.length - offset);
	}

	/**
	 * Returns true if longs from index are equal to array longs.
	 */
	default boolean isEqualTo(int index, long[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
		while (length-- > 0)
			if (getLong(index++) != array[offset++]) return false;
		return true;
	}

	/**
	 * Returns true if longs from index are equal to provider longs.
	 */
	default boolean isEqualTo(int index, LongProvider provider) {
		return isEqualTo(index, provider, 0);
	}

	/**
	 * Returns true if longs from index are equal to provider longs.
	 */
	default boolean isEqualTo(int index, LongProvider provider, int offset) {
		return isEqualTo(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns true if longs from index are equal to provider longs.
	 */
	default boolean isEqualTo(int index, LongProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return false;
		while (length-- > 0)
			if (getLong(index++) != provider.getLong(offset++)) return false;
		return true;
	}

	/**
	 * Returns true if longs contain the given array.
	 */
	default boolean contains(long... array) {
		return indexOf(0, array) >= 0;
	}

	/**
	 * Returns the first index that matches array longs. Returns -1 if no match.
	 */
	default int indexOf(int index, long... array) {
		return indexOf(index, array, 0);
	}

	/**
	 * Returns the first index that matches array longs. Returns -1 if no match.
	 */
	default int indexOf(int index, long[] array, int offset) {
		return indexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the first index that matches array longs. Returns -1 if no match.
	 */
	default int indexOf(int index, long[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, array, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the first index that matches provider longs. Returns -1 if no match.
	 */
	default int indexOf(int index, LongProvider provider) {
		return indexOf(index, provider, 0);
	}

	/**
	 * Returns the first index that matches provider longs. Returns -1 if no match.
	 */
	default int indexOf(int index, LongProvider provider, int offset) {
		return indexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the first index that matches provider longs. Returns -1 if no match.
	 */
	default int indexOf(int index, LongProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, provider, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the last index that matches array longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, long... array) {
		return lastIndexOf(index, array, 0);
	}

	/**
	 * Returns the last index that matches array longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, long[] array, int offset) {
		return lastIndexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the last index that matches array longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, long[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, array, offset, length)) return i;
		return -1;
	}

	/**
	 * Returns the last index that matches provider longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, LongProvider provider) {
		return lastIndexOf(index, provider, 0);
	}

	/**
	 * Returns the last index that matches provider longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, LongProvider provider, int offset) {
		return lastIndexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the last index that matches provider longs. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, LongProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, provider, offset, length)) return i;
		return -1;
	}

	/**
	 * Provides sequential long access.
	 */
	default Reader reader(int index) {
		return reader(index, length() - index);
	}

	/**
	 * Provides sequential long access.
	 */
	default Reader reader(int index, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		return new Reader(this, index, length);
	}

	/**
	 * Provides a limited string representation.
	 */
	static String toHex(LongProvider provider) {
		return toHex(JOINER, provider);
	}

	/**
	 * Provides a string representation.
	 */
	static String toHex(Joiner joiner, LongProvider provider) {
		return toString(joiner, l -> "0x" + Long.toHexString(l), provider);
	}

	/**
	 * Provides a limited string representation.
	 */
	static String toString(LongProvider provider) {
		return toString(JOINER, provider);
	}

	/**
	 * Provides a string representation.
	 */
	static String toString(Joiner joiner, LongProvider provider) {
		return toString(joiner, b -> b, provider);
	}

	/**
	 * Provides a limited string representation.
	 */
	static String toString(LongFunction<?> stringFn, LongProvider provider) {
		return toString(JOINER, stringFn, provider);
	}

	/**
	 * Provides a string representation.
	 */
	static String toString(Joiner joiner, LongFunction<?> stringFn, LongProvider provider) {
		return joiner.joinIndex(i -> stringFn.apply(provider.getLong(i)), provider.length());
	}
}
