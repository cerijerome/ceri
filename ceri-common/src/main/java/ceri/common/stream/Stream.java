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
import java.util.stream.BaseStream;
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
public class Stream<E extends Exception, T> {
	private static final Excepts.Consumer<RuntimeException, Object> NULL_CONSUMER = _ -> {};
	private static final Stream<RuntimeException, Object> EMPTY = new Stream<>(_ -> false);
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
	 * Returns an empty stream.
	 */
	public static <E extends Exception, T> Stream<E, T> empty() {
		return Reflect.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Stream<E, T> ofAll(T... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception, T> Stream<E, T> of(T[] values) {
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
		return RawArray.applySlice(values, offset, length,
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
		this.supplier = Reflect.unchecked(supplier);
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
	 * Maps to string, with null as empty string.
	 */
	public Stream<E, String> string() {
		return map(Strings::safe);
	}

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
	 * Maps elements to an iterable types, and flattens the streams. Null streams are dropped.
	 */
	public <R> Stream<E, R>
		expand(Excepts.Function<? extends E, ? super T, ? extends Iterable<? extends R>> mapper) {
		if (noOp(mapper)) return empty();
		return flatMap(t -> Stream.from(mapper.apply(t)));
	}

	/**
	 * Maps each element to a stream, and flattens the streams. Null streams are dropped.
	 */
	public <R> Stream<E, R>
		flatMap(Excepts.Function<? extends E, ? super T, ? extends Stream<E, ? extends R>> mapper) {
		return update(flatSupplier(map(mapper).nonNull().supplier));
	}

	// manipulation

	/**
	 * Wraps any unchecked exceptions as runtime.
	 */
	public Stream<RuntimeException, T> runtime() {
		return ex(ExceptionAdapter.runtime);
	}

	/**
	 * Adapts stream exceptions.
	 */
	public <F extends Exception> Stream<F, T> ex(ExceptionAdapter<F> adapter) {
		return update(exSupplier(supplier, adapter));
	}

	/**
	 * Limits the number of elements.
	 */
	public Stream<E, T> limit(long size) {
		if (emptyInstance()) return this;
		var counter = Counter.of(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.get())) > 0L));
	}

	/**
	 * Streams distinct elements.
	 */
	public Stream<E, T> distinct() {
		return filter(Sets.of()::add);
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public Stream<E, T> sorted(Comparator<? super T> comparator) {
		if (emptyInstance()) return this;
		Excepts.Operator<E, NextSupplier<E, T>> adapter =
			s -> iteratorSupplier(sortedList(s, comparator).iterator());
		return update(adaptedSupplier(supplier, adapter));
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
		return supplier.next(receiver) ? receiver.value : def;
	}

	/**
	 * Skips up to the given number of elements.
	 */
	public Stream<E, T> skip(int count) throws E {
		var receiver = new NextSupplier.Receiver<E, T>();
		while (count-- > 0)
			if (!supplier.next(receiver)) break;
		return this;
	}

	/**
	 * Consumes the next value and returns false if available.
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
		return iterator(supplier);
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.Consumer<? extends E, ? super T> consumer) throws E {
		supplier.forEach(consumer);
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
	public T[] toArray(Functions.IntFunction<T[]> generator) throws E {
		if (generator == null) return null;
		return collect(Collect.array(generator));
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
		return collect(this.supplier, supplier, accumulator, finisher);
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
		return reduce(supplier, accumulator, def);
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
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private <F extends Exception, R> Stream<F, R> update(NextSupplier<F, ? extends R> supplier) {
		if (emptyInstance()) return empty();
		return new Stream<>(supplier);
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

	private static <E extends Exception, F extends Exception, T> NextSupplier<F, T>
		exSupplier(NextSupplier<E, T> supplier, ExceptionAdapter<F> adapter) {
		var receiver = new NextSupplier.Receiver<E, T>();
		return c -> {
			if (!adapter.getBool(() -> supplier.next(receiver))) return false;
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

	private static <E extends Exception, T> T reduce(NextSupplier<E, ? extends T> supplier,
		Excepts.BinFunction<? extends E, ? super T, ? extends T> accumulator, T def) throws E {
		if (accumulator == null) return def;
		var receiver = new NextSupplier.Receiver<E, T>();
		if (!supplier.next(receiver)) return def;
		for (T t = receiver.value;;) {
			if (!supplier.next(receiver)) return t;
			t = accumulator.apply(t, receiver.value);
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
