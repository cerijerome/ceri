package ceri.common.data;

import static ceri.common.collection.ArrayUtil.validateFullSlice;
import ceri.common.collection.ArrayUtil;

/**
 * Combines ByteProvider and ByteReceiver interfaces.
 */
public interface ByteAccessor extends ByteProvider, ByteReceiver {
	/**
	 * Create a no-op instance.
	 */
	static ByteAccessor ofNull(int length) {
		return length == 0 ? Null.EMPTY : new Null(length);
	}

	@Override
	byte getByte(int index);

	@Override
	int setByte(int index, int value);

	@Override
	int length();

	@Override
	default boolean isEmpty() {
		return ByteProvider.super.isEmpty();
	}

	@Override
	default ByteAccessor slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	default ByteAccessor slice(int index, int length) {
		if (length == 0) return Null.EMPTY;
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	static class Null implements ByteAccessor {
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
		public byte getByte(int index) {
			ArrayUtil.validateIndex(length, index);
			return 0;
		}

		@Override
		public int setByte(int index, int value) {
			ArrayUtil.validateIndex(length, index);
			return index + 1;
		}

		@Override
		public ByteAccessor slice(int index, int length) {
			if (validateFullSlice(length(), index, length)) return this;
			return ofNull(length);
		}
	}
}
