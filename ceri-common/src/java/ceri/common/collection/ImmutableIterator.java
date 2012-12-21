/**
 * Created on Sep 6, 2007
 */
package ceri.common.collection;

import java.util.Iterator;

/**
 * Wrapper to make an iterator immutable.
 */
public class ImmutableIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;

	public ImmutableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Iterator is immutable.");
	}

}
