package ceri.common.collection;

import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utilities to create and modify iterators.
 */
public class Iterators {

	private Iterators() {}

	/**
	 * Returns an iterator from next function based on index with given size.
	 */
	public static <T> Iterator<T> indexed(int size, IntFunction<T> nextFn) {
		return indexed(i -> i < size, nextFn);
	}

	/**
	 * Returns an iterator from hasNext and next functions based on index.
	 */
	public static <T> Iterator<T> indexed(IntPredicate hasNextFn, IntFunction<T> nextFn) {
		return new Iterator<>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return hasNextFn.test(i);
			}

			@Override
			public T next() {
				if (!hasNext()) throw new NoSuchElementException("Index " + i);
				return nextFn.apply(i++);
			}
		};
	}

	/**
	 * Returns an int iterator from next function based on index with given size.
	 */
	public static PrimitiveIterator.OfLong longIndexed(int size, IntToLongFunction nextFn) {
		return longIndexed(i -> i < size, nextFn);
	}

	/**
	 * Returns an int iterator from hasNext and next functions based on index.
	 */
	public static PrimitiveIterator.OfLong longIndexed(IntPredicate hasNextFn,
		IntToLongFunction nextFn) {
		return new PrimitiveIterator.OfLong() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return hasNextFn.test(i);
			}

			@Override
			public long nextLong() {
				if (!hasNext()) throw new NoSuchElementException("Index " + i);
				return nextFn.applyAsLong(i++);
			}
		};
	}

	/**
	 * Returns an int iterator from next function based on index with given size.
	 */
	public static PrimitiveIterator.OfInt intIndexed(int size, IntUnaryOperator nextFn) {
		return intIndexed(i -> i < size, nextFn);
	}

	/**
	 * Returns an int iterator from hasNext and next functions based on index.
	 */
	public static PrimitiveIterator.OfInt intIndexed(IntPredicate hasNextFn,
		IntUnaryOperator nextFn) {
		return new PrimitiveIterator.OfInt() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return hasNextFn.test(i);
			}

			@Override
			public int nextInt() {
				if (!hasNext()) throw new NoSuchElementException("Index " + i);
				return nextFn.applyAsInt(i++);
			}
		};
	}

	/**
	 * Returns a reversed list iterator.
	 */
	public static <T> Iterator<T> reverseList(List<T> list) {
		final ListIterator<T> listIterator = list.listIterator(list.size());
		return new Iterator<>() {
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
		};
	}

	/**
	 * Allows an enumeration to be run in a for-each loop.
	 */
	public static <T> Iterable<T> forEach(final Enumeration<T> enumeration) {
		return enumeration::asIterator;
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> forEachReversedList(final List<T> list) {
		return () -> reverseList(list);
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> forEachReversedQueue(final Deque<T> deque) {
		return deque::descendingIterator;
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> forEachReversedSet(final NavigableSet<T> navSet) {
		return navSet::descendingIterator;
	}

	/**
	 * Construct a spliterator from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true. Pass
	 * Long.MAX_VALUE for estimated size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static <T> Spliterator<T> spliterator(Predicate<Consumer<? super T>> tryAdvanceFn,
		long estimatedSize, int characteristics) {
		return new Spliterators.AbstractSpliterator<>(estimatedSize, characteristics) {
			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				if (tryAdvanceFn == null) return false;
				return tryAdvanceFn.test(action);
			}
		};
	}

	/**
	 * Construct a spliterator from hasNext and next functions. Pass Long.MAX_VALUE for estimated
	 * size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static <T> Spliterator<T> spliterator(BooleanSupplier hasNextFn, Supplier<T> nextFn,
		long estimatedSize, int characteristics) {
		return spliterator(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.get());
			return true;
		}, estimatedSize, characteristics);
	}

	/**
	 * Construct a spliterator from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true. Pass
	 * Long.MAX_VALUE for estimated size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static Spliterator.OfInt intSpliterator(Predicate<IntConsumer> tryAdvanceFn,
		long estimatedSize, int characteristics) {
		return new Spliterators.AbstractIntSpliterator(estimatedSize, characteristics) {
			@Override
			public boolean tryAdvance(IntConsumer action) {
				if (tryAdvanceFn == null) return false;
				return tryAdvanceFn.test(action);
			}
		};
	}

	/**
	 * Construct a spliterator from hasNext and next functions. Pass Long.MAX_VALUE for estimated
	 * size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static Spliterator.OfInt intSpliterator(BooleanSupplier hasNextFn, IntSupplier nextFn,
		long estimatedSize, int characteristics) {
		return intSpliterator(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.getAsInt());
			return true;
		}, estimatedSize, characteristics);
	}

	/**
	 * Construct a spliterator from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true. Pass
	 * Long.MAX_VALUE for estimated size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static Spliterator.OfLong longSpliterator(Predicate<LongConsumer> tryAdvanceFn,
		long estimatedSize, int characteristics) {
		return new Spliterators.AbstractLongSpliterator(estimatedSize, characteristics) {
			@Override
			public boolean tryAdvance(LongConsumer action) {
				if (tryAdvanceFn == null) return false;
				return tryAdvanceFn.test(action);
			}
		};
	}

	/**
	 * Construct a spliterator from hasNext and next functions. Pass Long.MAX_VALUE for estimated
	 * size if unknown. Pass 0 for characteristics if none apply.
	 */
	public static Spliterator.OfLong longSpliterator(BooleanSupplier hasNextFn, LongSupplier nextFn,
		long estimatedSize, int characteristics) {
		return longSpliterator(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.getAsLong());
			return true;
		}, estimatedSize, characteristics);
	}

}
