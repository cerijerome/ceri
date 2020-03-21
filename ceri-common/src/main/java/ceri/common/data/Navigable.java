package ceri.common.data;

public interface Navigable {

	int offset();

	Navigable offset(int offset);

	int total();

	/**
	 * Mark the current offset.
	 */
	Navigable mark();

	/**
	 * Returns the number of bytes past the mark offset.
	 */
	int marked();

	/**
	 * Moves offset back to mark.
	 */
	default Navigable reset() {
		return rewind(marked());
	}

	/**
	 * Returns true if bytes are available.
	 */
	default boolean hasNext() {
		return remaining() > 0;
	}
	
	/**
	 * Returns the remaining number of bytes.
	 */
	default int remaining() {
		return total() - offset();
	}

	/**
	 * Move forward a number of bytes.
	 */
	default Navigable skip(int length) {
		return offset(offset() + length);
	}

	/**
	 * Move backward a number of bytes.
	 */
	default Navigable rewind(int length) {
		return offset(offset() - length);
	}

}
