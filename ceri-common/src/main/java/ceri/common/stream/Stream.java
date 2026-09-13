package ceri.common.stream;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Where possible, modifiers change the current
 * stream rather than create a new instance. Not thread-safe.
 */
public abstract class Stream<E extends Exception, T> {
	private static final Excepts.Consumer<?, ?> NULL_CONSUMER = _ -> {};
	private NextSupplier<E, T> supplier;

	/**
	 * Iterating functional interface
	 */
	@FunctionalInterface
	public interface NextSupplier<E extends Exception, T> {
		/**
		 * Temporary element receiver.
		 */
		class Receiver<E extends Exception, T> implements Excepts.Consumer<E, T>, Consumer<T> {
			public T value = null;

			@Override
			public void accept(T value) {
				this.value = value;
			}
		}

		/**
		 * Returns true and calls the consumer with the next element, otherwise returns false.
		 */
		boolean next(Excepts.Consumer<? extends E, ? super T> consumer) throws E;

		/**
		 * Iterates over elements.
		 */
		default void forEach(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
			while (next(consumer)) {}
		}

		/**
		 * Populates the receiver with the next filtered element, otherwise returns false.
		 */
		default boolean nextFiltered(Receiver<? extends E, T> receiver,
			Excepts.Predicate<? extends E, ? super T> filter) throws E {
			while (true) {
				if (!next(receiver)) return false;
				if (filter.test(receiver.value)) return true;
			}
		}
	}

	/**
	 * A handler for runtime exceptions that may be thrown by a java stream.
	 */
	public interface ExAdapter<E extends Exception>
		extends Functions.Function<RuntimeException, E> {

		/**
		 * Applies the adapter and throws the exception; if null, the original exception is
		 * re-thrown.
		 */
		default void handle(RuntimeException e) throws E {
			var ex = apply(e);
			if (ex != null) throw ex;
			throw e;
		}
	}

	/**
	 * An implementation that only allows runtime exceptions.
	 */
	public static class Rt<T> extends Stream<RuntimeException, T> {
		private static final Rt<?> EMPTY = new Rt<>(_ -> false);

		/**
		 * Returns an empty instance.
		 */
		public static <T> Rt<T> empty() {
			return Reflect.unchecked(EMPTY);
		}

		/**
		 * Creates a stream from the map and unmapping function.
		 */
		public static <K, V, T> Stream.Rt<T> unmap(
			Functions.BiFunction<? super K, ? super V, ? extends T> unmapper,
			Map<? extends K, ? extends V> map) {
			if (map == null || unmapper == null) return empty();
			return Stream.from(map.entrySet())
				.map(e -> unmapper.apply(e.getKey(), e.getValue()));
		}

		/**
		 * Creates a stream for the supplier.
		 */
		public static <T> Rt<T>
			ofSupplier(NextSupplier<? extends RuntimeException, ? extends T> supplier) {
			return new Rt<>(supplier);
		}

		Rt(NextSupplier<? extends RuntimeException, ? extends T> supplier) {
			super(supplier);
		}

		@Override
		public Rt<T> runtime() {
			return this;
		}

		/**
		 * Adapts the stream to allow the exception type.
		 */
		public <E extends Exception> Ex<E, T> ex() {
			return new Ex<>(Reflect.unchecked(supplier()));
		}

		@Override
		public <E extends Exception> Ex<E, T> ex(ExceptionAdapter<E> adapter) {
			return ex();
		}

		@Override
		public Rt<T> filter(Excepts.Predicate<? extends RuntimeException, ? super T> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter, allowing exceptions.
		 */
		public <E extends Exception> Ex<E, T>
			filterEx(Excepts.Predicate<? extends E, ? super T> filter) {
			return this.<E>ex().filter(filter);
		}

		@Override
		public <U> Rt<U> instances(Class<U> cls) {
			return cast(super.instances(cls));
		}

		@Override
		public Rt<T> nonNull() {
			return cast(super.nonNull());
		}

		@Override
		public Rt<String> string() {
			return cast(super.string());
		}

		@Override
		public <R> Rt<R>
			map(Excepts.Function<? extends RuntimeException, ? super T, ? extends R> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to a new type, allowing exceptions.
		 */
		public <E extends Exception, R> Ex<E, R>
			mapEx(Excepts.Function<? extends E, ? super T, ? extends R> mapper) {
			return this.<E>ex().map(mapper);
		}

		@Override
		public IntStream.Rt
			mapToInt(Excepts.ToIntFunction<? extends RuntimeException, ? super T> mapper) {
			if (super.noOp(mapper)) return IntStream.Rt.EMPTY;
			return IntStream.Rt.ofSupplier(super.intSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream, allowing exception.
		 */
		public <E extends Exception> IntStream.Ex<E>
			mapToIntEx(Excepts.ToIntFunction<? extends E, ? super T> mapper) {
			return this.<E>ex().mapToInt(mapper);
		}

		@Override
		public LongStream.Rt
			mapToLong(Excepts.ToLongFunction<? extends RuntimeException, ? super T> mapper) {
			if (super.noOp(mapper)) return LongStream.Rt.EMPTY;
			return LongStream.Rt.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream, allowing exception.
		 */
		public <E extends Exception> LongStream.Ex<E>
			mapToLongEx(Excepts.ToLongFunction<? extends E, ? super T> mapper) {
			return this.<E>ex().mapToLong(mapper);
		}

		@Override
		public DoubleStream.Rt
			mapToDouble(Excepts.ToDoubleFunction<? extends RuntimeException, ? super T> mapper) {
			if (super.noOp(mapper)) return DoubleStream.Rt.EMPTY;
			return DoubleStream.Rt.ofSupplier(super.doubleSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream, allowing exception.
		 */
		public <E extends Exception> DoubleStream.Ex<E>
			mapToDoubleEx(Excepts.ToDoubleFunction<? extends E, ? super T> mapper) {
			return this.<E>ex().mapToDouble(mapper);
		}

		@Override
		public <R> Rt<R> expand(Excepts.Function<? extends RuntimeException, ? super T, //
			? extends Iterable<? extends R>> mapper) {
			return cast(super.expand(mapper));
		}

		/**
		 * Maps elements to iterable types, and flattens the streams, allowing exceptions. Null
		 * streams are dropped.
		 */
		public <E extends Exception, R> Ex<E, R> expandEx(
			Excepts.Function<? extends E, ? super T, ? extends Iterable<? extends R>> mapper) {
			return this.<E>ex().expand(mapper);
		}

		@Override
		public <R> Rt<R> flatMap(Excepts.Function<? extends RuntimeException, ? super T, //
			? extends Stream<RuntimeException, ? extends R>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams, allowing exceptions. Null
		 * streams are dropped.
		 */
		public <E extends Exception, R> Ex<E, R> flatMapEx(
			Excepts.Function<? extends E, ? super T, ? extends Stream<E, ? extends R>> mapper) {
			return this.<E>ex().flatMap(mapper);
		}

		@Override
		public Rt<T> limit(long size) {
			return cast(super.limit(size));
		}

		@Override
		public Rt<T> distinct() {
			return cast(super.distinct());
		}

		@Override
		public Rt<T> sorted(Comparator<? super T> comparator) {
			return cast(super.sorted(comparator));
		}

		@Override
		public Rt<T> skip(int count) {
			return cast(super.skip(count));
		}

		/**
		 * Calls the consumer for each element.
		 */
		public <E extends Exception> void forEachEx(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
			this.<E>ex().forEach(consumer);
		}
		
		@Override
		<R> Rt<R> update(NextSupplier<? extends RuntimeException, ? extends R> supplier) {
			return new Rt<>(supplier);
		}

		@Override
		<R> Rt<R> emptyVal() {
			return Rt.empty();
		}
	}

	/**
	 * An implementation that allows a checked exception type.
	 */
	public static class Ex<E extends Exception, T> extends Stream<E, T> {
		private static final Ex<?, ?> EMPTY = new Ex<>(_ -> false);

		/**
		 * Returns an empty instance.
		 */
		public static <E extends Exception, T> Ex<E, T> empty() {
			return Reflect.unchecked(EMPTY);
		}

		/**
		 * Creates a stream from the map and unmapping function.
		 */
		public static <E extends Exception, K, V, T> Ex<E, T> unmap(
			Excepts.BiFunction<E, ? super K, ? super V, ? extends T> unmapper,
			Map<? extends K, ? extends V> map) {
			if (map == null || unmapper == null) return empty();
			return Stream.from(map.entrySet())
				.mapEx(e -> unmapper.apply(e.getKey(), e.getValue()));
		}

		/**
		 * Creates a stream for the supplier.
		 */
		public static <E extends Exception, T> Ex<E, T>
			ofSupplier(NextSupplier<? extends E, ? extends T> supplier) {
			return new Ex<>(supplier);
		}

		Ex(NextSupplier<? extends E, ? extends T> supplier) {
			super(supplier);
		}

		@Override
		public Rt<T> runtime() {
			return new Rt<>(super.exSupplier(supplier(), ExceptionAdapter.runtime));
		}

		@Override
		public Ex<E, T> filter(Excepts.Predicate<? extends E, ? super T> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter.
		 */
		public Ex<E, T> filterRt(Excepts.Predicate<? extends RuntimeException, ? super T> filter) {
			return filter(Reflect.unchecked(filter));
		}

		@Override
		public <U> Ex<E, U> instances(Class<U> cls) {
			return cast(super.instances(cls));
		}

		@Override
		public Ex<E, T> nonNull() {
			return cast(super.nonNull());
		}

		@Override
		public Ex<E, String> string() {
			return cast(super.string());
		}

		@Override
		public <R> Ex<E, R> map(Excepts.Function<? extends E, ? super T, ? extends R> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to a new type.
		 */
		public <R> Ex<E, R>
			mapRt(Excepts.Function<? extends RuntimeException, ? super T, ? extends R> mapper) {
			return map(Reflect.unchecked(mapper));
		}

		@Override
		public IntStream.Ex<E> mapToInt(Excepts.ToIntFunction<? extends E, ? super T> mapper) {
			if (super.noOp(mapper)) return IntStream.Ex.empty();
			return IntStream.Ex.ofSupplier(super.intSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream.
		 */
		public IntStream.Ex<E>
			mapToIntRt(Excepts.ToIntFunction<? extends RuntimeException, ? super T> mapper) {
			return mapToInt(Reflect.unchecked(mapper));
		}

		@Override
		public LongStream.Ex<E>
			mapToLong(Excepts.ToLongFunction<? extends E, ? super T> mapper) {
			if (super.noOp(mapper)) return LongStream.Ex.empty();
			return LongStream.Ex.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream.
		 */
		public LongStream.Ex<E>
			mapToLongRt(Excepts.ToLongFunction<? extends RuntimeException, ? super T> mapper) {
			return mapToLong(Reflect.unchecked(mapper));
		}

		@Override
		public DoubleStream.Ex<E>
			mapToDouble(Excepts.ToDoubleFunction<? extends E, ? super T> mapper) {
			if (super.noOp(mapper)) return DoubleStream.Ex.empty();
			return DoubleStream.Ex.ofSupplier(super.doubleSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to a new stream.
		 */
		public DoubleStream.Ex<E>
			mapToDoubleRt(Excepts.ToDoubleFunction<? extends RuntimeException, ? super T> mapper) {
			return mapToDouble(Reflect.unchecked(mapper));
		}

		@Override
		public <R> Ex<E, R> expand(
			Excepts.Function<? extends E, ? super T, ? extends Iterable<? extends R>> mapper) {
			return cast(super.expand(mapper));
		}

		/**
		 * Maps elements to an iterable types, and flattens the streams. Null streams are dropped.
		 */
		public <R> Ex<E, R> expandRt(Excepts.Function<? extends RuntimeException, ? super T, //
			? extends Iterable<? extends R>> mapper) {
			return expand(Reflect.unchecked(mapper));
		}

		@Override
		public <R> Ex<E, R> flatMap(
			Excepts.Function<? extends E, ? super T, ? extends Stream<E, ? extends R>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams. Null streams are dropped.
		 */
		public <R> Ex<E, R> flatMapRt(Excepts.Function<? extends RuntimeException, ? super T, //
			? extends Stream<RuntimeException, ? extends R>> mapper) {
			return flatMap(Reflect.unchecked(mapper));
		}

		@Override
		public Ex<E, T> limit(long size) {
			return cast(super.limit(size));
		}

		@Override
		public Ex<E, T> distinct() {
			return cast(super.distinct());
		}

		@Override
		public Ex<E, T> sorted(Comparator<? super T> comparator) {
			return cast(super.sorted(comparator));
		}

		@Override
		public Ex<E, T> skip(int count) throws E {
			return cast(super.skip(count));
		}

		@Override
		<R> Ex<E, R> update(NextSupplier<? extends E, ? extends R> supplier) {
			return new Ex<>(supplier);
		}

		@Override
		<R> Ex<E, R> emptyVal() {
			return empty();
		}
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <T> Rt<T> ofAll(T... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <T> Rt<T> of(T[] values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <T> Rt<T> of(T[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <T> Rt<T> of(T[] values, int offset, int length) {
		if (values == null) return Rt.empty();
		return RawArray.applySlice(values, offset, length,
			(o, l) -> l == 0 ? Rt.empty() : Rt.ofSupplier(arraySupplier(values, o, l)));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <T> Rt<T> from(Iterable<? extends T> iterable) {
		if (iterable == null) return Rt.empty();
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception, T> Ex<E, T> from(Iterable<? extends T> iterable,
		ExAdapter<E> exAdapter) {
		if (iterable == null) return Ex.empty();
		return from(iterable.iterator(), exAdapter);
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <T> Rt<T> from(Iterator<? extends T> iterator) {
		if (iterator == null || !iterator.hasNext()) return Rt.empty();
		return Rt.ofSupplier(iteratorSupplier(iterator, null));
	}

	/**
	 * Returns a stream of iterator values. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception, T> Ex<E, T> from(Iterator<? extends T> iterator,
		ExAdapter<E> exAdapter) {
		if (iterator == null || !iterator.hasNext()) return Ex.empty();
		return Ex.ofSupplier(iteratorSupplier(iterator, exAdapter));
	}

	/**
	 * Returns a stream from a java stream.
	 */
	public static <T> Rt<T> from(java.util.stream.BaseStream<? extends T, ?> stream) {
		if (stream == null) return Rt.empty();
		return from(stream.spliterator());
	}

	/**
	 * Returns a stream from a java stream. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception, T> Ex<E, T>
		from(java.util.stream.BaseStream<? extends T, ?> stream, ExAdapter<E> exAdapter) {
		if (stream == null) return Ex.empty();
		return from(stream.spliterator(), exAdapter);
	}

	/**
	 * Returns a stream of spliterator values.
	 */
	public static <T> Rt<T> from(Spliterator<? extends T> spliterator) {
		if (spliterator == null) return Rt.empty();
		return Rt.ofSupplier(spliteratorSupplier(spliterator, null));
	}

	/**
	 * Returns a stream of spliterator values. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception, T> Ex<E, T> from(Spliterator<? extends T> spliterator,
		ExAdapter<E> exAdapter) {
		if (spliterator == null) return Ex.empty();
		return Ex.ofSupplier(spliteratorSupplier(spliterator, exAdapter));
	}

	/**
	 * Creates a single stream from sequential streams.
	 */
	@SafeVarargs
	public static <T> Rt<T> merge(Rt<? extends T>... streams) {
		return of(streams).flatMap(t -> t);
	}

	/**
	 * Creates a single stream from sequential streams.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Ex<E, T> merge(Ex<E, ? extends T>... streams) {
		return of(streams).flatMapEx(t -> t);
	}

	Stream(NextSupplier<? extends E, ? extends T> supplier) {
		this.supplier = Reflect.unchecked(supplier);
	}

	/**
	 * Provides access to the supplier.
	 */
	public NextSupplier<E, T> supplier() {
		return supplier;
	}

	// adapters

	/**
	 * Adapts the stream, wrapping unchecked exceptions to runtime as needed.
	 */
	public Rt<T> runtime() {
		return emptyInstance() ? Rt.empty() :
			new Rt<>(exSupplier(supplier(), ExceptionAdapter.runtime));
	}

	/**
	 * Adapts stream exceptions.
	 */
	public <F extends Exception> Ex<F, T> ex(ExceptionAdapter<F> adapter) {
		return emptyInstance() ? Ex.empty() : new Ex<>(exSupplier(supplier(), adapter));
	}

	// filtering

	/**
	 * Only streams elements that match the filter.
	 */
	public Stream<E, T> filter(Excepts.Predicate<? extends E, ? super T> filter) {
		return noOp(filter) ? this : update(filterSupplier(supplier(), filter));
	}

	/**
	 * Filters elements that are instances of the type.
	 */
	public <U> Stream<E, U> instances(Class<U> cls) {
		return map(t -> Reflect.castOrNull(cls, t)).nonNull();
	}

	/**
	 * Drops non-null elements.
	 */
	public Stream<E, T> nonNull() {
		return filter(Objects::nonNull);
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.Predicate<? extends E, ? super T> predicate) throws E {
		return filter(predicate).supplier().next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.Predicate<? extends E, ? super T> predicate) throws E {
		if (noOp(predicate)) return true;
		return !anyMatch(t -> !predicate.test(t));
	}

	/**
	 * Returns true if no elements matched.
	 */
	public boolean noneMatch(Excepts.Predicate<? extends E, ? super T> predicate) throws E {
		return !anyMatch(predicate);
	}

	// mapping

	/**
	 * Maps to string, with null as empty string.
	 */
	public Stream<E, String> string() {
		return map(Strings::safe);
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <R> Stream<E, R> map(Excepts.Function<? extends E, ? super T, ? extends R> mapper) {
		return noOp(mapper) ? emptyVal() : update(mapSupplier(supplier(), mapper));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public abstract IntStream<E> mapToInt(Excepts.ToIntFunction<? extends E, ? super T> mapper);

	/**
	 * Maps stream elements to a new stream.
	 */
	public abstract LongStream<E>
		mapToLong(Excepts.ToLongFunction<? extends E, ? super T> mapper);

	/**
	 * Maps stream elements to a new stream.
	 */
	public abstract DoubleStream<E>
		mapToDouble(Excepts.ToDoubleFunction<? extends E, ? super T> mapper);

	/**
	 * Maps elements to an iterable types, and flattens the streams. Null streams are dropped.
	 */
	public <R> Stream<E, R>
		expand(Excepts.Function<? extends E, ? super T, ? extends Iterable<? extends R>> mapper) {
		return noOp(mapper) ? emptyVal() : flatMap(t -> Stream.from(mapper.apply(t)).ex());

	}

	/**
	 * Maps each element to a stream, and flattens the streams. Null streams are dropped.
	 */
	public <R> Stream<E, R> flatMap(Excepts.Function<? extends E, ? super T, //
		? extends Stream<E, ? extends R>> mapper) {
		return emptyInstance() ? emptyVal() :
			update(flatSupplier(map(mapper).nonNull().supplier()));
	}

	// manipulation

	/**
	 * Limits the number of elements.
	 */
	public Stream<E, T> limit(long size) {
		return emptyInstance() ? this : update(limitSupplier(supplier(), size));
	}

	/**
	 * Streams distinct elements.
	 */
	public Stream<E, T> distinct() {
		return emptyInstance() ? this : filter(Sets.of()::add);
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public Stream<E, T> sorted(Comparator<? super T> comparator) {
		return emptyInstance() ? this : update(sortedSupplier(supplier(), comparator));
	}

	/**
	 * Returns the next element or null.
	 */
	public T next() throws E {
		return next((T) null);
	}

	/**
	 * Returns the next element or default.
	 */
	public T next(T def) throws E {
		var receiver = new NextSupplier.Receiver<E, T>();
		return supplier().next(receiver) ? receiver.value : def;
	}

	/**
	 * Skips up to the given number of elements.
	 */
	public Stream<E, T> skip(int count) throws E {
		var receiver = new NextSupplier.Receiver<E, T>();
		while (count-- > 0)
			if (!supplier().next(receiver)) break;
		return this;
	}

	/**
	 * Consumes the next value and returns false if available.
	 */
	public boolean isEmpty() throws E {
		return !supplier().next(nullConsumer());
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (!supplier().next(nullConsumer())) return n;
	}

	// iteration

	/**
	 * Provides a one-off iterable that wraps checked exceptions as runtime.
	 */
	public Iterable<T> iterable() {
		var iterator = iterator();
		return () -> iterator;
	}

	/**
	 * Provides a one-off iterator that wraps checked exceptions as runtime.
	 */
	public Iterator<T> iterator() {
		return iterator(supplier());
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
		supplier().forEach(consumer);
	}

	// collection

	/**
	 * Adds elements to a collection.
	 */
	public <C extends Collection<? super T>> C add(C collection) throws E {
		if (collection != null) forEach(collection::add);
		return collection;
	}

	/**
	 * Puts elements in a map.
	 */
	public <K, M extends Map<K, T>> M put(M map,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper) throws E {
		return put(map, keyMapper, t -> t);
	}

	/**
	 * Puts elements in a map.
	 */
	public <K, V, M extends Map<K, V>> M put(M map,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper) throws E {
		return put(Maps.Put.def, map, keyMapper, valueMapper);
	}

	/**
	 * Puts elements in a map.
	 */
	public <K, V, M extends Map<K, V>> M put(Maps.Put put, M map,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper) throws E {
		if (map == null || keyMapper == null || valueMapper == null) return map;
		forEach(t -> Maps.put(put, map, keyMapper.apply(t), valueMapper.apply(t)));
		return map;
	}

	/**
	 * Collects elements into an object array.
	 */
	public Object[] toArray() throws E {
		return collect(Collect.array());
	}

	/**
	 * Collects elements into an array.
	 */
	public T[] toArray(Class<T> component) throws E {
		if (component == null) return null;
		return collect(Collect.array(component));
	}

	/**
	 * Collects elements to an immutable set.
	 */
	public Set<T> toSet() throws E {
		if (emptyInstance()) return Immutable.set();
		return collect(Collect.set());
	}

	/**
	 * Collects elements to an immutable list.
	 */
	public List<T> toList() throws E {
		if (emptyInstance()) return Immutable.list();
		return collect(Collect.list());
	}

	/**
	 * Collects mapped elements to an immutable map. Adds mapped elements to a map. Keys replace
	 * older mappings.
	 */
	public <K> Map<K, T> toMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn)
		throws E {
		return toMap(keyFn, t -> t);
	}

	/**
	 * Collects mapped elements to an immutable map. Keys replace older mappings.
	 */
	public <K, V> Map<K, V> toMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn,
		Excepts.Function<? extends E, ? super T, ? extends V> valueFn) throws E {
		if (emptyInstance()) return Immutable.map();
		return Immutable.wrap(put(Maps.of(), keyFn, valueFn));
	}

	/**
	 * Collects elements with a collector.
	 */
	public <A, R> R collect(Collector<? super T, A, R> collector) throws E {
		if (collector == null) return null;
		return collect(collector.supplier(), collector.accumulator(), collector.finisher());
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Supplier<A> supplier, BiConsumer<A, ? super T> accumulator,
		Function<A, R> finisher) throws E {
		return collect(supplier(), supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Reduces stream to an element or null, using an accumulator.
	 */
	public T reduce(Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator) throws E {
		return reduce(accumulator, null);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public T reduce(Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator, T def)
		throws E {
		return reduceStream(supplier(), accumulator, def);
	}

	// support

	/**
	 * Returns an empty instance.
	 */
	abstract <R> Stream<E, R> emptyVal();

	/**
	 * Returns an instance for the supplier.
	 */
	abstract <R> Stream<E, R> update(NextSupplier<? extends E, ? extends R> supplier);

	/**
	 * Cast to concrete type.
	 */
	<F extends Exception, R, S extends Stream<F, R>> S cast(Stream<F, R> stream) {
		return Reflect.unchecked(stream);
	}

	private boolean noOp(Object op) {
		return op == null || emptyInstance();
	}

	private boolean emptyInstance() {
		return this == emptyVal();
	}

	private static <E extends Exception, T> NextSupplier<E, T> filterSupplier(
		NextSupplier<E, T> supplier, Excepts.Predicate<? extends E, ? super T> filter) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!supplier.nextFiltered(receiver, filter)) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception, T, R> NextSupplier<E, R> mapSupplier(
		NextSupplier<E, T> supplier, Excepts.Function<? extends E, ? super T, R> mapper) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		limitSupplier(NextSupplier<E, T> supplier, long size) {
		var counter = Counter.of(size);
		return preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.get())) > 0L);
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		sortedSupplier(NextSupplier<E, T> supplier, Comparator<? super T> comparator) {
		Excepts.Operator<E, NextSupplier<E, T>> adapter =
			s -> iteratorSupplier(sortedList(s, comparator).iterator(), null);
		return adaptedSupplier(supplier, adapter);
	}

	private static <E extends Exception, F extends Exception, T> NextSupplier<F, T>
		exSupplier(NextSupplier<E, T> supplier, ExceptionAdapter<F> adapter) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!adapter.getBool(() -> supplier.next(receiver))) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception, T> Excepts.Consumer<E, T> nullConsumer() {
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private static <E extends Exception, T> IntStream.NextSupplier<E> intSupplier(
		NextSupplier<E, T> supplier, Excepts.ToIntFunction<? extends E, ? super T> mapper) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsInt(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, T> LongStream.NextSupplier<E> longSupplier(
		NextSupplier<E, T> supplier, Excepts.ToLongFunction<? extends E, ? super T> mapper) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsLong(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, T> DoubleStream.NextSupplier<E> doubleSupplier(
		NextSupplier<E, T> supplier, Excepts.ToDoubleFunction<? extends E, ? super T> mapper) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsDouble(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		preSupplier(NextSupplier<E, T> supplier, Excepts.BoolSupplier<? extends E> pre) {
		return c -> {
			if (!pre.getAsBool()) return false;
			return supplier.next(c);
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T> adaptedSupplier(
		NextSupplier<E, T> supplier, Excepts.Operator<E, NextSupplier<E, T>> adapter) {
		var receiver = new NextSupplier.Receiver<E, NextSupplier<E, T>>();
		return c -> { // create the supplier on first call to next
			if (receiver.value == null) receiver.value = adapter.apply(supplier);
			return receiver.value.next(c);
		};
	}

	private static <E extends Exception, T> T reduceStream(NextSupplier<E, T> supplier,
		Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator, T def) throws E {
		if (accumulator == null) return def;
		var receiver = new NextSupplier.Receiver<E, T>();
		if (!supplier.next(receiver)) return def;
		for (T t = receiver.value;;) {
			if (!supplier.next(receiver)) return t;
			t = accumulator.apply(t, receiver.value);
		}
	}

	private static <E extends Exception, T> NextSupplier<E, T> arraySupplier(T[] array, int offset,
		int length) {
		var counter = Counter.of(0);
		return c -> {
			if (counter.get() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		iteratorSupplier(Iterator<? extends T> iterator, ExAdapter<E> exAdapter) {
		return c -> {
			try {
				if (!iterator.hasNext()) return false;
			} catch (RuntimeException e) {
				if (exAdapter != null) exAdapter.handle(e);
				throw e;
			}
			c.accept(iterator.next());
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		spliteratorSupplier(Spliterator<? extends T> spliterator, ExAdapter<E> exAdapter) {
		var receiver = new NextSupplier.Receiver<RuntimeException, T>();
		return c -> {
			try {
				if (!spliterator.tryAdvance(receiver)) return false;
			} catch (RuntimeException e) {
				if (exAdapter != null) exAdapter.handle(e);
				throw e;
			}
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		flatSupplier(NextSupplier<E, ? extends Stream<E, ? extends T>> supplier) {
		var streamReceiver = new NextSupplier.Receiver<E, Stream<E, ? extends T>>();
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			while (true) {
				if (streamReceiver.value == null && !supplier.next(streamReceiver)) return false;
				if (streamReceiver.value.supplier().next(receiver)) break;
				streamReceiver.value = null;
			}
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception, T> List<T> sortedList(NextSupplier<E, T> supplier,
		Comparator<? super T> comparator) throws E {
		var list = Lists.<T>of();
		supplier.forEach(list::add);
		return Lists.sort(list, comparator);
	}

	private static <E extends Exception, T, A, R> R collect(NextSupplier<E, T> next,
		Supplier<A> supplier, BiConsumer<A, ? super T> accumulator, Function<A, R> finisher)
		throws E {
		if (supplier == null || finisher == null) return null;
		var container = supplier.get();
		if (accumulator != null && container != null)
			next.forEach(t -> accumulator.accept(container, t));
		return finisher.apply(container);
	}

	private static <E extends Exception, T> Iterator<T>
		iterator(Stream.NextSupplier<E, ? extends T> supplier) {
		var receiver = new NextSupplier.Receiver<E, T>();
		Excepts.BoolSupplier<E> next = () -> supplier.next(receiver);
		return new Iterator<>() {
			private boolean fetch = true;
			private boolean active = true;

			public boolean hasNext() {
				if (fetch) active = ExceptionAdapter.runtime.getBool(next);
				fetch = false;
				return active;
			}

			public T next() {
				if (!hasNext()) throw new NoSuchElementException();
				fetch = true;
				return receiver.value;
			}
		};
	}
}
