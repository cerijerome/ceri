package ceri.common.data;

import ceri.common.validation.ValidationUtil;

/**
 * Combines IntProvider and IntReceiver interfaces.
 */
public interface IntAccessor extends IntProvider, IntReceiver {
	/**
	 * Create a no-op instance.
	 */
	static IntAccessor ofNull(int length) {
		return length == 0 ? Null.EMPTY : new Null(length);
	}

	@Override
	int getInt(int index);

	@Override
	int setInt(int index, int value);

	@Override
	int length();

	@Override
	default boolean isEmpty() {
		return IntProvider.super.isEmpty();
	}

	@Override
	default IntAccessor slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	default IntAccessor slice(int index, int length) {
		if (length == 0) return Null.EMPTY;
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	static class Null implements IntAccessor {
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
		public int getInt(int index) {
			ValidationUtil.validateIndex(length, index);
			return 0;
		}

		@Override
		public int setInt(int index, int value) {
			ValidationUtil.validateIndex(length, index);
			return index + 1;
		}

		@Override
		public IntAccessor slice(int index, int length) {
			ValidationUtil.validateSlice(length(), index, length);
			if (length == 0) return EMPTY;
			if (length == length()) return this;
			return new Null(length);
		}
	}
}
