package ceri.common.stream;

import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.function.Excepts;
import ceri.common.function.FunctionWrapper;

/**
 * Wrapped stream that allows checked exceptions.
 */
public class WrappedIntStream<E extends Exception> implements AutoCloseable {
	private final FunctionWrapper<E> w;
	private final IntStream stream;

	/**
	 * Create a wrapped stream from a range of 0 to count - 1.
	 */
	public static <E extends Exception> WrappedIntStream<E> range(int count) {
		return range(0, count);
	}

	/**
	 * Create a wrapped stream from a range inclusive of start, exclusive of end values.
	 */
	public static <E extends Exception> WrappedIntStream<E> range(int startInc, int endExc) {
		return of(IntStream.range(startInc, endExc));
	}

	/**
	 * Create a wrapped stream from values.
	 */
	public static <E extends Exception> WrappedIntStream<E> of(int... values) {
		return of(IntStream.of(values));
	}

	/**
	 * Create a wrapped stream from an existing stream.
	 */
	public static <E extends Exception> WrappedIntStream<E> of(IntStream stream) {
		return new WrappedIntStream<>(FunctionWrapper.<E>of(), stream);
	}

	WrappedIntStream(FunctionWrapper<E> w, IntStream stream) {
		this.w = w;
		this.stream = stream;
	}

	/**
	 * Maps elements for a new stream.
	 */
	public WrappedIntStream<E> map(Excepts.IntOperator<E> mapper) {
		IntUnaryOperator wrapped = i -> w.wrap.getInt(() -> mapper.applyAsInt(i));
		return new WrappedIntStream<>(w, stream.map(wrapped));
	}

	/**
	 * Maps elements for a new stream.
	 */
	public <R> WrappedStream<E, R> mapToObj(Excepts.IntFunction<E, R> mapper) {
		IntFunction<R> wrapped = i -> w.wrap.get(() -> mapper.apply(i));
		return new WrappedStream<>(w, stream.mapToObj(wrapped));
	}

	/**
	 * Filters elements for a new stream.
	 */
	public WrappedIntStream<E> filter(Excepts.IntPredicate<E> predicate) {
		IntPredicate wrapped = i -> w.wrap.getBool(() -> predicate.test(i));
		return new WrappedIntStream<>(w, stream.filter(wrapped));
	}

	/**
	 * convert to a wrapped object stream.
	 */
	public WrappedStream<E, Integer> boxed() {
		return new WrappedStream<>(w, stream.boxed());
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(Excepts.IntConsumer<E> consumer) throws E {
		IntConsumer wrapped = i -> w.wrap.run(() -> consumer.accept(i));
		terminate(s -> s.forEach(wrapped));
	}

	/**
	 * Collects elements.
	 */
	public <R> R collect(Excepts.Supplier<E, R> supplier,
		Excepts.ObjIntConsumer<E, R> accumulator, Excepts.BiConsumer<E, R, R> combiner)
		throws E {
		Supplier<R> wrappedSup = () -> w.wrap.get(supplier);
		ObjIntConsumer<R> wrappedAcc = (r, i) -> w.wrap.run(() -> accumulator.accept(r, i));
		BiConsumer<R, R> wrappedCom = (l, r) -> w.wrap.run(() -> combiner.accept(l, r));
		return terminateAs(s -> s.collect(wrappedSup, wrappedAcc, wrappedCom));
	}

	/**
	 * Apply the stream function, returning a new wrapped stream.
	 */
	public WrappedIntStream<E> apply(Function<IntStream, IntStream> fn) {
		return new WrappedIntStream<>(w, fn.apply(stream));
	}

	/**
	 * Apply the stream function, returning a new wrapped stream.
	 */
	public <R> WrappedStream<E, R> applyObj(Function<IntStream, Stream<R>> fn) {
		return new WrappedStream<>(w, fn.apply(stream));
	}

	/**
	 * Find any element.
	 */
	public OptionalInt findAny() throws E {
		return terminateAs(IntStream::findAny);
	}

	/**
	 * Terminate the stream with the given function.
	 */
	public <R> R terminateAs(Excepts.Function<E, IntStream, R> fn) throws E {
		return w.unwrap.get(() -> fn.apply(stream));
	}

	/**
	 * Terminate the stream with the given function.
	 */
	public void terminate(Excepts.Consumer<E, IntStream> fn) throws E {
		w.unwrap.run(() -> fn.accept(stream));
	}

	@Override
	public void close() {
		stream.close();
	}
}
