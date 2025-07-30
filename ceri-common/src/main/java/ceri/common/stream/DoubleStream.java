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
public class DoubleStream<E extends Exception> {
	private static final Excepts.DoubleConsumer<?> NULL_CONSUMER = _ -> {};
	private static final DoubleStream<RuntimeException> EMPTY = new DoubleStream<>(_ -> false);
	private NextSupplier<E> supplier;

	/**
	 * Reduction operators.
	 */
	public static class Reduce {
		private Reduce() {}
		
		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.DoubleBiOperator<E> min() {
			return (l, r) -> Double.compare(l, r) <= 0 ? l : r;
		}
		
		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.DoubleBiOperator<E> max() {
			return (l, r) -> Double.compare(l, r) >= 0 ? l : r;
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
		class Receiver<E extends Exception> implements Excepts.DoubleConsumer<E> {
			public double value;

			@Override
			public void accept(double value) throws E {
				this.value = value;
			}
		}

		/**
		 * Returns true and calls the consumer with the next element, otherwise returns false.
		 */
		boolean next(Excepts.DoubleConsumer<? extends E> consumer) throws E;

		/**
		 * Iterates over elements.
		 */
		default void forEach(Excepts.DoubleConsumer<? extends E> consumer) throws E {
			while (next(consumer)) {}
		}

		/**
		 * Populates the receiver with the next filtered element, otherwise returns false.
		 */
		default boolean nextFiltered(NextSupplier.Receiver<? extends E> receiver,
			Excepts.DoublePredicate<? extends E> filter) throws E {
			while (true) {
				if (!next(receiver)) return false;
				if (filter.test(receiver.value)) return true;
			}
		}
	}

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> DoubleStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> DoubleStream<E> of(double... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> DoubleStream<E> of(double[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static <E extends Exception> DoubleStream<E> of(double[] values, int offset,
		int length) {
		if (values == null) return empty();
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new DoubleStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> DoubleStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> DoubleStream<E> from(Iterator<? extends Number> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToDouble(Number::doubleValue);
	}

	/**
	 * Creates a stream for the supplier.
	 */
	public static <E extends Exception> DoubleStream<E> ofSupplier(NextSupplier<E> supplier) {
		return new DoubleStream<>(supplier);
	}

	DoubleStream(NextSupplier<E> supplier) {
		this.supplier = supplier;
	}

	// filters

	/**
	 * Only streams elements that match the filter.
	 */
	public DoubleStream<E> filter(Excepts.DoublePredicate<? extends E> filter) {
		if (noOp(filter)) return this;
		return update(filterSupplier(supplier, filter));
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.DoublePredicate<? extends E> predicate) throws E {
		return filter(predicate).supplier.next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.DoublePredicate<? extends E> predicate) throws E {
		if (noOp(predicate)) return true;
		return !anyMatch(i -> !predicate.test(i));
	}

	/**
	 * Returns true if no elements matched.
	 */
	public boolean noneMatch(Excepts.DoublePredicate<? extends E> predicate) throws E {
		return !anyMatch(predicate);
	}

	// mappers

	/**
	 * Maps stream elements to boxed types.
	 */
	public Stream<E, Double> boxed() {
		return mapToObj(Double::valueOf);
	}

	/**
	 * Maps stream elements to new values.
	 */
	public DoubleStream<E> map(Excepts.DoubleOperator<? extends E> mapper) {
		if (noOp(mapper)) return empty();
		return update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to int values.
	 */
	public IntStream<E> mapToInt(Excepts.DoubleToIntFunction<? extends E> mapper) {
		if (noOp(mapper)) return IntStream.empty();
		return IntStream.ofSupplier(intSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to long values.
	 */
	public LongStream<E> mapToLong(Excepts.DoubleToLongFunction<? extends E> mapper) {
		if (noOp(mapper)) return LongStream.empty();
		return LongStream.ofSupplier(longSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to typed values.
	 */
	public <T> Stream<E, T> mapToObj(Excepts.DoubleFunction<? extends E, ? extends T> mapper) {
		if (noOp(mapper)) return Stream.empty();
		return new Stream<>(objSupplier(supplier, mapper));
	}

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public DoubleStream<E>
		flatMap(Excepts.DoubleFunction<? extends E, ? extends DoubleStream<E>> mapper) {
		if (noOp(mapper)) return empty();
		return update(flatSupplier(mapToObj(mapper).filter(Objects::nonNull).supplier()));
	}

	// manipulators

	/**
	 * Limits the number of elements.
	 */
	public DoubleStream<E> limit(long size) {
		var counter = Counter.ofLong(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.count())) > 0L));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public DoubleStream<E> distinct() {
		return filter(new HashSet<Double>()::add);
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public DoubleStream<E> sorted() {
		if (emptyInstance()) return this;
		return update(adaptedSupplier(supplier, s -> sortedSupplier(s)));
	}

	// terminating functions

	/**
	 * Returns the next element or default.
	 */
	public Double next() throws E {
		var receiver = new NextSupplier.Receiver<E>();
		return supplier.next(receiver) ? receiver.value : null;
	}

	/**
	 * Returns the next element or default.
	 */
	public double next(double def) throws E {
		return BasicUtil.defDouble(next(), def);
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

	public void forEach(Excepts.DoubleConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	/**
	 * Collects elements into an array.
	 */
	public double[] toArray() throws E {
		return array(supplier).truncate();
	}

	/**
	 * Collects elements into a supplied container, with accumulator.
	 */
	public <R> R collect(Excepts.Supplier<? extends E, R> supplier,
		Excepts.ObjDoubleConsumer<? extends E, R> accumulator) throws E {
		if (supplier == null) return null;
		var r = supplier.get();
		if (!noOp(accumulator) && r != null) forEach(i -> accumulator.accept(r, i));
		return r;
	}

	// reducers

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public Double reduce(Excepts.DoubleBiOperator<? extends E> accumulator) throws E {
		if (noOp(accumulator)) return null;
		var next = next();
		return next == null ? null : reduce(next, accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public double reduce(double identity, Excepts.DoubleBiOperator<? extends E> accumulator)
		throws E {
		if (noOp(accumulator)) return identity;
		var receiver = new NextSupplier.Receiver<E>();
		for (double d = identity;;) {
			if (!supplier.next(receiver)) return d;
			d = accumulator.applyAsDouble(d, receiver.value);
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

	private Excepts.DoubleConsumer<E> nullConsumer() {
		return BasicUtil.unchecked(NULL_CONSUMER);
	}

	private DoubleStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(double[] array, int offset,
		int length) {
		var counter = Counter.ofInt(0);
		return c -> {
			if (counter.count() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> filterSupplier(NextSupplier<E> supplier,
		Excepts.DoublePredicate<? extends E> filter) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.nextFiltered(receiver, filter)) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> mapSupplier(NextSupplier<E> supplier,
		Excepts.DoubleOperator<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsDouble(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> IntStream.NextSupplier<E>
		intSupplier(NextSupplier<E> supplier, Excepts.DoubleToIntFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsInt(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> LongStream.NextSupplier<E>
		longSupplier(NextSupplier<E> supplier, Excepts.DoubleToLongFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsLong(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, R> Stream.NextSupplier<E, R> objSupplier(
		NextSupplier<E> supplier, Excepts.DoubleFunction<? extends E, ? extends R> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, ? extends DoubleStream<E>> supplier) {
		var streamReceiver =
			new ceri.common.stream.Stream.NextSupplier.Receiver<E, DoubleStream<E>>();
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

	private static <E extends Exception> DynamicArray.OfDouble
		array(NextSupplier<? extends E> supplier) throws E {
		var array = DynamicArray.doubles();
		supplier.forEach(Functions.DoubleConsumer.except(array));
		return array;
	}
}
