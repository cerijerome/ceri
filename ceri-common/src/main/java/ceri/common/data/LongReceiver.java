package ceri.common.data;

import ceri.common.collection.ArrayUtil;

/**
 * Interface for receiving longs into an array. For bulk efficiency, consider overriding the
 * following methods that process one long at a time, or copy arrays.
 *
 * <pre>
 * int fill(int index, int length, long value); [1-long]
 * int copyFrom(int index, long[] array, int offset, int length); [1-long]
 * int copyFrom(int index, LongProvider provider, int offset, int length); [1-long]
 * </pre>
 */
public interface LongReceiver {

	static LongReceiver empty() {
		return LongArray.Mutable.EMPTY;
	}

	static LongReceiver of(long... longs) {
		return LongArray.Mutable.wrap(longs);
	}

	/**
	 * {@link Navigator} and {@link LongWriter} wrapper for a {@link LongReceiver}. This provides
	 * sequential writing of longs, and relative/absolute positioning for the next write. The type T
	 * allows typed access to the LongReceiver.
	 * <p/>
	 * LongWriter interface is complemented with methods that use remaining longs instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Writer extends Navigator<Writer> implements LongWriter<Writer> {
		private final LongReceiver receiver;
		private final int start;

		private Writer(LongReceiver receiver, int offset, int length) {
			super(length);
			this.receiver = receiver;
			this.start = offset;
		}

		/* LongWriter overrides and additions */

		@Override
		public Writer writeLong(long value) {
			return position(receiver.setLong(position(), value));
		}

		/**
		 * Fill remaining longs with same value.
		 */
		public Writer fill(long value) {
			return fill(remaining(), value);
		}

		@Override
		public Writer fill(int length, long value) {
			return position(receiver.fill(position(), length, value));
		}

		@Override
		public Writer writeFrom(long[] array, int offset, int length) {
			return position(receiver.copyFrom(position(), array, offset, length));
		}

		@Override
		public Writer writeFrom(LongProvider provider, int offset, int length) {
			return position(receiver.copyFrom(position(), provider, offset, length));
		}

		/* Other methods */

		@Override
		public Writer skip(int length) {
			return super.skip(length);
		}

		public LongReceiver receiver() {
			return receiver(remaining());
		}

		public LongReceiver receiver(int length) {
			LongReceiver receiver = this.receiver.slice(position(), length);
			position(position() + length);
			return receiver;
		}

		/**
		 * Creates a new reader for remaining longs without incrementing the offset.
		 */
		public Writer slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent longs without incrementing the offset. Use a negative
		 * length to look backwards, which may be useful for checksum calculations.
		 */
		public Writer slice(int length) {
			int offset = length < 0 ? offset() + length : offset();
			length = Math.abs(length);
			ArrayUtil.validateSlice(length(), offset, length);
			return new Writer(receiver, start + offset, length);
		}

		/**
		 * The actual position within the long receiver.
		 */
		private int position() {
			return start + offset();
		}

		/**
		 * Set the offset from receiver actual position.
		 */
		private Writer position(int position) {
			return offset(position - start);
		}
	}

	/**
	 * Length of the space to receive longs.
	 */
	int length();

	/**
	 * Determines if the length is 0.
	 */
	default boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Sets the long value at given index, returns index + 1.
	 */
	int setLong(int index, long value);

	/**
	 * Sets longs at given index. Returns the index after the written longs.
	 */
	default int setLongs(int index, long... array) {
		return copyFrom(index, array);
	}

	/**
	 * Sets the double value from long at the index. Returns the index after the written long.
	 */
	default int setDouble(int index, double value) {
		return setLong(index, Double.doubleToLongBits(value));
	}

	/**
	 * Creates an long receiver view from index.
	 */
	default LongReceiver slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates a long receiver sub-view. A negative length will right-justify the view. Returns the
	 * current receiver for zero index and same length.
	 */
	default LongReceiver slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Fills longs from the index with the same value. Returns the index after the written longs.
	 */
	default int fill(int index, long value) {
		return fill(index, length() - index, value);
	}

	/**
	 * Fills longs from the index with the same value. Returns the index after the written longs.
	 * Default implementation copies one long at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int fill(int index, int length, long value) {
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setLong(index++, value);
		return index;
	}

	/**
	 * Copies longs from the array to the index. Returns the index after the written longs.
	 */
	default int copyFrom(int index, long[] array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies longs from the array to the index. Returns the index after the written longs.
	 */
	default int copyFrom(int index, long[] array, int offset) {
		return copyFrom(index, array, offset, array.length - offset);
	}

	/**
	 * Copies longs from the array to the index. Returns the index after the written longs. Default
	 * implementation copies one long at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyFrom(int index, long[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setLong(index++, array[offset++]);
		return index;
	}

	/**
	 * Copies longs from the provider to the index. Returns the index after the written longs.
	 */
	default int copyFrom(int index, LongProvider array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies longs from the provider to the index. Returns the index after the written longs.
	 */
	default int copyFrom(int index, LongProvider array, int offset) {
		return copyFrom(index, array, offset, array.length() - offset);
	}

	/**
	 * Copies longs from the provider to the index. Returns the index after the written longs.
	 * Default implementation copies one long at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int copyFrom(int index, LongProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(provider.length(), offset, length);
		while (length-- > 0)
			setLong(index++, provider.getLong(offset++));
		return index;
	}

	/**
	 * Provides sequential long access.
	 */
	default Writer writer(int index) {
		return writer(index, length() - index);
	}

	/**
	 * Provides sequential long access.
	 */
	default Writer writer(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Writer(this, index, length);
	}

}
