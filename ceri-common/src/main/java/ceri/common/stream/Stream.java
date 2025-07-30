package ceri.common.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import ceri.common.array.ArrayUtil;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArrays;
import ceri.common.collection.CollectionUtil;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Where possible, modifiers change the current
 * stream rather than create a new instance. Not thread-safe.
 */
public class Stream<E extends Exception, T> {
	private static final Excepts.Consumer<RuntimeException, Object> NULL_CONSUMER = _ -> {};
	private static final Stream<RuntimeException, Object> EMPTY = new Stream<>(_ -> false);
	private NextSupplier<E, ? extends T> supplier;

	/**
	 * Collection operators
	 */
	public static class Collect {
		private Collect() {}

		/**
		 * A collector composed of method responses.
		 */
		private record Composed<T, A, R>(Supplier<A> supplier, BinaryOperator<A> combiner,
			BiConsumer<A, T> accumulator, Function<A, R> finisher,
			Set<Characteristics> characteristics) implements Collector<T, A, R> {}

		/**
		 * Collects elements into an object array.
		 */
		public static <T> Collector<T, ?, Object[]> array() {
			return array(Object[]::new);
		}

		/**
		 * Collects elements into an array.
		 */
		public static <R, T extends R> Collector<T, ?, R[]>
			array(Functions.IntFunction<R[]> constructor) {
			return of(() -> DynamicArray.of(constructor), DynamicArray.OfType::accept,
				DynamicArray::truncate);
		}

		/**
		 * Collects elements into a sorted array.
		 */
		public static <R extends Comparable<? super R>, T extends R> Collector<T, ?, R[]>
			sortedArray(Functions.IntFunction<R[]> constructor) {
			return sortedArray(constructor, Comparator.naturalOrder());
		}

		/**
		 * Collects elements into a sorted array.
		 */
		public static <R, T extends R> Collector<T, ?, R[]>
			sortedArray(Functions.IntFunction<R[]> constructor, Comparator<? super R> comparator) {
			return of(() -> DynamicArray.of(constructor), DynamicArray.OfType::accept,
				a -> ArrayUtil.sort(a.truncate(), comparator));
		}

		/**
		 * Collects elements into an immutable sorted list.
		 */
		public static <R extends Comparable<? super R>, T extends R> Collector<T, ?, List<R>>
			sortedList() {
			return sortedList(Comparator.naturalOrder());
		}

		/**
		 * Collects elements into an immutable sorted list.
		 */
		public static <R, T extends R> Collector<T, ?, List<R>>
			sortedList(Comparator<? super R> comparator) {
			return of(CollectionUtil.supplier.list(), Collection::add, list -> {
				Collections.sort(list, comparator);
				return Collections.unmodifiableList(list);
			});
		}

		/**
		 * Composes a collector from functions.
		 */
		public static <T, A, R> Collector<T, A, R> of(Supplier<A> supplier,
			BiConsumer<A, T> accumulator, Function<A, R> finisher) {
			return new Composed<>(supplier, null, accumulator, finisher, Set.of());
		}

		/**
		 * Composes a collector without a finisher.
		 */
		public static <T, A> Collector<T, A, A> of(Supplier<A> supplier,
			BiConsumer<A, T> accumulator) {
			return of(supplier, accumulator, t -> t);
		}
	}

	/**
	 * Reduction operators.
	 */
	public static class Reduce {
		private Reduce() {}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception, T extends Comparable<T>> Excepts.BinFunction<E, T, T>
			min() {
			return min(Comparator.naturalOrder());
		}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception, T> Excepts.BinFunction<E, T, T>
			min(Comparator<? super T> comparator) {
			return (l, r) -> comparator.compare(l, r) <= 0 ? l : r;
		}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception, T extends Comparable<T>> Excepts.BinFunction<E, T, T>
			max() {
			return max(Comparator.naturalOrder());
		}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception, T> Excepts.BinFunction<E, T, T>
			max(Comparator<? super T> comparator) {
			return (l, r) -> comparator.compare(l, r) >= 0 ? l : r;
		}
	}

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
	 * Returns an empty stream.
	 */
	public static <E extends Exception, T> Stream<E, T> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Stream<E, T> of(T... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception, T> Stream<E, T> of(T[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception, T> Stream<E, T> of(T[] values, int offset, int length) {
		if (values == null) return empty();
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : ofSupplier(arraySupplier(values, o, l)));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception, T> Stream<E, T> from(Iterable<? extends T> iterable) {
		if (iterable == null) return empty();
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception, T> Stream<E, T> from(Iterator<? extends T> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return ofSupplier(iteratorSupplier(iterator));
	}

	/**
	 * Returns a stream from a java stream.
	 */
	public static <E extends Exception, T> Stream<E, T> from(BaseStream<? extends T, ?> stream) {
		if (stream == null) return empty();
		return from(stream.spliterator());
	}

	/**
	 * Returns a stream of spliterator values.
	 */
	public static <E extends Exception, T> Stream<E, T> from(Spliterator<? extends T> spliterator) {
		if (spliterator == null) return empty();
		return ofSupplier(spliteratorSupplier(spliterator));
	}

	/**
	 * Creates a stream for the supplier.
	 */
	public static <E extends Exception, T> Stream<E, T>
		ofSupplier(NextSupplier<E, ? extends T> supplier) {
		return new Stream<>(supplier);
	}

	Stream(NextSupplier<E, ? extends T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Provides access to the supplier.
	 */
	public NextSupplier<E, ? extends T> supplier() {
		return supplier;
	}

	// filtering

	/**
	 * Only streams elements that match the filter.
	 */
	public Stream<E, T> filter(Excepts.Predicate<? extends E, ? super T> filter) {
		if (noOp(filter)) return this;
		return update(filterSupplier(supplier, filter));
	}

	/**
	 * Filters elements that are instances of the type.
	 */
	public <U> Stream<E, U> instances(Class<U> cls) {
		return map(t -> ReflectUtil.castOrNull(cls, t)).nonNull();
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
		return filter(predicate).supplier.next(nullConsumer());
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
	 * Maps stream elements to a new type.
	 */
	public <R> Stream<E, R> map(Excepts.Function<? extends E, ? super T, ? extends R> mapper) {
		if (noOp(mapper)) return empty();
		return this.<E, R>update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public IntStream<E> mapToInt(Excepts.ToIntFunction<? extends E, ? super T> mapper) {
		if (noOp(mapper)) return IntStream.empty();
		return new IntStream<>(intSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public LongStream<E> mapToLong(Excepts.ToLongFunction<? extends E, ? super T> mapper) {
		if (noOp(mapper)) return LongStream.empty();
		return new LongStream<>(longSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public DoubleStream<E> mapToDouble(Excepts.ToDoubleFunction<? extends E, ? super T> mapper) {
		if (noOp(mapper)) return DoubleStream.empty();
		return new DoubleStream<>(doubleSupplier(supplier, mapper));
	}

	/**
	 * Maps each element to a stream, and flattens the streams. Null streams are dropped.
	 */
	public <R> Stream<E, R> flatMap(Excepts.Function<? extends E, ? super T, //
		? extends Stream<E, ? extends R>> mapper) {
		return update(flatSupplier(map(mapper).nonNull().supplier));
	}

	// manipulation

	/**
	 * Wraps any unchecked exceptions as runtime.
	 */
	public Stream<RuntimeException, T> runtime() {
		return update(runtimeSupplier(supplier));
	}

	/**
	 * Limits the number of elements.
	 */
	public Stream<E, T> limit(long size) {
		if (emptyInstance()) return this;
		var counter = Counter.ofLong(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.count())) > 0L));
	}

	/**
	 * Streams distinct elements.
	 */
	public Stream<E, T> distinct() {
		return filter(new HashSet<T>()::add);
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public Stream<E, T> sorted(Comparator<? super T> comparator) {
		if (emptyInstance()) return this;
		return update(
			adaptedSupplier(supplier, s -> iteratorSupplier(sortedList(s, comparator).iterator())));
	}

	// termination

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
		return supplier.next(receiver) ? receiver.value : def;
	}

	/**
	 * Returns true if no elements are available. Consumes the next value if available.
	 */
	public boolean isEmpty() throws E {
		return !supplier.next(nullConsumer());
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (!supplier.next(nullConsumer())) return n;
	}

	// iteration

	/**
	 * Provides an iterable view that wraps checked exceptions as runtime.
	 */
	public Iterable<T> iterable() {
		return () -> iterator(supplier);
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
		supplier.forEach(consumer);
	}

	// collection

	/**
	 * Collects elements into an object array.
	 */
	public Object[] toArray() throws E {
		return collect(Collect.array());
	}

	/**
	 * Collects elements into an array.
	 */
	public T[] toArray(Functions.IntFunction<T[]> generator) throws E {
		if (generator == null) return null;
		return collect(Collect.array(generator));
	}

	/**
	 * Collects elements to an immutable set.
	 */
	public Set<T> toSet() throws E {
		if (emptyInstance()) return Set.of();
		return Collections.unmodifiableSet(collect(CollectionUtil.supplier.<T>set().get()));
	}

	/**
	 * Collects elements to an immutable list.
	 */
	public List<T> toList() throws E {
		if (emptyInstance()) return List.of();
		return Collections.unmodifiableList(collect(CollectionUtil.supplier.<T>list().get()));
	}

	/**
	 * Collects mapped elements to an immutable map.
	 */
	public <K> Map<K, T> toMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn)
		throws E {
		return toMap(keyFn, t -> t);
	}

	/**
	 * Collects mapped elements to an immutable map.
	 */
	public <K, V> Map<K, V> toMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn,
		Excepts.Function<? extends E, ? super T, ? extends V> valueFn) throws E {
		if (emptyInstance()) return Map.of();
		return Collections
			.unmodifiableMap(collectMap(keyFn, valueFn, CollectionUtil.supplier.<K, V>map().get()));
	}

	/**
	 * Adds mapped elements to a map.
	 */
	public <K, M extends Map<K, T>> M
		collectMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn, M map) throws E {
		return collectMap(keyFn, t -> t, map);
	}

	/**
	 * Adds mapped elements to a map.
	 */
	public <K, V, M extends Map<K, V>> M collectMap(
		Excepts.Function<? extends E, ? super T, ? extends K> keyFn,
		Excepts.Function<? extends E, ? super T, ? extends V> valueFn, M map) throws E {
		if (map != null && keyFn != null && valueFn != null)
			forEach(t -> map.put(keyFn.apply(t), valueFn.apply(t)));
		return map;
	}

	/**
	 * Adds elements to a collection.
	 */
	public <C extends Collection<? super T>> C collect(C collection) throws E {
		if (collection != null) forEach(collection::add);
		return collection;
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
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator) throws E {
		return collect(this.supplier, supplier, accumulator, r -> r);
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Supplier<A> supplier, BiConsumer<A, ? super T> accumulator,
		Function<A, R> finisher) throws E {
		return collect(this.supplier, supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Reduces stream to an element or null, using an accumulator.
	 */
	public T reduce(Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator) throws E {
		if (accumulator == null) return null;
		return reduce(next(), accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public <U> U reduce(U identity,
		Excepts.BiFunction<? extends E, ? super U, ? super T, ? extends U> accumulator) throws E {
		return reduce(supplier, identity, accumulator);
	}

	// support

	/**
	 * Returns true if this is the empty instance.
	 */
	boolean emptyInstance() {
		return this == EMPTY;
	}

	private boolean noOp(Object op) {
		return op == null || emptyInstance();
	}

	private Excepts.Consumer<E, T> nullConsumer() {
		return BasicUtil.unchecked(NULL_CONSUMER);
	}

	private <F extends Exception, R> Stream<F, R> update(NextSupplier<F, ? extends R> supplier) {
		if (emptyInstance()) return empty();
		return new Stream<>(supplier);
	}

	private static <E extends Exception, T> NextSupplier<E, T> arraySupplier(T[] array, int offset,
		int length) {
		var counter = Counter.ofInt(0);
		return c -> {
			if (counter.count() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		iteratorSupplier(Iterator<? extends T> iterator) {
		return c -> {
			if (!iterator.hasNext()) return false;
			c.accept(iterator.next());
			return true;
		};
	}

	private static <E extends Exception, T> NextSupplier<E, T>
		spliteratorSupplier(Spliterator<? extends T> spliterator) {
		var receiver = new NextSupplier.Receiver<RuntimeException, T>();
		return c -> {
			if (!spliterator.tryAdvance(receiver)) return false;
			c.accept(receiver.value);
			return true;
		};
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
		flatSupplier(NextSupplier<E, ? extends Stream<E, ? extends T>> supplier) {
		var streamReceiver = new NextSupplier.Receiver<E, Stream<E, ? extends T>>();
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			while (true) {
				if (streamReceiver.value == null && !supplier.next(streamReceiver)) return false;
				if (streamReceiver.value.supplier.next(receiver)) break;
				streamReceiver.value = null;
			}
			c.accept(receiver.value);
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

	private static <E extends Exception, T> NextSupplier<RuntimeException, T>
		runtimeSupplier(NextSupplier<E, T> supplier) {
		return c -> ExceptionAdapter.runtime.getBool(() -> supplier.next(c::accept));
	}

	private static <E extends Exception, T> List<T> sortedList(NextSupplier<E, T> supplier,
		Comparator<? super T> comparator) throws E {
		var list = new ArrayList<T>();
		supplier.forEach(list::add);
		Collections.sort(list, comparator);
		return list;
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

	private static <E extends Exception, T, U> U reduce(NextSupplier<E, T> supplier, U identity,
		Excepts.BiFunction<? extends E, ? super U, ? super T, ? extends U> accumulator) throws E {
		if (accumulator == null) return identity;
		var receiver = new NextSupplier.Receiver<E, T>();
		for (U u = identity;;) {
			if (!supplier.next(receiver)) return u;
			u = accumulator.apply(u, receiver.value);
		}
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
