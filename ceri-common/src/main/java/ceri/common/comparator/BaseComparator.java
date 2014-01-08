package ceri.common.comparator;

import java.util.Comparator;

/**
 * Base comparator handling null cases.
 * Null objects are equal, otherwise null is always less than an non-null object.
 */
public abstract class BaseComparator<T> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		if (o1 == o2) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		if (o1.equals(o2)) return 0;
		return compareNonNull(o1, o2);
	}

	/**
	 * Compare objects that we know are not null and not equal.
	 */
	protected abstract int compareNonNull(T o1, T o2);

}
