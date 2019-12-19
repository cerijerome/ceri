package ceri.common.collection;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionIntPredicate;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.FunctionWrapper;

public class WrappedIntStream<E extends Exception> implements AutoCloseable {
	private final FunctionWrapper<E> w;
	private final IntStream stream;

	public static <E extends Exception> WrappedIntStream<E> range(int count) {
		return range(0, count);
	}

	public static <E extends Exception> WrappedIntStream<E> range(int from, int to) {
		return of(IntStream.range(from, to));
	}

	public static <E extends Exception> WrappedIntStream<E> of(int... values) {
		return of(IntStream.of(values));
	}

	public static <E extends Exception> WrappedIntStream<E> of(IntStream stream) {
		return new WrappedIntStream<>(FunctionWrapper.create(), stream);
	}

	WrappedIntStream(FunctionWrapper<E> w, IntStream stream) {
		this.w = w;
		this.stream = stream;
	}

	@Override
	public void close() {
		stream.close();
	}

	public WrappedIntStream<E> map(ExceptionIntUnaryOperator<E> mapFn) {
		return new WrappedIntStream<>(w, stream.map(w.wrap(mapFn)));
	}

	public <R> WrappedStream<E, R> mapToObj(ExceptionIntFunction<E, R> mapFn) {
		return new WrappedStream<>(w, stream.mapToObj(w.wrap(mapFn)));
	}

	public WrappedIntStream<E> filter(ExceptionIntPredicate<E> predicate) {
		return new WrappedIntStream<>(w, stream.filter(w.wrap(predicate)));
	}

	public WrappedStream<E, Integer> boxed() {
		return new WrappedStream<>(w, stream.boxed());
	}

	public void forEach(ExceptionIntConsumer<E> fn) throws E {
		w.unwrap(() -> stream.forEach(w.wrap(fn)));
	}

	public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator,
		BiConsumer<R, R> combiner) throws E {
		return w.unwrapSupplier(() -> stream.collect(supplier, accumulator, combiner));
	}

	public WrappedIntStream<E> apply(Function<IntStream, IntStream> fn) {
		return new WrappedIntStream<>(w, fn.apply(stream));
	}

	public <R> WrappedStream<E, R> applyObj(Function<IntStream, Stream<R>> fn) {
		return new WrappedStream<>(w, fn.apply(stream));
	}

	public <R> R terminateAs(ExceptionFunction<E, IntStream, R> fn) throws E {
		return w.unwrapSupplier(() -> fn.apply(stream));
	}

	public void terminate(ExceptionConsumer<E, IntStream> fn) throws E {
		w.unwrap(() -> fn.accept(stream));
	}

}
