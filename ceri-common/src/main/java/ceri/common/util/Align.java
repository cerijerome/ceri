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
	 * Get the offset position, rounding up for center/middle alignment. A negative offset may be
	 * given if the actual size is larger than container size. For vertical alignment, top is
	 * considered the start.
	 */
	int offset(int actual, int length);

	/**
	 * Get the offset position, rounding up for center/middle alignment.
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
			switch (this) {
			case left:
				return 0;
			case right:
				return length - actual;
			default:
				return (length - actual) >> 1; // allows for -ve
			}
		}

		@Override
		public H reverse() {
			switch (this) {
			case left:
				return right;
			case right:
				return left;
			default:
				return this;
			}
		}

		@Override
		public V invert() {
			switch (this) {
			case left:
				return V.top;
			case right:
				return V.bottom;
			default:
				return V.middle;
			}
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
			switch (this) {
			case top:
				return 0;
			case bottom:
				return length - actual;
			default:
				return (length - actual) >> 1; // allows for -ve
			}
		}

		@Override
		public V reverse() {
			switch (this) {
			case top:
				return bottom;
			case bottom:
				return top;
			default:
				return this;
			}
		}

		@Override
		public H invert() {
			switch (this) {
			case top:
				return H.left;
			case bottom:
				return H.right;
			default:
				return H.center;
			}
		}
	}
}
