package ceri.common.stream;

import java.util.stream.BaseStream;

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
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static IntStream<RuntimeException> ofInt(int... values) {
		return IntStream.of(values);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static LongStream<RuntimeException> ofLong(long... values) {
		return LongStream.of(values);
	}

	/**
	 * Returns a stream of values that supports runtime exceptions.
	 */
	public static DoubleStream<RuntimeException> ofDouble(double... values) {
		return DoubleStream.of(values);
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
}
