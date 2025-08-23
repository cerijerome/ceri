package ceri.common.stream;

import java.util.Map;
import java.util.stream.BaseStream;
import ceri.common.function.Excepts;

/**
 * Utility methods for simplified streams.
 */
public class Streams {
	private Streams() {}

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
	 * Returns a stream from a java stream.
	 */
	public static <T> Stream<RuntimeException, T> from(BaseStream<? extends T, ?> stream) {
		return Stream.from(stream);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static IntStream<RuntimeException> ints(int... values) {
		return IntStream.of(values);
	}

	/**
	 * Returns a stream of a value range, that supports runtime exceptions.
	 */
	public static IntStream<RuntimeException> slice(int offset, int length) {
		return IntStream.slice(offset, length);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static LongStream<RuntimeException> longs(long... values) {
		return LongStream.of(values);
	}

	/**
	 * Returns a stream of a value range, that supports runtime exceptions.
	 */
	public static LongStream<RuntimeException> slice(long offset, long length) {
		return LongStream.slice(offset, length);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static DoubleStream<RuntimeException> doubles(double... values) {
		return DoubleStream.of(values);
	}

	/**
	 * Stream values from 0 to 1 in even steps.
	 */
	public static DoubleStream<RuntimeException> segment(int steps) {
		return DoubleStream.segment(steps);
	}

	/**
	 * Creates a single stream from sequential streams.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Stream<E, T> merge(Stream<E, ? extends T>... streams) {
		return Stream.<E, Stream<E, ? extends T>>of(streams).flatMap(t -> t);
	}

	/**
	 * Convert a map to a stream.
	 */
	public static <E extends Exception, K, V, T> Stream<E, T> unmap(
		Excepts.BiFunction<E, ? super K, ? super V, ? extends T> unmapper,
		Map<? extends K, ? extends V> map) {
		if (map == null || unmapper == null) return Stream.empty();
		return Stream.<E, Map.Entry<? extends K, ? extends V>>from(map.entrySet())
			.map(e -> unmapper.apply(e.getKey(), e.getValue()));
	}
}
