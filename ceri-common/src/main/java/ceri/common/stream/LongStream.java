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
public class LongStream<E extends Exception> {
	private static final Excepts.LongConsumer<?> NULL_CONSUMER = _ -> {};
	private static final LongStream<RuntimeException> EMPTY = new LongStream<>(_ -> false);
	private NextSupplier<E> supplier;

	/**
	 * Iterating functional interface
	 */
	@FunctionalInterface
	interface NextSupplier<E extends Exception> {
		/**
		 * Temporary element receiver.
		 */
		class Receiver<E extends Exception> implements Excepts.LongConsumer<E> {
			public long value;

			@Override
			public void accept(long value) throws E {
				this.value = value;
			}
		}

		/**
		 * Returns true and calls the consumer with the next element, otherwise returns false.
		 */
		boolean next(Excepts.LongConsumer<? extends E> consumer) throws E;

		/**
		 * Iterates over elements.
		 */
		default void forEach(Excepts.LongConsumer<? extends E> consumer) throws E {
			while (next(consumer)) {}
		}

		/**
		 * Populates the receiver with the next filtered element, otherwise returns false.
		 */
		default boolean nextFiltered(NextSupplier.Receiver<? extends E> receiver,
			Excepts.LongPredicate<? extends E> filter) throws E {
			while (true) {
				if (!next(receiver)) return false;
				if (filter.test(receiver.value)) return true;
			}
		}
	}

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> LongStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> LongStream<E> of(long... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> LongStream<E> of(long[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> LongStream<E> of(long[] values, int offset, int length) {
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new LongStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> LongStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> LongStream<E> from(Iterator<? extends Number> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToLong(Number::longValue);
	}

	LongStream(NextSupplier<E> supplier) {
		this.supplier = supplier;
	}

	NextSupplier<E> supplier() {
		return supplier;
	}

	// filters

	/**
	 * Only streams elements that match the filter.
	 */
	public LongStream<E> filter(Excepts.LongPredicate<? extends E> filter) {
		if (noOp(filter)) return this;
		return update(filterSupplier(supplier, filter));
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.LongPredicate<? extends E> predicate) throws E {
		return filter(predicate).supplier.next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.LongPredicate<? extends E> predicate) throws E {
		if (noOp(predicate)) return true;
		return !anyMatch(i -> !predicate.test(i));
	}

	/**
	 * Returns true if no elements matched.
	 */
	public boolean noneMatch(Excepts.LongPredicate<? extends E> predicate) throws E {
		return !anyMatch(predicate);
	}

	// mappers

	/**
	 * Maps stream elements to boxed types.
	 */
	public Stream<E, Long> boxed() {
		return mapToObj(Long::valueOf);
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public LongStream<E> map(Excepts.LongOperator<? extends E> mapper) {
		if (noOp(mapper)) return empty();
		return update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(Excepts.LongFunction<? extends E, ? extends T> mapper) {
		if (noOp(mapper)) return Stream.empty();
		return new Stream<>(objSupplier(supplier, mapper));
	}

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public LongStream<E>
		flatMap(Excepts.LongFunction<? extends E, ? extends LongStream<E>> mapper) {
		if (noOp(mapper)) return empty();
		return update(flatSupplier(mapToObj(mapper).filter(Objects::nonNull).supplier()));
	}

	// manipulators

	/**
	 * Limits the number of elements.
	 */
	public LongStream<E> limit(long size) {
		var counter = Counter.ofLong(size);
		return update(preSupplier(supplier, () -> counter.count() > 0 && counter.inc(-1) >= 0));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public LongStream<E> distinct() {
		return filter(new HashSet<Long>()::add);
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public LongStream<E> sorted() {
		if (emptyInstance()) return this;
		return update(adaptedSupplier(supplier, s -> sortedSupplier(s)));
	}

	// terminating functions

	/**
	 * Returns the next element or default.
	 */
	public Long next() throws E {
		var receiver = new NextSupplier.Receiver<E>();
		return supplier.next(receiver) ? receiver.value : null;
	}

	/**
	 * Returns the next element or default.
	 */
	public long next(long def) throws E {
		return BasicUtil.defLong(next(), def);
	}

	/**
	 * Returns the minimum value or default.
	 */
	public Long min() throws E {
		return reduce((t, u) -> Long.compare(t, u) <= 0 ? t : u);
	}

	/**
	 * Returns the minimum value or default.
	 */
	public long min(int def) throws E {
		return BasicUtil.defLong(min(), def);
	}

	/**
	 * Returns the maximum value or default.
	 */
	public Long max() throws E {
		return reduce((t, u) -> Long.compare(t, u) >= 0 ? t : u);
	}

	/**
	 * Returns the maximum value or default.
	 */
	public long max(long def) throws E {
		return BasicUtil.defLong(max(), def);
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		for (long n = 0L;; n++)
			if (!supplier.next(nullConsumer())) return n;
	}

	// collectors

	public void forEach(Excepts.LongConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	/**
	 * Collects elements into an array.
	 */
	public long[] toArray() throws E {
		return array(supplier).truncate();
	}

	/**
	 * Collects elements into a supplied container, with accumulator.
	 */
	public <R> R collect(Excepts.Supplier<? extends E, R> supplier,
		Excepts.ObjLongConsumer<? extends E, R> accumulator) throws E {
		if (supplier == null) return null;
		var r = supplier.get();
		if (!noOp(accumulator) && r != null) forEach(i -> accumulator.accept(r, i));
		return r;
	}

	// reducers

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public Long reduce(Excepts.LongBiOperator<? extends E> accumulator) throws E {
		if (noOp(accumulator)) return null;
		var next = next();
		return next == null ? null : reduce(next, accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public long reduce(long identity, Excepts.LongBiOperator<? extends E> accumulator) throws E {
		if (noOp(accumulator)) return identity;
		var receiver = new NextSupplier.Receiver<E>();
		for (long l = identity;;) {
			if (!supplier.next(receiver)) return l;
			l = accumulator.applyAsLong(l, receiver.value);
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

	private Excepts.LongConsumer<E> nullConsumer() {
		return BasicUtil.unchecked(NULL_CONSUMER);
	}

	private LongStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(long[] array, int offset,
		int length) {
		var counter = Counter.ofInt(0);
		return c -> {
			if (counter.count() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> filterSupplier(NextSupplier<E> supplier,
		Excepts.LongPredicate<? extends E> filter) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.nextFiltered(receiver, filter)) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> mapSupplier(NextSupplier<E> supplier,
		Excepts.LongOperator<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsLong(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, R> Stream.NextSupplier<E, R> objSupplier(
		NextSupplier<E> supplier, Excepts.LongFunction<? extends E, ? extends R> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, ? extends LongStream<E>> supplier) {
		var streamReceiver =
			new ceri.common.stream.Stream.NextSupplier.Receiver<E, LongStream<E>>();
		var receiver = new NextSupplier.Receiver<E>();
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

	private static <E extends Exception> NextSupplier<E> preSupplier(NextSupplier<E> supplier,
		Excepts.BoolSupplier<? extends E> pre) {
		return c -> {
			if (!pre.getAsBool()) return false;
			return supplier.next(c);
		};
	}

	private static <E extends Exception> NextSupplier<E> adaptedSupplier(NextSupplier<E> supplier,
		Excepts.Operator<E, NextSupplier<E>> adapter) {
		var receiver = new ceri.common.stream.Stream.NextSupplier.Receiver<E, NextSupplier<E>>();
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

	private static <E extends Exception> DynamicArray.OfLong
		array(NextSupplier<? extends E> supplier) throws E {
		var array = DynamicArray.longs();
		supplier.forEach(Functions.LongConsumer.except(array));
		return array;
	}
}
