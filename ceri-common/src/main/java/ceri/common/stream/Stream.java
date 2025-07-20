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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import ceri.common.array.DynamicArray;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Modifiers change the current stream rather than
 * create a new instance. Not thread-safe.
 */
public class Stream<E extends Exception, T> implements Excepts.Iterable<E, T> {
	private static final Excepts.Consumer<RuntimeException, Object> NULL_CONSUMER = _ -> {};
	private static final Stream<RuntimeException, Object> EMPTY = new Stream<>(_ -> false);
	private NextSupplier<E, ? extends T> supplier;

	/**
	 * Iterating functional interface
	 */
	@FunctionalInterface
	interface NextSupplier<E extends Exception, T> {
		/**
		 * Temporary element receiver.
		 */
		class Receiver<E extends Exception, T> implements Excepts.Consumer<E, T> {
			public T value = null;

			@Override
			public void accept(T value) throws E {
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
		if (values == null || values.length == 0) return empty();
		return new Stream<>(arraySupplier(values));
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
		return new Stream<>(iteratorSupplier(iterator));
	}

	Stream(NextSupplier<E, ? extends T> supplier) {
		this.supplier = supplier;
	}

	NextSupplier<E, ? extends T> supplier() {
		return supplier;
	}

	/**
	 * Only streams elements that match the filter.
	 */
	public Stream<E, T> filter(Excepts.Predicate<? extends E, ? super T> filter) {
		if (noOp(filter)) return this;
		return update(filterSupplier(supplier, filter));
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

	// mappers

	/**
	 * Maps stream elements to a new type.
	 */
	public <R> Stream<E, R> map(Excepts.Function<? extends E, ? super T, ? extends R> mapper) {
		if (noOp(mapper)) return empty();
		return this.<R>update(mapSupplier(supplier, mapper));
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
	 * Maps each element to a stream, and flattens the streams.
	 */
	public <R> Stream<E, R> flatMap(Excepts.Function<? extends E, ? super T, //
		? extends Stream<E, ? extends R>> mapper) {
		return update(flatSupplier(map(mapper).filter(Objects::nonNull).supplier));
	}

	// manipulators

	/**
	 * Limits the number of elements.
	 */
	public Stream<E, T> limit(long size) {
		var counter = Counter.ofLong(size);
		return update(preSupplier(supplier, () -> counter.count() > 0 && counter.inc(-1) > 0));
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

	// terminating functions

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
	 * Returns the minimum value or null.
	 */
	public T min(Comparator<? super T> comparator) throws E {
		return reduce((t, u) -> comparator.compare(t, u) <= 0 ? t : u);
	}

	/**
	 * Returns the minimum value or default.
	 */
	public T min(Comparator<? super T> comparator, T def) throws E {
		return BasicUtil.def(min(comparator), def);
	}

	/**
	 * Returns the maximum value or null.
	 */
	public T max(Comparator<? super T> comparator) throws E {
		return reduce((t, u) -> comparator.compare(t, u) >= 0 ? t : u);
	}

	/**
	 * Returns the maximum value or default.
	 */
	public T max(Comparator<? super T> comparator, T def) throws E {
		return BasicUtil.def(max(comparator), def);
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (!supplier.next(nullConsumer())) return n;
	}

	// collectors

	@Override
	public Excepts.Iterator<E, T> iterator() {
		return iterator(supplier);
	}

	@Override
	public void forEach(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
		supplier.forEach(consumer);
	}

	/**
	 * Collects elements into an array.
	 */
	public T[] toArray(Functions.IntFunction<T[]> generator) throws E {
		if (generator == null) return null;
		var array = DynamicArray.of(generator);
		forEach(Functions.Consumer.except(array));
		return array.truncate();
	}

	/**
	 * Collects elements.
	 */
	public Set<T> toSet() throws E {
		if (emptyInstance()) return Set.of();
		return Collections.unmodifiableSet(collect(CollectionUtil.supplier.<T>set().get()));
	}

	/**
	 * Collects elements.
	 */
	public List<T> toList() throws E {
		if (emptyInstance()) return List.of();
		return Collections.unmodifiableList(collect(CollectionUtil.supplier.<T>list().get()));
	}

	/**
	 * Collects and sorts elements.
	 */
	public List<T> toList(Comparator<? super T> comparator) throws E {
		if (emptyInstance()) return List.of();
		return Collections.unmodifiableList(sortedList(comparator));
	}

	/**
	 * Collects elements.
	 */
	public <K> Map<K, T> toMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn)
		throws E {
		if (emptyInstance()) return Map.of();
		return Collections
			.unmodifiableMap(collectMap(keyFn, CollectionUtil.supplier.<K, T>map().get()));
	}

	/**
	 * Collects elements.
	 */
	public <K, M extends Map<K, T>> M
		collectMap(Excepts.Function<? extends E, ? super T, ? extends K> keyFn, M map) throws E {
		Objects.requireNonNull(keyFn);
		if (map != null) forEach(t -> map.put(keyFn.apply(t), t));
		return map;
	}

	/**
	 * Collects elements into a collection.
	 */
	public <C extends Collection<T>> C collect(C collection) throws E {
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
	public <A, R> R collect(Supplier<A> supplier, BiConsumer<A, ? super T> accumulator,
		Function<A, R> finisher) throws E {
		if (supplier == null || accumulator == null || finisher == null) return null;
		var container = supplier.get();
		contain(container, accumulator);
		return finisher.apply(container);
	}

	// reducers

	/**
	 * Reduces stream to an element or null, using an accumulator.
	 */
	public T reduce(Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator) throws E {
		if (noOp(accumulator)) return null;
		return reduce(next(), accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public <U> U reduce(U identity,
		Excepts.BiFunction<? extends E, ? super U, ? super T, ? extends U> accumulator) throws E {
		if (noOp(accumulator)) return identity;
		var receiver = new NextSupplier.Receiver<E, T>();
		for (U u = identity;;) {
			if (!supplier.next(receiver)) return u;
			if (receiver.value == null) continue;
			var result = accumulator.apply(u, receiver.value);
			if (result != null) u = result;
		}
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

	private <R> Stream<E, R> update(NextSupplier<E, ? extends R> supplier) {
		if (emptyInstance()) return empty();
		return new Stream<>(supplier);
	}

	private List<T> sortedList(Comparator<? super T> comparator) throws E {
		var list = collect(CollectionUtil.supplier.<T>list().get());
		Collections.sort(list, comparator);
		return list;
	}

	private <R> void contain(R container, BiConsumer<R, ? super T> accumulator) throws E {
		if (container == null || accumulator == null) return;
		var receiver = new NextSupplier.Receiver<E, T>();
		while (true) {
			if (!supplier.next(receiver)) break;
			accumulator.accept(container, receiver.value);
		}
	}

	private static <E extends Exception, T> NextSupplier<E, T> arraySupplier(T[] array) {
		var counter = Counter.ofInt(0);
		return c -> {
			if (counter.count() >= array.length) return false;
			c.accept(array[counter.preInc(1)]);
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
		return c -> {
			if (receiver.value == null) receiver.value = adapter.apply(supplier);
			return receiver.value.next(c);
		};
	}

	private static <E extends Exception, T> List<T> sortedList(NextSupplier<E, T> supplier,
		Comparator<? super T> comparator) throws E {
		var list = new ArrayList<T>();
		supplier.forEach(list::add);
		Collections.sort(list, comparator);
		return list;
	}

	private static <E extends Exception, T> Excepts.Iterator<E, T>
		iterator(NextSupplier<E, ? extends T> supplier) {
		return new Excepts.Iterator<>() {
			private final NextSupplier.Receiver<E, T> receiver = new NextSupplier.Receiver<>();
			private boolean fetch = true;
			private boolean active = true;

			public boolean hasNext() throws E {
				if (fetch) active = supplier.next(receiver);
				fetch = false;
				return active;
			}

			public T next() throws E {
				if (!hasNext()) throw new NoSuchElementException();
				fetch = true;
				return receiver.value;
			}
		};
	}
}
