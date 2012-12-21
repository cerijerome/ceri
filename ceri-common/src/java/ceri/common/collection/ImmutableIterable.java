/**
 * Created on Sep 6, 2007
 */
package ceri.common.collection;

import java.util.Iterator;

/**
 * Wrapper to make an iterable type return an immutable iterator.
 */
public class ImmutableIterable<T> implements Iterable<T> {
	private final Iterable<T> iterable;

	public ImmutableIterable(Iterable<T> iterable) {
		this.iterable = iterable;
	}

	@Override
	public Iterator<T> iterator() {
		return new ImmutableIterator<>(iterable.iterator());
	}

}
