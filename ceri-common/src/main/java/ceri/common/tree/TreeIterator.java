package ceri.common.tree;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

/**
 * Iterates starts with first leaves, then the parent. <code>
 * n-+-n-+-n    1-+-2-+-3
 *   |   |-n      |   |-4
 *   |   `-n ==>  |   `-5
 *   |-n-+-n      |-6-+-7
 *   |   `-n      |   `-8
 *   `-n          `-9
 * </code>
 */
public class TreeIterator<T extends Parent<T>> implements Iterator<T> {
	public final T node;
	private final Deque<Iterator<T>> iterators = new ArrayDeque<>();

	public TreeIterator(T node) {
		this.node = node;
		iterators.add(Collections.singleton(node).iterator());
	}

	@Override
	public boolean hasNext() {
		return iterators.getLast().hasNext();
	}

	@Override
	public T next() {
		var iterator = iterators.getLast();
		T next = iterator.next();
		if (!next.children().isEmpty()) {
			iterator = next.children().iterator();
			iterators.add(iterator);
		} else while (!iterator.hasNext() && iterators.size() > 1) {
			iterators.removeLast();
			iterator = iterators.getLast();
		}
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Tree is immutable.");
	}

}
