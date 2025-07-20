package ceri.common.stream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArrays;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Modifiers change the current stream rather than
 * create a new instance. Not thread-safe.
 */
public class IntStream<E extends Exception> {
	private static final Excepts.IntConsumer<RuntimeException> NULL_CONSUMER = _ -> {};
	private static final IntStream<RuntimeException> EMPTY = new IntStream<>(_ -> false);
	private NextSupplier<E> supplier;

	/**
	 * Iterating functional interface
	 */
	@FunctionalInterface
	interface NextSupplier<E extends Exception> {
		/**
		 * Returns true and calls the consumer with the next element, otherwise returns false.
		 */
		boolean next(Excepts.IntConsumer<? extends E> consumer) throws E;

		/**
		 * Iterates over elements.
		 */
		default void forEach(Excepts.IntConsumer<? extends E> consumer) throws E {
			while (next(consumer)) {}
		}

		/**
		 * Populates the receiver with the next filtered element, otherwise returns false.
		 */
		default boolean nextFiltered(Receiver<? extends E> receiver,
			Excepts.IntPredicate<? extends E> filter) throws E {
			while (true) {
				if (!next(receiver)) return false;
				if (filter.test(receiver.value)) return true;
			}
		}
	}

	/**
	 * Temporary element receiver.
	 */
	static class Receiver<E extends Exception> implements Excepts.IntConsumer<E> {
		public int value;

		@Override
		public void accept(int value) throws E {
			this.value = value;
		}
	}

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> IntStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> IntStream<E> of(int... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> IntStream<E> of(int[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> IntStream<E> of(int[] values, int offset, int length) {
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new IntStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> IntStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> IntStream<E> from(Iterator<? extends Number> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToInt(Number::intValue);
	}

	IntStream(NextSupplier<E> supplier) {
		this.supplier = supplier;
	}

	// filters

	/**
	 * Only streams elements that match the filter.
	 */
	public IntStream<E> filter(Excepts.IntPredicate<? extends E> filter) {
		if (noOp(filter)) return this;
		return update(filterSupplier(supplier, filter));
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		return filter(predicate).supplier.next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		if (noOp(predicate)) return true;
		return !anyMatch(i -> !predicate.test(i));
	}

	/**
	 * Returns true if no elements matched.
	 */
	public boolean noneMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		return !anyMatch(predicate);
	}

	// mappers

	/**
	 * Maps stream elements to a new type.
	 */
	public Stream<E, Integer> boxed() {
		return mapToObj(Integer::valueOf);
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public IntStream<E> map(Excepts.IntOperator<? extends E> mapper) {
		if (noOp(mapper)) return empty();
		return update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(Excepts.IntFunction<? extends E, ? extends T> mapper) {
		if (noOp(mapper)) return Stream.empty();
		return new Stream<>(objSupplier(supplier, mapper));
	}

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public IntStream<E> flatMap(Excepts.IntFunction<? extends E, ? extends IntStream<E>> mapper) {
		if (noOp(mapper)) return empty();
		return update(flatSupplier(objSupplier(supplier, mapper)));
	}

	// manipulators

	/**
	 * Limits the number of elements.
	 */
	public IntStream<E> limit(long size) {
		if (emptyInstance()) return this;
		return update(limitSupplier(supplier, size));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public IntStream<E> distinct() {
		if (emptyInstance()) return this;
		return update(distinctSupplier(supplier));
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public IntStream<E> sorted() {
		if (emptyInstance()) return this;
		return update(adaptedSupplier(supplier, s -> sortedSupplier(s)));
	}

	// terminating functions

	/**
	 * Returns the next element or default.
	 */
	public Integer next() throws E {
		var receiver = new Receiver<E>();
		return supplier.next(receiver) ? receiver.value : null;
	}

	/**
	 * Returns the next element or default.
	 */
	public int next(int def) throws E {
		return BasicUtil.defInt(next(), def);
	}

	/**
	 * Returns the minimum value or default.
	 */
	public Integer min() throws E {
		return reduce((t, u) -> Integer.compare(t, u) <= 0 ? t : u);
	}

	/**
	 * Returns the minimum value or default.
	 */
	public int min(int def) throws E {
		return BasicUtil.defInt(min(), def);
	}

	/**
	 * Returns the maximum value or default.
	 */
	public Integer max() throws E {
		return reduce((t, u) -> Integer.compare(t, u) >= 0 ? t : u);
	}

	/**
	 * Returns the maximum value or default.
	 */
	public int max(int def) throws E {
		return BasicUtil.defInt(max(), def);
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (!supplier.next(nullConsumer())) return n;
	}

	// collectors

	public void forEach(Excepts.IntConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	/**
	 * Collects elements into an array.
	 */
	public int[] toArray() throws E {
		return array(supplier).truncate();
	}

	/**
	 * Collects elements into a supplied container, with accumulator.
	 */
	public <R> R collect(Excepts.Supplier<? extends E, R> supplier,
		Excepts.ObjIntConsumer<? extends E, R> accumulator) throws E {
		if (supplier == null) return null;
		var r = supplier.get();
		if (!noOp(accumulator) && r != null) forEach(i -> accumulator.accept(r, i));
		return r;
	}

	// reducers

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public Integer reduce(Excepts.IntBiOperator<? extends E> accumulator) throws E {
		if (noOp(accumulator)) return null;
		var next = next();
		return next == null ? null : reduce(next, accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public int reduce(int identity, Excepts.IntBiOperator<? extends E> accumulator) throws E {
		if (noOp(accumulator)) return identity;
		var receiver = new Receiver<E>();
		for (int i = identity;;) {
			if (!supplier.next(receiver)) return i;
			i = accumulator.applyAsInt(i, receiver.value);
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

	private Excepts.IntConsumer<E> nullConsumer() {
		return BasicUtil.unchecked(NULL_CONSUMER);
	}

	private IntStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(int[] array, int offset,
		int length) {
		var counter = Counter.ofInt(0);
		return c -> {
			if (counter.count() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> filterSupplier(NextSupplier<E> supplier,
		Excepts.IntPredicate<? extends E> filter) {
		var receiver = new Receiver<E>();
		return c -> {
			if (!supplier.nextFiltered(receiver, filter)) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> mapSupplier(NextSupplier<E> supplier,
		Excepts.IntOperator<? extends E> mapper) {
		var receiver = new Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsInt(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, R> Stream.NextSupplier<E, R> objSupplier(
		NextSupplier<E> supplier, Excepts.IntFunction<? extends E, ? extends R> mapper) {
		var receiver = new Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, IntStream<E>> supplier) {
		var streamReceiver = new Stream.Receiver<E, IntStream<E>>();
		var receiver = new Receiver<E>();
		return c -> {
			while (true) {
				if (streamReceiver.value == null
					&& !supplier.nextFiltered(streamReceiver, Objects::nonNull)) return false;
				if (streamReceiver.value.supplier.next(receiver)) {
					c.accept(receiver.value);
					return true;
				}
				streamReceiver.value = null;
			}
		};
	}

	private static <E extends Exception> NextSupplier<E> limitSupplier(NextSupplier<E> supplier,
		long limit) {
		var counter = Counter.ofLong(0L);
		return c -> {
			if (counter.count() >= limit || !supplier.next(c)) return false;
			counter.inc(1L);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		distinctSupplier(NextSupplier<E> supplier) {
		var set = new HashSet<Integer>();
		var receiver = new Receiver<E>();
		return c -> {
			while (true) {
				if (!supplier.next(receiver)) return false;
				if (!set.add(receiver.value)) continue;
				c.accept(receiver.value);
				return true;
			}
		};
	}

	private static <E extends Exception> NextSupplier<E> adaptedSupplier(NextSupplier<E> supplier,
		Excepts.Operator<E, NextSupplier<E>> adapter) {
		var receiver = new Stream.Receiver<E, NextSupplier<E>>();
		return c -> {
			if (receiver.value == null) receiver.value = adapter.apply(supplier);
			return receiver.value.next(c);
		};
	}

	private static <E extends Exception> NextSupplier<E> sortedSupplier(NextSupplier<E> supplier)
		throws E {
		var array = array(supplier);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
	}

	private static <E extends Exception> DynamicArray.OfInt
		array(NextSupplier<? extends E> supplier) throws E {
		var array = DynamicArray.ints();
		supplier.forEach(Functions.IntConsumer.except(array));
		return array;
	}
}
