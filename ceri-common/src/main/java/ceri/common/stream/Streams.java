package ceri.common.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.Predicate;
import ceri.common.util.BasicUtil;

/**
 * Utility methods for simplified streams.
 */
public class Streams {
	private Streams() {}

	/**
	 * Cast a runtime exception based stream to a specific exception type.
	 */
	public static <E extends Exception, T> Stream<E, T>
		ex(Stream<? extends RuntimeException, ? extends T> stream) {
		return BasicUtil.unchecked(stream);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	@SafeVarargs
	public static <T> Stream<RuntimeException, T> of(T... values) {
		return Stream.of(values);
	}

	/**
	 * Returns a stream of iterable values that supports runtime exceptions.
	 */
	public static <T> Stream<RuntimeException, T> from(Iterable<T> iterable) {
		return Stream.from(iterable);
	}

	/**
	 * Returns a stream of iterator values that supports runtime exceptions.
	 */
	public static <T> Stream<RuntimeException, T> from(Iterator<T> iterator) {
		return Stream.from(iterator);
	}

	/**
	 * Convenience constructor with filter to set exception type.
	 */
	public static <E extends Exception, T> Stream<E, T> filtered(T[] array,
		Predicate<E, ? super T> filter) {
		return Stream.<E, T>of(array).filter(filter);
	}

	/**
	 * Convenience constructor with filter to set exception type.
	 */
	public static <E extends Exception, T> Stream<E, T> filtered(Iterable<T> iterable,
		Predicate<E, ? super T> filter) {
		return Stream.<E, T>from(iterable).filter(filter);
	}

	/**
	 * Convenience constructor with mapping to set exception type.
	 */
	public static <E extends Exception, T, R> Stream<E, R> mapped(T[] array,
		Function<E, ? super T, ? extends R> mapper) {
		return Stream.<E, T>of(array).map(mapper);
	}

	/**
	 * Convenience constructor with mapping to set exception type.
	 */
	public static <E extends Exception, T, R> Stream<E, R> mapped(Iterable<T> iterable,
		Function<E, ? super T, ? extends R> mapper) {
		return Stream.<E, T>from(iterable).map(mapper);
	}

	/**
	 * Streams elements sorted by natural order.
	 */
	public static <E extends Exception, T extends Comparable<? super T>> Stream<E, T>
		sorted(Stream<E, T> stream) throws E {
		return stream.sorted(Comparator.naturalOrder());
	}

	/**
	 * Collects and sorts elements by natural order.
	 */
	public static <E extends Exception, T extends Comparable<? super T>> List<T>
		sortedList(Stream<? extends E, T> stream) throws E {
		return stream.toList(Comparator.naturalOrder());
	}

	/**
	 * Collects and sorts elements by natural order.
	 */
	public static <E extends Exception, T extends Comparable<? super T>> T[]
		sortedArray(Stream<? extends E, T> stream, IntFunction<T[]> generator) throws E {
		var array = stream.toArray(generator);
		Arrays.sort(array);
		return array;
	}

	/**
	 * Collects and sorts elements by natural order.
	 */
	public static <E extends Exception> int[] sortedArray(IntStream<? extends E> stream) throws E {
		var array = stream.toArray();
		Arrays.sort(array);
		return array;
	}

	/**
	 * Collects and sorts elements by natural order.
	 */
	public static <E extends Exception> long[] sortedArray(LongStream<? extends E> stream)
		throws E {
		var array = stream.toArray();
		Arrays.sort(array);
		return array;
	}

	/**
	 * Collects and sorts elements by natural order.
	 */
	public static <E extends Exception> double[] sortedArray(DoubleStream<? extends E> stream)
		throws E {
		var array = stream.toArray();
		Arrays.sort(array);
		return array;
	}

	/**
	 * Returns the minimum value or null.
	 */
	public static <E extends Exception, T extends Comparable<? super T>> T
		min(Stream<? extends E, T> stream) throws E {
		return stream.min(Comparator.naturalOrder());
	}

	/**
	 * Returns the maximum value or null.
	 */
	public static <E extends Exception, T extends Comparable<? super T>> T
		max(Stream<? extends E, T> stream) throws E {
		return stream.max(Comparator.naturalOrder());
	}

}
