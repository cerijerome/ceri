/**
 * Created on Feb 14, 2006
 */
package ceri.common.comparator;

import java.util.Comparator;

/**
 * A wrapper class to reverse a given comparator.
 */
public class ReverseComparator<T> implements Comparator<T> {
	private final Comparator<T> innerComparator;

	/**
	 * Constructor taking comparator to reverse.
	 */
	private ReverseComparator(Comparator<T> innerComparator) {
		this.innerComparator = innerComparator;
	}

	/**
	 * Constructor taking comparator to reverse.
	 */
	public static <T> ReverseComparator<T> create(Comparator<T> comparator) {
		return new ReverseComparator<>(comparator);
	}

	/**
	 * Comparator interface.
	 */
	@Override
	public int compare(T lhs, T rhs) {
		return innerComparator.compare(rhs, lhs);
	}

}
