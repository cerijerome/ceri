package ceri.common.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

/**
 * Iterator support.
 */
public class Iterators {
	private static final Iterator<Object> NULL = nullObj();
	/** A no-op stateless instance. */
	public static final PrimitiveIterator.OfInt nullInt = nullInt();
	/** A no-op stateless instance. */
	public static final PrimitiveIterator.OfLong nullLong = nullLong();
	/** A no-op stateless instance. */
	public static final PrimitiveIterator.OfDouble nullDouble = nullDouble();

	private Iterators() {}

	/**
	 * Returns a no-op, stateless iterator.
	 */
	public static <T> Iterator<T> ofNull() {
		return BasicUtil.unchecked(NULL);
	}

	/**
	 * Returns the next value, or null if unavailable.
	 */
	public static <T> T next(Iterator<? extends T> iterator) {
		return next(iterator, null);
	}

	/**
	 * Returns the next value, or default if unavailable.
	 */
	public static <T> T next(Iterator<? extends T> iterator, T def) {
		if (iterator == null || !iterator.hasNext()) return def;
		return iterator.next();
	}

	/**
	 * Skips up to n elements from the iterator.
	 */
	public static <T> Iterator<T> skip(Iterator<T> iterator, int n) {
		if (iterator != null) while (n-- > 0) {
			if (!iterator.hasNext()) break;
			iterator.next();
		}
		return iterator;
	}

	/**
	 * Returns the nth value (starting from 0), or null if unavailable.
	 */
	public static <T> T nth(Iterator<? extends T> iterator, int n) {
		return nth(iterator, n, null);
	}

	/**
	 * Returns the nth value (starting from 0), or default if unavailable.
	 */
	public static <T> T nth(Iterator<? extends T> iterator, int n, T def) {
		return next(skip(iterator, n), def);
	}
	
	/**
	 * Calls the consumer for each element and returns the element count.
	 */
	public static <E extends Exception, T> int forEach(Iterator<T> iterator,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		if (iterator == null || consumer == null) return 0;
		for (int n = 0;; n++) {
			if (!iterator.hasNext()) return n;
			consumer.accept(iterator.next());
		}
	}

	/**
	 * Removes elements that match the predicate, and returns the number of removed elements.
	 */
	public static <E extends Exception, T> int removeIf(Iterator<T> iterator,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		if (iterator == null) return 0;
		int n = 0;
		while (iterator.hasNext()) {
			if (!predicate.test(iterator.next())) continue;
			iterator.remove();
			n++;
		}
		return n;
	}

	/**
	 * Returns an iterator from next function based on index with given size.
	 */
	public static <T> Iterator<T> indexed(int size, Functions.IntFunction<T> nextFn) {
		return indexed(i -> i < size, nextFn);
	}

	/**
	 * Returns an iterator from hasNext and next functions based on index.
	 */
	public static <T> Iterator<T> indexed(Functions.IntPredicate hasNextFn,
		Functions.IntFunction<T> nextFn) {
		if (hasNextFn == null || nextFn == null) return ofNull();
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
	public static PrimitiveIterator.OfInt intIndexed(int size, Functions.IntOperator nextFn) {
		return intIndexed(i -> i < size, nextFn);
	}

	/**
	 * Returns an int iterator from hasNext and next functions based on index.
	 */
	public static PrimitiveIterator.OfInt intIndexed(Functions.IntPredicate hasNextFn,
		Functions.IntOperator nextFn) {
		if (hasNextFn == null || nextFn == null) return nullInt;
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
	 * Returns a long iterator from next function, based on index with given size.
	 */
	public static PrimitiveIterator.OfLong longIndexed(int size,
		Functions.IntToLongFunction nextFn) {
		return longIndexed(i -> i < size, nextFn);
	}

	/**
	 * Returns a long iterator from hasNext and next functions, based on index.
	 */
	public static PrimitiveIterator.OfLong longIndexed(Functions.IntPredicate hasNextFn,
		Functions.IntToLongFunction nextFn) {
		if (hasNextFn == null || nextFn == null) return nullLong;
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
	 * Returns a double iterator from next function, based on index with given size.
	 */
	public static PrimitiveIterator.OfDouble doubleIndexed(int size,
		Functions.IntToDoubleFunction nextFn) {
		return doubleIndexed(i -> i < size, nextFn);
	}

	/**
	 * Returns a double iterator from hasNext and next functions, based on index.
	 */
	public static PrimitiveIterator.OfDouble doubleIndexed(Functions.IntPredicate hasNextFn,
		Functions.IntToDoubleFunction nextFn) {
		if (hasNextFn == null || nextFn == null) return nullDouble;
		return new PrimitiveIterator.OfDouble() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return hasNextFn.test(i);
			}

			@Override
			public double nextDouble() {
				if (!hasNext()) throw new NoSuchElementException("Index " + i);
				return nextFn.applyAsDouble(i++);
			}
		};
	}

	// support
	
	private static Iterator<Object> nullObj() {
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

	private static PrimitiveIterator.OfInt nullInt() {
		return new PrimitiveIterator.OfInt() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public int nextInt() {
				throw new NoSuchElementException("next");
			}
		};
	}

	private static PrimitiveIterator.OfLong nullLong() {
		return new PrimitiveIterator.OfLong() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public long nextLong() {
				throw new NoSuchElementException("next");
			}
		};
	}

	private static PrimitiveIterator.OfDouble nullDouble() {
		return new PrimitiveIterator.OfDouble() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public double nextDouble() {
				throw new NoSuchElementException("next");
			}
		};
	}
}
