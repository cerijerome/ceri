package ceri.common.util;

public interface Align {

	/**
	 * Reverses alignment.
	 */
	Align reverse();

	/**
	 * Switches between horizontal and vertical alignment.
	 */
	Align invert();

	/**
	 * Get the offset position, rounding up for center/middle alignment. Actual is the length of
	 * item to align, length is the size to fit into. A negative offset may be given if the actual
	 * size is larger than container size. For vertical alignment, top is considered the start.
	 */
	int offset(int actual, int length);

	/**
	 * Get the offset position, rounding up for center/middle alignment. Actual is the length of
	 * item to align, length is the size to fit into.
	 */
	int offsetFloor(int actual, int length);

	public static enum H implements Align {
		left,
		center,
		right;

		@Override
		public int offset(int actual, int length) {
			return offsetFloor(actual, this == center ? length + 1 : length);
		}

		@Override
		public int offsetFloor(int actual, int length) {
			return switch (this) {
				case left -> 0;
				case right -> length - actual;
				default -> (length - actual) >> 1; // allows for -ve
			};
		}

		@Override
		public H reverse() {
			return switch (this) {
				case left -> right;
				case right -> left;
				default -> this;
			};
		}

		@Override
		public V invert() {
			return switch (this) {
				case left -> V.top;
				case right -> V.bottom;
				default -> V.middle;
			};
		}
	}

	public static enum V implements Align {
		top,
		middle,
		bottom;

		@Override
		public int offset(int actual, int length) {
			return offsetFloor(actual, this == middle ? length + 1 : length);
		}

		@Override
		public int offsetFloor(int actual, int length) {
			return switch (this) {
				case top -> 0;
				case bottom -> length - actual;
				default -> (length - actual) >> 1; // allows for -ve
			};
		}

		@Override
		public V reverse() {
			return switch (this) {
				case top -> bottom;
				case bottom -> top;
				default -> this;
			};
		}

		@Override
		public H invert() {
			return switch (this) {
				case top -> H.left;
				case bottom -> H.right;
				default -> H.center;
			};
		}
	}
}
