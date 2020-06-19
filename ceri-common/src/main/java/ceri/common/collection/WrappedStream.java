package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.spliterator;
import java.util.Collection;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.function.ExceptionBooleanSupplier;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.FunctionWrapper;

/**
 * Wrapped stream that allows and throws checked exceptions.
 */
public class WrappedStream<E extends Exception, T> implements AutoCloseable {
	private final FunctionWrapper<E> w;
	private final Stream<T> stream;

	/**
	 * Construct a stream from hasNext and next functions.
	 */
	public static <E extends Exception, T> WrappedStream<E, T>
		stream(ExceptionBooleanSupplier<E> hasNextFn, ExceptionSupplier<E, T> nextFn) {
		return stream(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.get());
			return true;
		});
	}

	/**
	 * Construct a stream from spliterator tryAdvance function.
	 */
	public static <E extends Exception, T> WrappedStream<E, T>
		stream(ExceptionPredicate<E, Consumer<? super T>> tryAdvanceFn) {
		FunctionWrapper<E> wrapper = FunctionWrapper.create();
		Predicate<Consumer<? super T>> wrappedFn = wrapper.wrap(tryAdvanceFn);
		Spliterator<T> spliterator = spliterator(wrappedFn, Long.MAX_VALUE, Spliterator.ORDERED);
		return WrappedStream.of(wrapper, StreamSupport.stream(spliterator, false));
	}

	@SuppressWarnings("resource") // no files open at this point
	public static <E extends Exception, T, R> WrappedStream<E, R> mapped(Stream<T> stream,
		ExceptionFunction<E, T, R> fn) {
		return WrappedStream.<E, T>of(stream).map(fn);
	}

	public static <E extends Exception, T, R> WrappedStream<E, R> stream(Collection<T> collection,
		ExceptionFunction<E, T, R> fn) {
		return mapped(collection.stream(), fn);
	}

	public static <E extends Exception, T> WrappedStream<E, T> stream(Collection<T> collection) {
		return of(collection.stream());
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

	@Override
	public void close() {
		stream.close();
	}

	public <R> WrappedStream<E, R> map(ExceptionFunction<E, T, R> mapFn) {
		return new WrappedStream<>(w, stream.map(w.wrap(mapFn)));
	}

	public WrappedIntStream<E> mapToInt(ExceptionToIntFunction<E, T> mapFn) {
		return new WrappedIntStream<>(w, stream.mapToInt(w.wrap(mapFn)));
	}

	public WrappedStream<E, T> filter(ExceptionPredicate<E, ? super T> predicate) {
		return new WrappedStream<>(w, stream.filter(w.wrap(predicate)));
	}

	public void forEach(ExceptionConsumer<E, ? super T> fn) throws E {
		w.unwrap(() -> stream.forEach(w.wrap(fn)));
	}

	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator,
		BiConsumer<R, R> combiner) throws E {
		return w.unwrapSupplier(() -> stream.collect(supplier, accumulator, combiner));
	}

	public <R, A> R collect(Collector<? super T, A, R> collector) throws E {
		return w.unwrapSupplier(() -> stream.collect(collector));
	}

	public <R> WrappedStream<E, R>
		apply(ExceptionFunction<E, ? super Stream<T>, ? extends Stream<R>> fn) throws E {
		return new WrappedStream<>(w, fn.apply(stream));
	}

	@SuppressWarnings("resource")
	public WrappedIntStream<E>
		applyInt(ExceptionFunction<E, ? super Stream<T>, ? extends IntStream> fn) throws E {
		IntStream intStream = fn.apply(stream);
		return new WrappedIntStream<>(w, intStream);
	}

	public Optional<T> findAny() throws E {
		return terminateAs(Stream::findAny);
	}

	public Optional<T> findFirst() throws E {
		return terminateAs(Stream::findFirst);
	}

	public <R> R terminateAs(ExceptionFunction<E, ? super Stream<T>, R> fn) throws E {
		return w.unwrapSupplier(() -> fn.apply(stream));
	}

	public void terminate(ExceptionConsumer<E, ? super Stream<T>> fn) throws E {
		w.unwrap(() -> fn.accept(stream));
	}

}
