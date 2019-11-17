package ceri.common.collection;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.FunctionWrapper;
import ceri.common.util.BasicUtil;

/**
 * Wrapped stream that allows and throws checked exceptions.
 */
public class WrappedStream<E extends Exception, T> {
	private final FunctionWrapper<E> w;
	private final Stream<T> stream;

	/**
	 * Construct a stream from spliterator tryAdvance function.
	 */
	public static <E extends Exception, T> WrappedStream<E, T>
		stream(ExceptionFunction<E, Consumer<? super T>, Boolean> tryAdvanceFn) {
		return stream(Long.MAX_VALUE, 0, false, tryAdvanceFn);
	}

	/**
	 * Construct a stream from spliterator tryAdvance function.
	 */
	public static <E extends Exception, T> WrappedStream<E, T> stream(long estSize,
		int characteristics, boolean parallel,
		ExceptionFunction<E, Consumer<? super T>, Boolean> tryAdvanceFn) {
		FunctionWrapper<E> wrapper = FunctionWrapper.create();
		Function<Consumer<? super T>, Boolean> wrappedFn = wrapper.wrap(tryAdvanceFn);
		Spliterator<T> spliterator =
			new Spliterators.AbstractSpliterator<>(estSize, characteristics) {
				@Override
				public boolean tryAdvance(Consumer<? super T> action) {
					return wrappedFn.apply(action);
				}
			};
		return WrappedStream.of(wrapper, StreamSupport.stream(spliterator, parallel));
	}

	public static <E extends Exception, T, R> WrappedStream<E, R> mapped(Stream<T> stream,
		ExceptionFunction<E, T, R> fn) {
		return WrappedStream.<E, T>of(stream).map(fn);
	}

	public static <E extends Exception, T, R> WrappedStream<E, R> stream(Collection<T> collection,
		ExceptionFunction<E, T, R> fn) {
		return mapped(collection.stream(), fn);
	}

	public static <E extends Exception, T> WrappedStream<E, T> stream(Class<E> cls,
		Collection<T> collection) {
		return stream(cls, collection);
	}

	public static <E extends Exception, T> WrappedStream<E, T> stream(Collection<T> collection) {
		return of(collection.stream());
	}

	@SafeVarargs
	public static <E extends Exception, T> WrappedStream<E, T> of(Class<E> cls, T... values) {
		return of(cls, Stream.of(values));
	}

	public static <E extends Exception, T> WrappedStream<E, T> of(Class<E> cls, Stream<T> stream) {
		BasicUtil.unused(cls); // for typing
		return of(stream);
	}

	@SafeVarargs
	public static <E extends Exception, T> WrappedStream<E, T> of(T... values) {
		return of(Stream.of(values));
	}

	public static <E extends Exception, T> WrappedStream<E, T> of(Stream<T> stream) {
		return of(FunctionWrapper.create(), stream);
	}

	private static <E extends Exception, T> WrappedStream<E, T> of(FunctionWrapper<E> w,
		Stream<T> stream) {
		return new WrappedStream<>(w, stream);
	}

	WrappedStream(FunctionWrapper<E> w, Stream<T> stream) {
		this.w = w;
		this.stream = stream;
	}

	public <R> WrappedStream<E, R> map(ExceptionFunction<E, T, R> mapFn) {
		return new WrappedStream<>(w, stream.map(w.wrap(mapFn)));
	}

	public WrappedIntStream<E> mapToInt(ExceptionToIntFunction<E, T> mapFn) {
		return new WrappedIntStream<>(w, stream.mapToInt(w.wrap(mapFn)));
	}

	public WrappedStream<E, T> filter(ExceptionPredicate<E, T> predicate) {
		return new WrappedStream<>(w, stream.filter(w.wrap(predicate)));
	}

	public void forEach(ExceptionConsumer<E, T> fn) throws E {
		w.unwrap(() -> stream.forEach(w.wrap(fn)));
	}

	public void collect(ExceptionConsumer<E, T> fn) throws E {
		w.unwrap(() -> stream.forEach(w.wrap(fn)));
	}

	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator,
		BiConsumer<R, R> combiner) throws E {
		return w.unwrapSupplier(() -> stream.collect(supplier, accumulator, combiner));
	}

	public <R, A> R collect(Collector<? super T, A, R> collector) throws E {
		return w.unwrapSupplier(() -> stream.collect(collector));
	}

	public <R> WrappedStream<E, R> apply(Function<Stream<T>, Stream<R>> fn) {
		return new WrappedStream<>(w, fn.apply(stream));
	}

	public WrappedIntStream<E> applyInt(Function<Stream<T>, IntStream> fn) {
		return new WrappedIntStream<>(w, fn.apply(stream));
	}

	public <R> R terminateAs(ExceptionFunction<E, Stream<T>, R> fn) throws E {
		return w.unwrapSupplier(() -> fn.apply(stream));
	}

	public void terminate(ExceptionConsumer<E, Stream<T>> fn) throws E {
		w.unwrap(() -> fn.accept(stream));
	}

}
