package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.supplier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Set;
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
import ceri.common.function.Excepts;
import ceri.common.util.BasicUtil;

/**
 * Utilities to create and modify iterators.
 */
public class IteratorUtil {
	private static final Iterator<Object> NULL_ITERATOR = createNullIterator();
	private static final Iterable<Object> NULL_ITERABLE = () -> NULL_ITERATOR;

	private IteratorUtil() {}

	/**
	 * Returns a no-op iterator.
	 */
	public static <T> Iterator<T> nullIterator() {
		return BasicUtil.unchecked(NULL_ITERATOR);
	}

	/**
	 * Returns a no-op iterable.
	 */
	public static <T> Iterable<T> nullIterable() {
		return BasicUtil.unchecked(NULL_ITERABLE);
	}

	/**
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		if (iterator == null) nullIterable();
		return () -> iterator;
	}

	/**
	 * Returns the nth element of the iterator, or null if none.
	 */
	public static <T> T nth(Iterator<T> iterator, int n) {
		if (iterator == null || n < 0) return null;
		while (true) {
			if (!iterator.hasNext()) return null;
			var t = iterator.next();
			if (--n < 0) return t;
		}
	}

	/**
	 * Iterates each element, passing to a consumer; returns the element count.
	 */
	public static <E extends Exception, T> int forEach(Iterator<T> iterator,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		if (iterator == null || consumer == null) return 0;
		return filterConsume(iterator, null, consumer);
	}

	/**
	 * Removes entries that match the predicate. Returns count of removed entries.
	 */
	public static <E extends Exception, T> int removeIf(Iterator<T> iterator,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		if (iterator == null || predicate == null) return 0;
		return filterConsume(iterator, predicate, _ -> iterator.remove());
	}

	/**
	 * Collects iterator elements.
	 */
	public static <T> List<T> collectAsList(Iterator<? extends T> iterator) {
		return collect(iterator, supplier.<T>list().get());
	}

	/**
	 * Collects iterator elements.
	 */
	public static <T> Set<T> collectAsSet(Iterator<? extends T> iterator) {
		return collect(iterator, supplier.<T>set().get());
	}

	/**
	 * Collects iterator elements.
	 */
	public static <T, C extends Collection<T>> C collect(Iterator<? extends T> iterator,
		C collection) {
		if (collection != null) forEach(iterator, collection::add);
		return collection;
	}

	/**
	 * Iterates each element, matching and passing to a consumer; returns the matched element count.
	 */
	public static <E extends Exception, T> int filterConsume(Iterator<T> iterator,
		Excepts.Predicate<E, ? super T> predicate, Excepts.Consumer<E, ? super T> consumer)
		throws E {
		if (iterator == null) return 0;
		int n = 0;
		while (iterator.hasNext()) {
			var t = iterator.next();
			if (predicate != null && !predicate.test(t)) continue;
			if (consumer != null) consumer.accept(t);
			n++;
		}
		return n;
	}

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

	private static Iterator<Object> createNullIterator() {
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Object next() {
				throw new NoSuchElementException("next");
			}
		};
	}
}
