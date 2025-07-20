package ceri.common.stream;

import java.util.Collection;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.collection.IteratorUtil;
import ceri.common.function.Excepts;
import ceri.common.function.FunctionWrapper;

/**
 * Wrapped stream that allows checked exceptions.
 */
public class WrappedStream<E extends Exception, T> implements AutoCloseable {
	private final FunctionWrapper<E> w;
	private final Stream<T> stream;

	/**
	 * Construct a stream from hasNext and next functions.
	 */
	public static <E extends Exception, T> WrappedStream<E, T>
		stream(Excepts.BoolSupplier<E> hasNextFn, Excepts.Supplier<E, T> nextFn) {
		return stream(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBool()) return false;
			if (nextFn != null) action.accept(nextFn.get());
			return true;
		});
	}

	/**
	 * Construct a stream from spliterator tryAdvance function.
	 */
	public static <E extends Exception, T> WrappedStream<E, T>
		stream(Excepts.Predicate<E, Consumer<? super T>> tryAdvance) {
		var w = FunctionWrapper.<E>of();
		Predicate<Consumer<? super T>> wrapped = c -> w.wrap.getBool(() -> tryAdvance.test(c));
		var spliterator = IteratorUtil.spliterator(wrapped, Long.MAX_VALUE, Spliterator.ORDERED);
		return new WrappedStream<>(w, StreamSupport.stream(spliterator, false));
	}

	/**
	 * Create a wrapped stream from an existing stream and mapping function.
	 */
	@SuppressWarnings("resource") // no files open at this point
	public static <E extends Exception, T, R> WrappedStream<E, R> mapped(Stream<T> stream,
		Excepts.Function<E, T, R> fn) {
		return WrappedStream.<E, T>of(stream).map(fn);
	}

	/**
	 * Create a wrapped stream from a collection and mapping function.
	 */
	public static <E extends Exception, T, R> WrappedStream<E, R> stream(Collection<T> collection,
		Excepts.Function<E, T, R> fn) {
		return mapped(collection.stream(), fn);
	}

	/**
	 * Create a wrapped stream from a collection.
	 */
	public static <E extends Exception, T> WrappedStream<E, T> stream(Collection<T> collection) {
		return WrappedStream.<E, T>of(collection.stream());
	}

	/**
	 * Create a wrapped stream from values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> WrappedStream<E, T> of(T... values) {
		return WrappedStream.<E, T>of(Stream.of(values));
	}

	/**
	 * Create a wrapped stream from an existing stream.
	 */
	public static <E extends Exception, T> WrappedStream<E, T> of(Stream<T> stream) {
		return new WrappedStream<>(FunctionWrapper.<E>of(), stream);
	}

	WrappedStream(FunctionWrapper<E> w, Stream<T> stream) {
		this.w = w;
		this.stream = stream;
	}

	/**
	 * Maps elements for a new stream.
	 */
	public <R> WrappedStream<E, R> map(Excepts.Function<E, T, R> mapper) {
		Function<T, R> wrapped = t -> w.wrap.get(() -> mapper.apply(t));
		return new WrappedStream<>(w, stream.map(wrapped));
	}

	/**
	 * Maps elements for a new int stream.
	 */
	public WrappedIntStream<E> mapToInt(Excepts.ToIntFunction<E, T> mapper) {
		ToIntFunction<T> wrapped = t -> w.wrap.getInt(() -> mapper.applyAsInt(t));
		return new WrappedIntStream<>(w, stream.mapToInt(wrapped));
	}

	/**
	 * Filters elements for a new stream.
	 */
	public WrappedStream<E, T> filter(Excepts.Predicate<E, ? super T> predicate) {
		Predicate<T> wrapped = t -> w.wrap.getBool(() -> predicate.test(t));
		return new WrappedStream<>(w, stream.filter(wrapped));
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(Excepts.Consumer<E, ? super T> consumer) throws E {
		Consumer<T> wrapped = t -> w.wrap.run(() -> consumer.accept(t));
		terminate(s -> s.forEach(wrapped));
	}

	/**
	 * Collects elements.
	 */
	public <R> R collect(Excepts.Supplier<E, R> supplier,
		Excepts.BiConsumer<E, R, ? super T> accumulator, Excepts.BiConsumer<E, R, R> combiner)
		throws E {
		Supplier<R> wrappedSup = () -> w.wrap.get(supplier);
		BiConsumer<R, T> wrappedAcc = (r, t) -> w.wrap.run(() -> accumulator.accept(r, t));
		BiConsumer<R, R> wrappedCom = (l, r) -> w.wrap.run(() -> combiner.accept(l, r));
		return terminateAs(s -> s.collect(wrappedSup, wrappedAcc, wrappedCom));
	}

	/**
	 * Collects elements.
	 */
	public <R, A> R collect(Collector<? super T, A, R> collector) throws E {
		return terminateAs(s -> s.collect(collector));
	}

	/**
	 * Apply the stream function, returning a new wrapped stream.
	 */
	@SuppressWarnings("all") // avoid alternating warnings
	public <R> WrappedStream<E, R>
		apply(Excepts.Function<E, ? super Stream<T>, ? extends Stream<R>> fn) throws E {
		Stream<R> s = fn.apply(stream);
		return new WrappedStream<>(w, s);
	}

	/**
	 * Apply the stream function, returning a new wrapped stream.
	 */
	@SuppressWarnings("resource")
	public WrappedIntStream<E>
		applyInt(Excepts.Function<E, ? super Stream<T>, ? extends IntStream> fn) throws E {
		IntStream intStream = fn.apply(stream);
		return new WrappedIntStream<>(w, intStream);
	}

	/**
	 * Find any element.
	 */
	public Optional<T> findAny() throws E {
		return terminateAs(Stream::findAny);
	}

	/**
	 * Terminate the stream with the given function.
	 */
	public <R> R terminateAs(Excepts.Function<E, ? super Stream<T>, R> fn) throws E {
		return w.unwrap.get(() -> fn.apply(stream));
	}

	/**
	 * Terminate the stream with the given function.
	 */
	public void terminate(Excepts.Consumer<E, ? super Stream<T>> fn) throws E {
		w.unwrap.run(() -> fn.accept(stream));
	}

	@Override
	public void close() {
		stream.close();
	}
}
