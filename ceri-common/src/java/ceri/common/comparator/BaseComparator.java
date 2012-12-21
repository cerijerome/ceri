/**
 * Created on Aug 25, 2007
 */
package ceri.common.comparator;

import java.util.Comparator;

/**
 * Base comparator handling null cases.
 */
public abstract class BaseComparator<T> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		if (o1 == null && o2 == null) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		if (o1 == o2 || o1.equals(o2)) return 0;
		return compareNonNull(o1, o2);
	}

	/**
	 * Compare objects that we know are not null.
	 */
	protected abstract int compareNonNull(T o1, T o2);

}
