package ceri.common.collection;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReverseListIterator<T> implements Iterator<T> {
	private final ListIterator<T> listIterator;

	public ReverseListIterator(List<T> list) {
		listIterator = list.listIterator(list.size());
	}

	@Override
	public boolean hasNext() {
		return listIterator.hasPrevious();
	}

	@Override
	public T next() {
		return listIterator.previous();
	}

	@Override
	public void remove() {
		listIterator.remove();
	}

}
