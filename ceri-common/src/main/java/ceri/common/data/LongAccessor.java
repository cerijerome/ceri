package ceri.common.data;

import ceri.common.collection.ArrayUtil;

/**
 * Combines LongProvider and LongReceiver interfaces.
 */
public interface LongAccessor extends LongProvider, LongReceiver {
	/**
	 * Create a no-op instance.
	 */
	static LongAccessor ofNull(int length) {
		return length == 0 ? Null.EMPTY : new Null(length);
	}

	@Override
	long getLong(int index);

	@Override
	int setLong(int index, long value);

	@Override
	int length();

	@Override
	default boolean isEmpty() {
		return LongProvider.super.isEmpty();
	}

	@Override
	default LongAccessor slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	default LongAccessor slice(int index, int length) {
		if (length == 0) return Null.EMPTY;
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	static class Null implements LongAccessor {
		public static final Null EMPTY = new Null(0);
		private int length;

		private Null(int length) {
			this.length = length;
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public long getLong(int index) {
			ArrayUtil.validateIndex(length, index);
			return 0;
		}

		@Override
		public int setLong(int index, long value) {
			ArrayUtil.validateIndex(length, index);
			return index + 1;
		}

		@Override
		public LongAccessor slice(int index, int length) {
			ArrayUtil.validateSlice(length(), index, length);
			if (length == 0) return EMPTY;
			if (length == length()) return this;
			return new Null(length);
		}
	}
}
