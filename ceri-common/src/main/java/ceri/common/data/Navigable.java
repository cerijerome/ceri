package ceri.common.data;

@Deprecated
public interface Navigable {

	int offset();

	Navigable offset(int offset);

	int length();

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
		return skip(-marked());
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
		return length() - offset();
	}

	/**
	 * Move forward a number of bytes. Use a negative length to move backwards.
	 */
	default Navigable skip(int length) {
		return offset(offset() + length);
	}

}
