package ceri.common.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import ceri.common.collection.CollectionSupplier;
import ceri.common.function.Excepts.BiFunction;
import ceri.common.function.Excepts.BinFunction;
import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.ObjIntConsumer;
import ceri.common.function.Excepts.Predicate;
import ceri.common.function.Excepts.Supplier;
import ceri.common.function.Excepts.ToDoubleFunction;
import ceri.common.function.Excepts.ToIntFunction;
import ceri.common.function.Excepts.ToLongFunction;
import ceri.common.function.Predicates;
import ceri.common.util.BasicUtil;

/**
 * A simple stream that allows checked exceptions. Modifiers change the current stream rather than
 * create a new instance. Not thread-safe.
 */
public class Stream<E extends Exception, T> {
	private static final Object END = new Object(); // marks the end of supplied values
	private static final Stream<RuntimeException, Object> EMPTY = new Stream<>(() -> END);
	private static final CollectionSupplier collections = CollectionSupplier.of();
	private Supplier<E, Object> supplier;

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

	private Stream(Supplier<E, Object> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Only streams elements that match the filter.
	 */
	public Stream<E, T> filter(Predicate<? extends E, ? super T> filter) {
		Objects.requireNonNull(filter);
		return update(filterSupplier(supplier, filter));
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <R> Stream<E, R> map(Function<? extends E, ? super T, ? extends R> mapper) {
		Objects.requireNonNull(mapper);
		return BasicUtil.unchecked(update(mapperSupplier(supplier, mapper)));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public IntStream<E> mapToInt(ToIntFunction<? extends E, ? super T> mapper) {
		Objects.requireNonNull(mapper);
		return IntStream.of(map(mapper::applyAsInt));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public LongStream<E> mapToLong(ToLongFunction<? extends E, ? super T> mapper) {
		Objects.requireNonNull(mapper);
		return LongStream.of(map(mapper::applyAsLong));
	}

	/**
	 * Maps stream elements to a new stream.
	 */
	public DoubleStream<E> mapToDouble(ToDoubleFunction<? extends E, ? super T> mapper) {
		Objects.requireNonNull(mapper);
		return DoubleStream.of(map(mapper::applyAsDouble));
	}

	/**
	 * Limits the number of elements.
	 */
	public Stream<E, T> limit(long size) {
		return update(limitSupplier(supplier, size));
	}

	/**
	 * Streams distinct elements, by first collecting into a linked set.
	 */
	public Stream<E, T> distinct() throws E {
		if (emptyInstance()) return this;
		return from(collect(new LinkedHashSet<>()));
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public Stream<E, T> sorted(Comparator<? super T> comparator) throws E {
		if (emptyInstance()) return this;
		return from(sortedList(comparator));
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(Consumer<? extends E, ? super T> action) throws E {
		Objects.requireNonNull(action);
		while (true) {
			var obj = supplier.get();
			if (obj == END) break;
			action.accept(BasicUtil.unchecked(obj));
		}
	}

	/**
	 * Iterates elements and indexes with a consumer.
	 */
	public void forEachIndex(ObjIntConsumer<? extends E, ? super T> action) throws E {
		Objects.requireNonNull(action);
		int i = 0;
		while (true) {
			var obj = supplier.get();
			if (obj == END) break;
			action.accept(BasicUtil.unchecked(obj), i++);
		}
	}

	/**
	 * Collects elements into an array.
	 */
	public T[] toArray(IntFunction<T[]> generator) throws E {
		Objects.requireNonNull(generator);
		return collect(new ArrayList<>()).toArray(generator);
	}

	/**
	 * Collects elements.
	 */
	public Set<T> toSet() throws E {
		if (emptyInstance()) return Set.of();
		return Collections.unmodifiableSet(collect(collections.<T>set().get()));
	}

	/**
	 * Collects elements.
	 */
	public List<T> toList() throws E {
		if (emptyInstance()) return List.of();
		return Collections.unmodifiableList(collect(collections.<T>list().get()));
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
	public <C extends Collection<T>> C collect(C collection) throws E {
		if (collection != null) forEach(collection::add);
		return collection;
	}

	/**
	 * Collects elements.
	 */
	public <K> Map<K, T> toMap(Function<? extends E, ? super T, ? extends K> keyFn) throws E {
		if (emptyInstance()) return Map.of();
		return Collections.unmodifiableMap(collectMap(keyFn, collections.<K, T>map().get()));
	}

	/**
	 * Collects elements.
	 */
	public <K, M extends Map<K, T>> M
		collectMap(Function<? extends E, ? super T, ? extends K> keyFn, M map) throws E {
		Objects.requireNonNull(keyFn);
		if (map != null) forEach(t -> map.put(keyFn.apply(t), t));
		return map;
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (supplier.get() == END) return n;
	}

	/**
	 * Returns the maximum value or null.
	 */
	public T min(Comparator<? super T> comparator) throws E {
		return reduce((t, u) -> comparator.compare(t, u) <= 0 ? t : u);
	}

	/**
	 * Returns the minimum value or null.
	 */
	public T max(Comparator<? super T> comparator) throws E {
		return reduce((t, u) -> comparator.compare(t, u) >= 0 ? t : u);
	}

	/**
	 * Reduces stream to an element or null.
	 */
	public T reduce(BinFunction<? extends E, ? super T, ? extends T> accumulator) throws E {
		if (accumulator == null) return null;
		return this.reduce(null, (t, u) -> {
			if (t == null) return u;
			if (u == null) return t;
			return accumulator.apply(t, u);
		});
	}

	/**
	 * Reduces stream to an element or null.
	 */
	public <U> U reduce(U identity,
		BiFunction<? extends E, ? super U, ? super T, ? extends U> accumulator) throws E {
		if (accumulator == null) return identity;
		for (U u = identity;;) {
			var obj = supplier.get();
			if (obj == END) return u;
			if (obj == null) continue;
			var result = accumulator.apply(u, t(obj));
			if (result != null) u = result;
		}
	}

	/**
	 * Returns true if any element matches.
	 */
	public boolean anyMatch(Predicate<? extends E, ? super T> predicate) throws E {
		Objects.requireNonNull(predicate);
		return filter(predicate).supplier.get() != END;
	}

	/**
	 * Returns true if all elements match.
	 */
	public boolean allMatch(Predicate<? extends E, ? super T> predicate) throws E {
		Objects.requireNonNull(predicate);
		while (true) {
			var obj = supplier.get();
			if (obj == END) return true;
			if (!predicate.test(t(obj))) return false;
		}
	}

	/**
	 * Returns true if no elements match.
	 */
	public boolean noneMatch(Predicate<? extends E, ? super T> predicate) throws E {
		Objects.requireNonNull(predicate);
		return allMatch(Predicates.not(predicate));
	}

	/**
	 * Returns the next element or null.
	 */
	public T next() throws E {
		return next(null);
	}

	/**
	 * Returns the next element or default.
	 */
	public T next(T def) throws E {
		var obj = supplier.get();
		return obj == END ? def : t(obj);
	}

	/**
	 * Returns true if this is the empty instance.
	 */
	boolean emptyInstance() {
		return this == EMPTY;
	}

	private T t(Object obj) {
		return BasicUtil.unchecked(obj);
	}

	private Stream<E, T> update(Supplier<E, Object> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private List<T> sortedList(Comparator<? super T> comparator) throws E {
		var list = collect(CollectionSupplier.of().<T>list().get());
		Collections.sort(list, comparator);
		return list;
	}

	private static <E extends Exception, T, R> Supplier<E, Object> mapperSupplier(
		Supplier<? extends E, Object> supplier,
		Function<? extends E, ? super T, ? extends R> mapper) {
		return () -> {
			var obj = supplier.get();
			return obj == END ? END : mapper.apply(BasicUtil.unchecked(obj));
		};
	}

	private static <E extends Exception, T> Supplier<E, Object> filterSupplier(
		Supplier<? extends E, Object> supplier, Predicate<? extends E, ? super T> filter) {
		return () -> {
			while (true) {
				var obj = supplier.get();
				if (obj == END || filter.test(BasicUtil.unchecked(obj))) return obj;
			}
		};
	}

	private static <E extends Exception> Supplier<E, Object>
		limitSupplier(Supplier<? extends E, Object> supplier, long size) {
		return new Supplier<>() {
			private long i = 0;

			public Object get() throws E {
				if (i >= size) return END;
				var obj = supplier.get();
				if (obj != END) i++;
				return obj;
			}
		};
	}

	private static <E extends Exception, T> Supplier<E, Object> arraySupplier(T[] values) {
		return new Supplier<>() {
			private int i = 0;

			public Object get() {
				if (i >= values.length) return END;
				return values[i++];
			}
		};
	}

	private static <E extends Exception, T> Supplier<E, Object>
		iteratorSupplier(Iterator<T> iterator) {
		return () -> iterator.hasNext() ? iterator.next() : END;
	}
}
