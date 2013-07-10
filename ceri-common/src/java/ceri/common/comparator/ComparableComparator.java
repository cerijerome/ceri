/**
 * Created on Aug 25, 2007
 */
package ceri.common.comparator;

/**
 * Comparator for comparable types that handles null cases.
 */
class ComparableComparator<T extends Comparable<? super T>> extends BaseComparator<T> {

	@Override
	protected int compareNonNull(T o1, T o2) {
		return o1.compareTo(o2);
	}

}
