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
 * A simple stream that allows checked exceptions. Where possible, modifiers change the current
 * stream rather than create a new instance. Not thread-safe.
 */
public class LongStream<E extends Exception> {
	private static final Excepts.LongConsumer<?> NULL_CONSUMER = _ -> {};
	private static final LongStream<RuntimeException> EMPTY = new LongStream<>(_ -> false);
	private NextSupplier<E> supplier;

	/**
	 * Reduction operators.
	 */
	public static class Reduce {
		private Reduce() {}
		
		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> min() {
			return (l, r) -> Long.compare(l, r) <= 0 ? l : r;
		}
		
		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> max() {
			return (l, r) -> Long.compare(l, r) >= 0 ? l : r;
		}
		
		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> and() {
			return (l, r) -> l & r;
		}
		
		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> or() {
			return (l, r) -> l | r;
		}
		
		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> xor() {
			return (l, r) -> l ^ r;
		}
	}
	
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
		if (values == null) return empty();
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new LongStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Streams a range of values.
	 */
	public static <E extends Exception> LongStream<E> range(long offset, long length) {
		var counter = Counter.ofLong(0L);
		return ofSupplier(c -> {
			if (counter.count() >= length) return false;
			c.accept(offset + counter.preInc(1));
			return true;
		});
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

	/**
	 * Creates a stream for the supplier.
	 */
	public static <E extends Exception> LongStream<E> ofSupplier(NextSupplier<E> supplier) {
		return new LongStream<>(supplier);
	}

	LongStream(NextSupplier<E> supplier) {
		this.supplier = supplier;
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
	 * Maps stream elements to ints.
	 */
	public IntStream<E> ints() {
		if (emptyInstance()) return IntStream.empty();
		var supplier = this.supplier;
		return IntStream.ofSupplier(c -> supplier.next(l -> c.accept((int) l)));
	}

	/**
	 * Maps stream elements to new values.
	 */
	public LongStream<E> map(Excepts.LongOperator<? extends E> mapper) {
		if (noOp(mapper)) return empty();
		return update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to int values.
	 */
	public IntStream<E> mapToInt(Excepts.LongToIntFunction<? extends E> mapper) {
		if (noOp(mapper)) return IntStream.empty();
		return IntStream.ofSupplier(intSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to double values.
	 */
	public DoubleStream<E> mapToDouble(Excepts.LongToDoubleFunction<? extends E> mapper) {
		if (noOp(mapper)) return DoubleStream.empty();
		return DoubleStream.ofSupplier(doubleSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to typed values.
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
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.count())) > 0L));
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

	private static <E extends Exception> IntStream.NextSupplier<E>
		intSupplier(NextSupplier<E> supplier, Excepts.LongToIntFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsInt(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> DoubleStream.NextSupplier<E>
		doubleSupplier(NextSupplier<E> supplier, Excepts.LongToDoubleFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsDouble(receiver.value));
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
