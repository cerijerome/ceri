package ceri.common.stream;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArray;
import ceri.common.collect.Sets;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
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
	 * Collects stream elements into containers.
	 */
	public interface Collector<A, R> {
		/**
		 * Provides a container.
		 */
		Functions.Supplier<A> supplier();

		/**
		 * Provides an accumulator to add elements to the container.
		 */
		Functions.ObjDoubleConsumer<A> accumulator();

		/**
		 * Provides a finisher to complete the container.
		 */
		Functions.Function<A, R> finisher();
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
		return Reflect.unchecked(EMPTY);
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
		return RawArray.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new DoubleStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Stream values from 0 to 1 in even steps.
	 */
	public static <E extends Exception> DoubleStream<E> segment(int steps) {
		return IntStream.<E>slice(0, steps).mapToDouble(i -> (double) i / (steps - 1));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> DoubleStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		var iterator = iterable.iterator();
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToDouble(Number::doubleValue);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static <E extends Exception> DoubleStream<E> from(PrimitiveIterator.OfDouble iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return ofSupplier(c -> {
			if (!iterator.hasNext()) return false;
			c.accept(iterator.nextDouble());
			return true;
		});
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

	// filtration

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

	// mapping

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

	// manipulation

	/**
	 * Limits the number of elements.
	 */
	public DoubleStream<E> limit(long size) {
		var counter = Counter.of(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.get())) > 0L));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public DoubleStream<E> distinct() {
		return filter(Sets.of()::add);
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public DoubleStream<E> sorted() {
		if (emptyInstance()) return this;
		return update(adaptedSupplier(supplier, s -> sortedSupplier(s)));
	}

	// termination

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
		return Basics.defDouble(next(), def);
	}

	/**
	 * Skips up to the given number of elements.
	 */
	public DoubleStream<E> skip(int count) throws E {
		var receiver = new NextSupplier.Receiver<E>();
		while (count-- > 0)
			if (!supplier.next(receiver)) break;
		return this;
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
	 * Provides a one-off iterator that wraps checked exceptions as runtime.
	 */
	public PrimitiveIterator.OfDouble iterator() {
		return iterator(supplier);
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.DoubleConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	// collection

	/**
	 * Collects elements into an array.
	 */
	public double[] toArray() throws E {
		return collect(Collect.Doubles.array);
	}

	/**
	 * Collects elements with a collector.
	 */
	public <A, R> R collect(Collector<A, R> collector) throws E {
		if (collector == null) return null;
		return collect(collector.supplier(), collector.accumulator(), collector.finisher());
	}

	/**
	 * Collect elements with container supplier and accumulator.
	 */
	public <R> R collect(Functions.Supplier<R> supplier, Functions.ObjDoubleConsumer<R> accumulator)
		throws E {
		return collect(supplier, accumulator, r -> r);
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Functions.Supplier<A> supplier,
		Functions.ObjDoubleConsumer<A> accumulator, Functions.Function<A, R> finisher) throws E {
		return collect(this.supplier, supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Return the min value, or default.
	 */
	public double min(double def) throws E {
		return reduce(Reduce.Doubles.min(), def);
	}

	/**
	 * Return the max value, or default.
	 */
	public double max(double def) throws E {
		return reduce(Reduce.Doubles.max(), def);
	}

	/**
	 * Returns the summation value, allowing overflows, or default.
	 */
	public double sum() throws E {
		return reduce(Reduce.Doubles.sum(), 0.0);
	}

	/**
	 * Returns the average value, or 0.
	 */
	public double average() throws E {
		return collect(Collect.Doubles.average);
	}

	/**
	 * Reduces stream to a value using an accumulator, or null.
	 */
	public Double reduce(Excepts.DoubleBiOperator<? extends E> accumulator) throws E {
		return reduce(supplier, accumulator);
	}

	/**
	 * Reduces stream to a value using an accumulator, or default.
	 */
	public double reduce(Excepts.DoubleBiOperator<? extends E> accumulator, double def) throws E {
		return Basics.defDouble(reduce(accumulator), def);
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
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private DoubleStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(double[] array, int offset,
		int length) {
		var counter = Counter.of(0);
		return c -> {
			if (counter.get() >= length) return false;
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
		var array = collect(supplier, DynamicArray::doubles, DynamicArray.OfDouble::accept, t -> t);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
	}

	private static <E extends Exception, A, R> R collect(NextSupplier<E> next,
		Functions.Supplier<A> supplier, Functions.ObjDoubleConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		if (supplier == null || finisher == null) return null;
		var container = supplier.get();
		if (accumulator != null && container != null)
			next.forEach(d -> accumulator.accept(container, d));
		return finisher.apply(container);
	}

	private static <E extends Exception> Double reduce(NextSupplier<E> supplier,
		Excepts.DoubleBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return null;
		var receiver = new NextSupplier.Receiver<E>();
		if (!supplier.next(receiver)) return null;
		for (double d = receiver.value;;) {
			if (!supplier.next(receiver)) return d;
			d = accumulator.applyAsDouble(d, receiver.value);
		}
	}

	private static <E extends Exception> PrimitiveIterator.OfDouble
		iterator(NextSupplier<E> supplier) {
		var receiver = new NextSupplier.Receiver<E>();
		Excepts.BoolSupplier<E> next = () -> supplier.next(receiver);
		return new PrimitiveIterator.OfDouble() {
			private boolean fetch = true;
			private boolean active = true;

			@Override
			public boolean hasNext() {
				if (fetch) active = ExceptionAdapter.runtime.getBool(next);
				fetch = false;
				return active;
			}

			@Override
			public double nextDouble() {
				if (!hasNext()) throw new NoSuchElementException();
				fetch = true;
				return receiver.value;
			}
		};
	}
}
