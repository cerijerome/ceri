package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import ceri.common.util.BasicUtil;

/**
 * For navigating within a range. T must be the sub-class type, enabling fluent method chains.
 */
public abstract class Navigator<T extends Navigator<T>> {
	private int length = 0;
	private int offset = 0;
	private int mark = 0;

	protected Navigator(int length) {
		validateMin(length, 0);
		this.length = length;
	}

	/**
	 * Returns the current offset.
	 */
	public int offset() {
		return offset;
	}

	/**
	 * Validates and sets the offset.
	 */
	public T offset(int offset) {
		validateRange(offset, 0, length);
		this.offset = offset;
		return typedThis();
	}

	/**
	 * The length of the navigable range.
	 */
	public int length() {
		return length;
	}

	/**
	 * Mark the current offset.
	 */
	public T mark() {
		mark = offset;
		return typedThis();
	}

	/**
	 * Returns the number of bytes past the mark offset.
	 */
	public int marked() {
		return offset() - mark;
	}

	/**
	 * Moves offset back to mark.
	 */
	public T reset() {
		return skip(-marked());
	}

	/**
	 * Returns true if bytes are available.
	 */
	public boolean hasNext() {
		return remaining() > 0;
	}

	/**
	 * Returns the remaining number of bytes.
	 */
	public int remaining() {
		return length() - offset();
	}

	/**
	 * Move forward a number of bytes. Use a negative length to move backwards.
	 */
	public T skip(int length) {
		return offset(offset() + length);
	}

	/**
	 * Updates the length. Available for subclasses if needed.
	 */
	protected T length(int length) {
		validateMin(length, 0);
		this.length = length;
		if (offset() > length) return offset(length);
		return typedThis();
	}
	
	/**
	 * Returns the typed instance.
	 */
	private T typedThis() {
		return BasicUtil.uncheckedCast(this);
	}

}
