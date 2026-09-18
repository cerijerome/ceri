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
 * stream rather than create a new instance. Not thread-safe. Implementation note: the supplier is
 * replaced on change, so the adapter functions must pass in the current supplier.
 */
public abstract class DoubleStream<E extends Exception> {
	private static final Excepts.DoubleConsumer<?> NULL_CONSUMER = _ -> {};
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
		Functions.ObjDoubleConsumer<A> doubleAccumulator();

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
	 * An implementation that only allows runtime exceptions.
	 */
	public static class Rt extends DoubleStream<RuntimeException> {
		public static final Rt EMPTY = new Rt(_ -> false);

		/**
		 * Creates a stream for the supplier.
		 */
		public static Rt ofSupplier(NextSupplier<? extends RuntimeException> supplier) {
			return new Rt(supplier);
		}

		Rt(NextSupplier<? extends RuntimeException> supplier) {
			super(supplier);
		}

		@Override
		public Rt runtime() {
			return this;
		}

		/**
		 * Adapts the stream to allow the exception type.
		 */
		public <E extends Exception> Ex<E> ex() {
			return super.emptyInstance() ? Ex.empty() : new Ex<>(Reflect.unchecked(supplier()));
		}

		@Override
		public <E extends Exception> Ex<E> ex(ExceptionAdapter<E> adapter) {
			return ex();
		}

		@Override
		public Rt filter(Excepts.DoublePredicate<? extends RuntimeException> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter, allowing exceptions.
		 */
		public <E extends Exception> Ex<E> filterEx(Excepts.DoublePredicate<? extends E> filter) {
			return this.<E>ex().filter(filter);
		}

		@Override
		public Stream.Rt<Double> boxed() {
			return mapToObj(Double::valueOf);
		}

		@Override
		public Rt map(Excepts.DoubleOperator<? extends RuntimeException> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to new values, allowing exceptions.
		 */
		public <E extends Exception> Ex<E> mapEx(Excepts.DoubleOperator<? extends E> mapper) {
			return this.<E>ex().map(mapper);
		}

		@Override
		public IntStream.Rt
			mapToInt(Excepts.DoubleToIntFunction<? extends RuntimeException> mapper) {
			return super.noOp(mapper) ? IntStream.Rt.EMPTY :
				IntStream.Rt.ofSupplier(super.intSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to int values, allowing exceptions.
		 */
		public <E extends Exception> IntStream.Ex<E>
			mapToIntEx(Excepts.DoubleToIntFunction<? extends E> mapper) {
			return this.<E>ex().mapToInt(mapper);
		}

		@Override
		public LongStream.Rt
			mapToLong(Excepts.DoubleToLongFunction<? extends RuntimeException> mapper) {
			return super.noOp(mapper) ? LongStream.Rt.EMPTY :
				LongStream.Rt.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to long values, allowing exceptions.
		 */
		public <E extends Exception> LongStream.Ex<E>
			mapToLongEx(Excepts.DoubleToLongFunction<? extends E> mapper) {
			return this.<E>ex().mapToLong(mapper);
		}

		@Override
		public <T> Stream.Rt<T>
			mapToObj(Excepts.DoubleFunction<? extends RuntimeException, ? extends T> mapper) {
			return super.noOp(mapper) ? Stream.Rt.empty() :
				Stream.Rt.ofSupplier(super.objSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to typed values, allowing exceptions.
		 */
		public <E extends Exception, T> Stream.Ex<E, T>
			mapToObjEx(Excepts.DoubleFunction<? extends E, ? extends T> mapper) {
			return this.<E>ex().mapToObj(mapper);
		}

		@Override
		public Rt flatMap(Excepts.DoubleFunction<? extends RuntimeException, //
			? extends DoubleStream<RuntimeException>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams, allowing exceptions.
		 */
		public <E extends Exception> Ex<E>
			flatMapEx(Excepts.DoubleFunction<? extends E, ? extends DoubleStream<E>> mapper) {
			return this.<E>ex().flatMap(mapper);
		}

		@Override
		public Rt limit(long size) {
			return cast(super.limit(size));
		}

		@Override
		public Rt distinct() {
			return cast(super.distinct());
		}

		@Override
		public Rt sorted() {
			return cast(super.sorted());
		}

		@Override
		public Rt skip(int count) {
			return cast(super.skip(count));
		}

		@Override
		Rt emptyVal() {
			return EMPTY;
		}
	}

	/**
	 * An implementation that allows a checked exception type.
	 */
	public static class Ex<E extends Exception> extends DoubleStream<E> {
		private static final Ex<?> EMPTY = new Ex<>(_ -> false);

		/**
		 * Returns an empty stream.
		 */
		public static <E extends Exception> Ex<E> empty() {
			return Reflect.unchecked(EMPTY);
		}

		/**
		 * Creates a stream for the supplier.
		 */
		public static <E extends Exception> Ex<E> ofSupplier(NextSupplier<? extends E> supplier) {
			return new Ex<>(supplier);
		}

		Ex(NextSupplier<? extends E> supplier) {
			super(supplier);
		}

		@Override
		public Ex<E> filter(Excepts.DoublePredicate<? extends E> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter.
		 */
		public Ex<E> filterRt(Excepts.DoublePredicate<? extends RuntimeException> filter) {
			return filter(Reflect.unchecked(filter));
		}

		@Override
		public Stream.Ex<E, Double> boxed() {
			return mapToObj(Double::valueOf);
		}

		@Override
		public Ex<E> map(Excepts.DoubleOperator<? extends E> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to new values.
		 */
		public Ex<E> mapRt(Excepts.DoubleOperator<? extends RuntimeException> mapper) {
			return map(Reflect.unchecked(mapper));
		}

		@Override
		public IntStream.Ex<E> mapToInt(Excepts.DoubleToIntFunction<? extends E> mapper) {
			return super.noOp(mapper) ? IntStream.Ex.empty() :
				IntStream.Ex.ofSupplier(super.intSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to int values, allowing exceptions.
		 */
		public IntStream.Ex<E>
			mapToIntRt(Excepts.DoubleToIntFunction<? extends RuntimeException> mapper) {
			return mapToInt(Reflect.unchecked(mapper));
		}

		@Override
		public LongStream.Ex<E> mapToLong(Excepts.DoubleToLongFunction<? extends E> mapper) {
			return super.noOp(mapper) ? LongStream.Ex.empty() :
				LongStream.Ex.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to long values, allowing exceptions.
		 */
		public LongStream.Ex<E>
			mapToLongRt(Excepts.DoubleToLongFunction<? extends RuntimeException> mapper) {
			return mapToLong(Reflect.unchecked(mapper));
		}

		@Override
		public <T> Stream.Ex<E, T>
			mapToObj(Excepts.DoubleFunction<? extends E, ? extends T> mapper) {
			return super.noOp(mapper) ? Stream.Ex.empty() :
				Stream.Ex.ofSupplier(super.objSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to typed values.
		 */
		public <T> Stream.Ex<E, T>
			mapToObjRt(Excepts.DoubleFunction<? extends RuntimeException, ? extends T> mapper) {
			return mapToObj(Reflect.unchecked(mapper));
		}

		@Override
		public Ex<E>
			flatMap(Excepts.DoubleFunction<? extends E, ? extends DoubleStream<E>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams.
		 */
		public Ex<E> flatMapRt(Excepts.DoubleFunction<? extends RuntimeException, //
			? extends DoubleStream<? extends RuntimeException>> mapper) {
			return flatMap(Reflect.unchecked(mapper));
		}

		@Override
		public Ex<E> limit(long size) {
			return cast(super.limit(size));
		}

		@Override
		public Ex<E> distinct() {
			return cast(super.distinct());
		}

		@Override
		public Ex<E> sorted() {
			return cast(super.sorted());
		}

		@Override
		public Ex<E> skip(int count) throws E {
			return cast(super.skip(count));
		}

		@Override
		Ex<E> emptyVal() {
			return empty();
		}
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(double... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(double[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(double[] values, int offset, int length) {
		if (values == null) return Rt.EMPTY;
		return RawArray.applySlice(values, offset, length,
			(o, l) -> l == 0 ? Rt.EMPTY : Rt.ofSupplier(arraySupplier(values, o, l)));
	}

	/**
	 * Stream values from 0 to 1 in even steps.
	 */
	public static DoubleStream.Rt segment(int steps) {
		return IntStream.slice(0, steps).mapToDouble(i -> i / Math.max(1.0, steps - 1));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static Rt from(Iterable<? extends Number> iterable) {
		if (iterable == null) return Rt.EMPTY;
		var iterator = iterable.iterator();
		if (iterator == null || !iterator.hasNext()) return Rt.EMPTY;
		return Stream.from(iterator).mapToDouble(Number::doubleValue);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static Rt from(PrimitiveIterator.OfDouble iterator) {
		if (iterator == null || !iterator.hasNext()) return Rt.EMPTY;
		return Rt.ofSupplier(c -> {
			if (!iterator.hasNext()) return false;
			c.accept(iterator.nextDouble());
			return true;
		});
	}

	DoubleStream(NextSupplier<? extends E> supplier) {
		this.supplier = Reflect.unchecked(supplier);
	}

	/**
	 * Provides access to the supplier.
	 */
	public NextSupplier<E> supplier() {
		return supplier;
	}

	// adapters

	/**
	 * Adapts the stream, wrapping unchecked exceptions to runtime as needed.
	 */
	public Rt runtime() {
		return emptyInstance() ? Rt.EMPTY : new Rt(exSupplier(ExceptionAdapter.runtime));
	}

	/**
	 * Adapts stream exceptions.
	 */
	public <F extends Exception> Ex<F> ex(ExceptionAdapter<F> adapter) {
		return emptyInstance() ? Ex.empty() : new Ex<>(exSupplier(adapter));
	}

	// filtration

	/**
	 * Only streams elements that match the filter.
	 */
	public DoubleStream<E> filter(Excepts.DoublePredicate<? extends E> filter) {
		return noOp(filter) ? this : update(filterSupplier(supplier(), filter));
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.DoublePredicate<? extends E> predicate) throws E {
		return filter(predicate).supplier().next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.DoublePredicate<? extends E> predicate) throws E {
		return noOp(predicate) ? true : !anyMatch(i -> !predicate.test(i));
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
		return noOp(mapper) ? emptyVal() : update(mapSupplier(supplier(), mapper));
	}

	/**
	 * Maps stream elements to int values.
	 */
	public abstract IntStream<E> mapToInt(Excepts.DoubleToIntFunction<? extends E> mapper);

	/**
	 * Maps stream elements to int values.
	 */
	public abstract LongStream<E> mapToLong(Excepts.DoubleToLongFunction<? extends E> mapper);

	/**
	 * Maps stream elements to typed values.
	 */
	public abstract <T> Stream<E, T>
		mapToObj(Excepts.DoubleFunction<? extends E, ? extends T> mapper);

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public DoubleStream<E>
		flatMap(Excepts.DoubleFunction<? extends E, ? extends DoubleStream<E>> mapper) {
		return noOp(mapper) ? emptyVal() :
			update(flatSupplier(mapToObj(mapper).filter(Objects::nonNull).supplier()));
	}

	// manipulation

	/**
	 * Limits the number of elements.
	 */
	public DoubleStream<E> limit(long size) {
		var counter = Counter.of(size);
		return update(
			preSupplier(supplier(), () -> counter.preInc(-Long.signum(counter.get())) > 0L));
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
		return emptyInstance() ? this : update(adaptedSupplier(supplier(), s -> sortedSupplier(s)));
	}

	// termination

	/**
	 * Returns the next element or default.
	 */
	public Double next() throws E {
		var receiver = new NextSupplier.Receiver<E>();
		return supplier().next(receiver) ? receiver.value : null;
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
			if (!supplier().next(receiver)) break;
		return this;
	}

	/**
	 * Returns true if no elements are available. Consumes the next value if available.
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
	 * Provides a one-off iterator that wraps checked exceptions as runtime.
	 */
	public PrimitiveIterator.OfDouble iterator() {
		return iterator(supplier());
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.DoubleConsumer<? extends E> consumer) throws E {
		supplier().forEach(consumer);
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
		return collector == null ? null :
			collect(collector.supplier(), collector.doubleAccumulator(), collector.finisher());
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
		return collect(supplier(), supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Returns the min value, or default.
	 */
	public double min(double def) throws E {
		return reduce(Reduce.Doubles.min(), def);
	}

	/**
	 * Returns the max value, or default.
	 */
	public double max(double def) throws E {
		return reduce(Reduce.Doubles.max(), def);
	}

	/**
	 * Returns the sum value, allowing overflows, or 0.
	 */
	public double sum() throws E {
		return reduce(Reduce.Doubles.sum(), 0L);
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
		return reduceStream(supplier(), accumulator);
	}

	/**
	 * Reduces stream to a value using an accumulator, or default.
	 */
	public double reduce(Excepts.DoubleBiOperator<? extends E> accumulator, double def) throws E {
		return Basics.defDouble(reduce(accumulator), def);
	}

	// support

	/**
	 * Returns an empty instance.
	 */
	abstract DoubleStream<E> emptyVal();

	/**
	 * Cast to concrete type.
	 */
	<F extends Exception, S extends DoubleStream<F>> S cast(DoubleStream<F> stream) {
		return Reflect.unchecked(stream);
	}

	/**
	 * Returns true if this is the empty instance.
	 */
	private boolean emptyInstance() {
		return this == emptyVal();
	}

	private boolean noOp(Object op) {
		return op == null || emptyInstance();
	}

	private <F extends Exception> NextSupplier<F> exSupplier(ExceptionAdapter<F> adapter) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!adapter.getBool(() -> supplier().next(receiver))) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private Excepts.DoubleConsumer<E> nullConsumer() {
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private DoubleStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
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

	private static <E extends Exception, T> Stream.NextSupplier<E, T> objSupplier(
		NextSupplier<E> supplier, Excepts.DoubleFunction<? extends E, ? extends T> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> preSupplier(NextSupplier<E> supplier,
		Excepts.BoolSupplier<? extends E> pre) {
		return c -> pre.getAsBool() ? supplier.next(c) : false;
	}

	private static <E extends Exception> NextSupplier<E> adaptedSupplier(NextSupplier<E> supplier,
		Excepts.Operator<E, NextSupplier<E>> adapter) {
		var receiver = new Stream.NextSupplier.Receiver<E, NextSupplier<E>>();
		return c -> {
			if (receiver.value == null) receiver.value = adapter.apply(supplier);
			return receiver.value.next(c);
		};
	}

	private static <E extends Exception> Double reduceStream(NextSupplier<E> supplier,
		Excepts.DoubleBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return null;
		var receiver = new NextSupplier.Receiver<E>();
		if (!supplier.next(receiver)) return null;
		for (double d = receiver.value;;) {
			if (!supplier.next(receiver)) return d;
			d = accumulator.applyAsDouble(d, receiver.value);
		}
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, ? extends DoubleStream<E>> supplier) {
		var streamReceiver = new Stream.NextSupplier.Receiver<E, DoubleStream<E>>();
		var receiver = new NextSupplier.Receiver<E>();
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

	private static <E extends Exception> NextSupplier<E>
		sortedSupplier(NextSupplier<? extends E> supplier) throws E {
		var array = collect(supplier, DynamicArray::doubles, DynamicArray.OfDouble::accept, t -> t);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
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

	private static <E extends Exception, A, R> R collect(NextSupplier<E> next,
		Functions.Supplier<A> supplier, Functions.ObjDoubleConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		if (supplier == null || finisher == null) return null;
		var container = supplier.get();
		if (accumulator != null && container != null)
			next.forEach(i -> accumulator.accept(container, i));
		return finisher.apply(container);
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
