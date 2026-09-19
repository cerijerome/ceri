package ceri.common.stream;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import ceri.common.array.Array;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArray;
import ceri.common.collect.Sets;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Stream.ExAdapter;
import ceri.common.util.Basics;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Where possible, modifiers change the current
 * stream rather than create a new instance. Not thread-safe. Implementation note: the supplier is
 * replaced on change, so the adapter functions must pass in the current supplier.
 */
public abstract class IntStream<E extends Exception> {
	private static final Excepts.IntConsumer<?> NULL_CONSUMER = _ -> {};
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
		Functions.ObjIntConsumer<A> intAccumulator();

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
		class Receiver<E extends Exception> implements Excepts.IntConsumer<E>, IntConsumer {
			public int value;

			@Override
			public void accept(int value) {
				this.value = value;
			}
		}

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
		default boolean nextFiltered(NextSupplier.Receiver<? extends E> receiver,
			Excepts.IntPredicate<? extends E> filter) throws E {
			while (true) {
				if (!next(receiver)) return false;
				if (filter.test(receiver.value)) return true;
			}
		}
	}

	/**
	 * An implementation that only allows runtime exceptions.
	 */
	public static class Rt extends IntStream<RuntimeException> {
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
			return super.emptyInstance() ? Ex.empty() : new Ex<>(supplierEx());
		}

		@Override
		public <E extends Exception> Ex<E> ex(ExceptionAdapter<E> adapter) {
			return ex();
		}

		@Override
		public Rt filter(Excepts.IntPredicate<? extends RuntimeException> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter, allowing exceptions.
		 */
		public <E extends Exception> Ex<E> filterEx(Excepts.IntPredicate<? extends E> filter) {
			return super.noOp(filter) ? ex() : new Ex<>(filterSupplier(supplierEx(), filter));
		}

		@Override
		public Stream.Rt<Integer> boxed() {
			return mapToObj(Integer::valueOf);
		}

		@Override
		public LongStream.Rt unsigned() {
			return mapToLong(Maths::uint);
		}

		@Override
		public Rt map(Excepts.IntOperator<? extends RuntimeException> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to new values, allowing exceptions.
		 */
		public <E extends Exception> Ex<E> mapEx(Excepts.IntOperator<? extends E> mapper) {
			return super.noOp(mapper) ? Ex.empty() : new Ex<>(mapSupplier(supplierEx(), mapper));
		}

		@Override
		public LongStream.Rt
			mapToLong(Excepts.IntToLongFunction<? extends RuntimeException> mapper) {
			return super.noOp(mapper) ? LongStream.Rt.EMPTY :
				LongStream.Rt.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to long values, allowing exceptions.
		 */
		public <E extends Exception> LongStream.Ex<E>
			mapToLongEx(Excepts.IntToLongFunction<? extends E> mapper) {
			return super.noOp(mapper) ? LongStream.Ex.empty() :
				LongStream.Ex.ofSupplier(super.longSupplier(supplierEx(), mapper));
		}

		@Override
		public DoubleStream.Rt
			mapToDouble(Excepts.IntToDoubleFunction<? extends RuntimeException> mapper) {
			return super.noOp(mapper) ? DoubleStream.Rt.EMPTY :
				DoubleStream.Rt.ofSupplier(super.doubleSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to double values, allowing exceptions.
		 */
		public <E extends Exception> DoubleStream.Ex<E>
			mapToDoubleEx(Excepts.IntToDoubleFunction<? extends E> mapper) {
			return super.noOp(mapper) ? DoubleStream.Ex.empty() :
				DoubleStream.Ex.ofSupplier(super.doubleSupplier(supplierEx(), mapper));
		}

		@Override
		public <T> Stream.Rt<T>
			mapToObj(Excepts.IntFunction<? extends RuntimeException, ? extends T> mapper) {
			return super.noOp(mapper) ? Stream.Rt.empty() :
				Stream.Rt.ofSupplier(super.objSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to typed values, allowing exceptions.
		 */
		public <E extends Exception, T> Stream.Ex<E, T>
			mapToObjEx(Excepts.IntFunction<? extends E, ? extends T> mapper) {
			return super.noOp(mapper) ? Stream.Ex.empty() :
				Stream.Ex.ofSupplier(super.objSupplier(supplierEx(), mapper));
		}

		@Override
		public Rt flatMap(Excepts.IntFunction<? extends RuntimeException, //
			? extends IntStream<RuntimeException>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams, allowing exceptions.
		 */
		public <E extends Exception> Ex<E>
			flatMapEx(Excepts.IntFunction<? extends E, ? extends IntStream<E>> mapper) {
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

		/**
		 * Calls the consumer for each element.
		 */
		public <E extends Exception> void forEachEx(Excepts.IntConsumer<? extends E> consumer)
			throws E {
			this.<E>ex().forEach(consumer);
		}

		@Override
		Rt emptyVal() {
			return EMPTY;
		}

		private <E extends Exception> NextSupplier<E> supplierEx() {
			return Reflect.unchecked(supplier());
		}
	}

	/**
	 * An implementation that allows a checked exception type.
	 */
	public static class Ex<E extends Exception> extends IntStream<E> {
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
		public Ex<E> filter(Excepts.IntPredicate<? extends E> filter) {
			return cast(super.filter(filter));
		}

		/**
		 * Only streams elements that match the filter.
		 */
		public Ex<E> filterRt(Excepts.IntPredicate<? extends RuntimeException> filter) {
			return filter(Reflect.unchecked(filter));
		}

		@Override
		public Stream.Ex<E, Integer> boxed() {
			return mapToObj(Integer::valueOf);
		}

		@Override
		public LongStream.Ex<E> unsigned() {
			return mapToLong(Maths::uint);
		}

		@Override
		public Ex<E> map(Excepts.IntOperator<? extends E> mapper) {
			return cast(super.map(mapper));
		}

		/**
		 * Maps stream elements to new values.
		 */
		public Ex<E> mapRt(Excepts.IntOperator<? extends RuntimeException> mapper) {
			return map(Reflect.unchecked(mapper));
		}

		@Override
		public LongStream.Ex<E> mapToLong(Excepts.IntToLongFunction<? extends E> mapper) {
			return super.noOp(mapper) ? LongStream.Ex.empty() :
				LongStream.Ex.ofSupplier(super.longSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to long values, allowing exceptions.
		 */
		public LongStream.Ex<E>
			mapToLongRt(Excepts.IntToLongFunction<? extends RuntimeException> mapper) {
			return mapToLong(Reflect.unchecked(mapper));
		}

		@Override
		public DoubleStream.Ex<E> mapToDouble(Excepts.IntToDoubleFunction<? extends E> mapper) {
			return super.noOp(mapper) ? DoubleStream.Ex.empty() :
				DoubleStream.Ex.ofSupplier(super.doubleSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to double values, allowing exceptions.
		 */
		public DoubleStream.Ex<E>
			mapToDoubleRt(Excepts.IntToDoubleFunction<? extends RuntimeException> mapper) {
			return mapToDouble(Reflect.unchecked(mapper));
		}

		@Override
		public <T> Stream.Ex<E, T> mapToObj(Excepts.IntFunction<? extends E, ? extends T> mapper) {
			return super.noOp(mapper) ? Stream.Ex.empty() :
				Stream.Ex.ofSupplier(super.objSupplier(supplier(), mapper));
		}

		/**
		 * Maps stream elements to typed values.
		 */
		public <T> Stream.Ex<E, T>
			mapToObjRt(Excepts.IntFunction<? extends RuntimeException, ? extends T> mapper) {
			return mapToObj(Reflect.unchecked(mapper));
		}

		@Override
		public Ex<E> flatMap(Excepts.IntFunction<? extends E, ? extends IntStream<E>> mapper) {
			return cast(super.flatMap(mapper));
		}

		/**
		 * Maps each element to a stream, and flattens the streams.
		 */
		public Ex<E> flatMapRt(Excepts.IntFunction<? extends RuntimeException, //
			? extends IntStream<? extends RuntimeException>> mapper) {
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

		/**
		 * Calls the consumer for each element.
		 */
		public void forEachRt(Excepts.IntConsumer<? extends RuntimeException> consumer) throws E {
			forEach(Reflect.unchecked(consumer));
		}

		@Override
		Ex<E> emptyVal() {
			return empty();
		}
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(int... values) {
		return of(values, 0);
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(int[] values, int offset) {
		return of(values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a stream of values.
	 */
	public static Rt of(int[] values, int offset, int length) {
		if (values == null) return Rt.EMPTY;
		return RawArray.applySlice(values, offset, length,
			(o, l) -> l == 0 ? Rt.EMPTY : Rt.ofSupplier(arraySupplier(values, o, l)));
	}

	/**
	 * Streams a range of values.
	 */
	public static Rt slice(int offset, int length) {
		return Rt.ofSupplier(sliceSupplier(offset, length));
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static Rt from(Iterable<? extends Number> iterable) {
		return Stream.from(iterable).mapToInt(Number::intValue);

	}

	/**
	 * Returns a stream of iterable values. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception> Ex<E> from(Iterable<? extends Number> iterable,
		ExAdapter<E> exAdapter) {
		return iterable == null ? Ex.empty() :
			Stream.from(iterable, exAdapter).mapToInt(Number::intValue);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static Rt from(PrimitiveIterator.OfInt iterator) {
		return (iterator == null || !iterator.hasNext()) ? Rt.EMPTY :
			Rt.ofSupplier(iteratorSupplier(iterator, null));
	}

	/**
	 * Returns a stream for a primitive iterator. Allows runtime exceptions to be unpacked and
	 * thrown.
	 */
	public static <E extends Exception> Ex<E> from(PrimitiveIterator.OfInt iterator,
		ExAdapter<E> exAdapter) {
		return (iterator == null || !iterator.hasNext()) ? Ex.empty() :
			Ex.ofSupplier(iteratorSupplier(iterator, exAdapter));
	}

	/**
	 * Returns a stream from the java stream.
	 */
	public static Rt from(java.util.stream.IntStream stream) {
		return stream == null ? Rt.EMPTY : from(stream.spliterator());
	}

	/**
	 * Returns a stream from the java stream. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception> Ex<E> from(java.util.stream.IntStream stream,
		ExAdapter<E> exAdapter) {
		return stream == null ? Ex.empty() : from(stream.spliterator(), exAdapter);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static Rt from(Spliterator.OfInt spliterator) {
		return spliterator == null ? Rt.EMPTY :
			Rt.ofSupplier(spliteratorSupplier(spliterator, null));
	}

	/**
	 * Returns a stream of spliterator values. Allows runtime exceptions to be unpacked and thrown.
	 */
	public static <E extends Exception> Ex<E> from(Spliterator.OfInt spliterator,
		ExAdapter<E> exAdapter) {
		return spliterator == null ? Ex.empty() :
			Ex.ofSupplier(spliteratorSupplier(spliterator, exAdapter));
	}

	/**
	 * Creates a single stream from sequential streams.
	 */
	public static Rt merge(Rt... streams) {
		return Array.isEmpty(streams) ? Rt.EMPTY :
			slice(0, streams.length).flatMap(i -> Basics.def(streams[i], Ex.empty()));
	}

	/**
	 * Creates a single stream from sequential streams.
	 */
	@SafeVarargs
	public static <E extends Exception> Ex<E> merge(Ex<? extends E>... streams) {
		return Array.isEmpty(streams) ? Ex.empty() : slice(0, streams.length)
			.flatMapEx(i -> Basics.def(Reflect.unchecked(streams[i]), Ex.empty()));
	}

	IntStream(NextSupplier<? extends E> supplier) {
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
		return noOp(adapter) ? Ex.empty() : new Ex<>(exSupplier(adapter));
	}

	// filtration

	/**
	 * Only streams elements that match the filter.
	 */
	public IntStream<E> filter(Excepts.IntPredicate<? extends E> filter) {
		return noOp(filter) ? this : update(filterSupplier(supplier(), filter));
	}

	/**
	 * Returns true if any element matched.
	 */
	public boolean anyMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		return filter(predicate).supplier().next(nullConsumer());
	}

	/**
	 * Returns true if all elements matched.
	 */
	public boolean allMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		return noOp(predicate) ? true : !anyMatch(i -> !predicate.test(i));
	}

	/**
	 * Returns true if no elements matched.
	 */
	public boolean noneMatch(Excepts.IntPredicate<? extends E> predicate) throws E {
		return !anyMatch(predicate);
	}

	// mapping

	/**
	 * Maps stream elements to boxed types.
	 */
	public abstract Stream<E, Integer> boxed();

	/**
	 * Maps stream elements to unsigned ints.
	 */
	public abstract LongStream<E> unsigned();

	/**
	 * Maps stream elements to new values.
	 */
	public IntStream<E> map(Excepts.IntOperator<? extends E> mapper) {
		return noOp(mapper) ? emptyVal() : update(mapSupplier(supplier(), mapper));
	}

	/**
	 * Maps stream elements to long values.
	 */
	public abstract LongStream<E> mapToLong(Excepts.IntToLongFunction<? extends E> mapper);

	/**
	 * Maps stream elements to double values.
	 */
	public abstract DoubleStream<E> mapToDouble(Excepts.IntToDoubleFunction<? extends E> mapper);

	/**
	 * Maps stream elements to typed values.
	 */
	public abstract <T> Stream<E, T> mapToObj(Excepts.IntFunction<? extends E, ? extends T> mapper);

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public IntStream<E> flatMap(Excepts.IntFunction<? extends E, ? extends IntStream<E>> mapper) {
		return noOp(mapper) ? emptyVal() :
			update(flatSupplier(mapToObj(mapper).filter(Objects::nonNull).supplier()));
	}

	// manipulation

	/**
	 * Limits the number of elements.
	 */
	public IntStream<E> limit(long size) {
		return emptyInstance() ? this : update(limitSupplier(supplier(), size));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public IntStream<E> distinct() {
		return filter(Sets.of()::add);
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public IntStream<E> sorted() {
		return emptyInstance() ? this : update(adaptedSupplier(supplier(), s -> sortedSupplier(s)));
	}

	// termination

	/**
	 * Returns the next element or default.
	 */
	public Integer next() throws E {
		var receiver = new NextSupplier.Receiver<E>();
		return supplier().next(receiver) ? receiver.value : null;
	}

	/**
	 * Returns the next element or default.
	 */
	public int next(int def) throws E {
		return Basics.defInt(next(), def);
	}

	/**
	 * Skips up to the given number of elements.
	 */
	public IntStream<E> skip(int count) throws E {
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
	public PrimitiveIterator.OfInt iterator() {
		return iterator(supplier());
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.IntConsumer<? extends E> consumer) throws E {
		supplier().forEach(consumer);
	}

	// collection

	/**
	 * Collects elements into an array.
	 */
	public int[] toArray() throws E {
		return collect(Collect.Ints.array);
	}

	/**
	 * Collects elements with a collector.
	 */
	public <A, R> R collect(Collector<A, R> collector) throws E {
		return collector == null ? null :
			collect(collector.supplier(), collector.intAccumulator(), collector.finisher());
	}

	/**
	 * Collect elements with container supplier and accumulator.
	 */
	public <R> R collect(Functions.Supplier<R> supplier, Functions.ObjIntConsumer<R> accumulator)
		throws E {
		return collect(supplier, accumulator, r -> r);
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Functions.Supplier<A> supplier, Functions.ObjIntConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		return collect(supplier(), supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Returns the min value, or default.
	 */
	public int min(int def) throws E {
		return reduce(Reduce.Ints.min(), def);
	}

	/**
	 * Returns the max value, or default.
	 */
	public int max(int def) throws E {
		return reduce(Reduce.Ints.max(), def);
	}

	/**
	 * Returns the sum value, allowing overflows, or 0.
	 */
	public int sum() throws E {
		return reduce(Reduce.Ints.sum(), 0);
	}

	/**
	 * Returns the average value, or 0.
	 */
	public double average() throws E {
		return collect(Collect.Ints.average);
	}

	/**
	 * Reduces stream to a value using an accumulator, or null.
	 */
	public Integer reduce(Excepts.IntBiOperator<? extends E> accumulator) throws E {
		return reduceStream(supplier(), accumulator);
	}

	/**
	 * Reduces stream to a value using an accumulator, or default.
	 */
	public int reduce(Excepts.IntBiOperator<? extends E> accumulator, int def) throws E {
		return Basics.defInt(reduce(accumulator), def);
	}

	// support

	/**
	 * Returns an empty instance.
	 */
	abstract IntStream<E> emptyVal();

	/**
	 * Cast to concrete type.
	 */
	<F extends Exception, S extends IntStream<F>> S cast(IntStream<F> stream) {
		return Reflect.unchecked(stream);
	}

	/**
	 * Returns true if this is the empty instance.
	 */
	private boolean emptyInstance() {
		return this == Rt.EMPTY || this == Ex.EMPTY;
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

	private Excepts.IntConsumer<E> nullConsumer() {
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private IntStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> filterSupplier(NextSupplier<E> supplier,
		Excepts.IntPredicate<? extends E> filter) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.nextFiltered(receiver, filter)) return false;
			c.accept(receiver.value);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> mapSupplier(NextSupplier<E> supplier,
		Excepts.IntOperator<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsInt(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> limitSupplier(NextSupplier<E> supplier,
		long size) {
		var counter = Counter.of(size);
		return preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.get())) > 0L);
	}

	private static <E extends Exception> LongStream.NextSupplier<E>
		longSupplier(NextSupplier<E> supplier, Excepts.IntToLongFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsLong(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> DoubleStream.NextSupplier<E>
		doubleSupplier(NextSupplier<E> supplier, Excepts.IntToDoubleFunction<? extends E> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.applyAsDouble(receiver.value));
			return true;
		};
	}

	private static <E extends Exception, T> Stream.NextSupplier<E, T> objSupplier(
		NextSupplier<E> supplier, Excepts.IntFunction<? extends E, ? extends T> mapper) {
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

	private static <E extends Exception> Integer reduceStream(NextSupplier<E> supplier,
		Excepts.IntBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return null;
		var receiver = new NextSupplier.Receiver<E>();
		if (!supplier.next(receiver)) return null;
		for (int i = receiver.value;;) {
			if (!supplier.next(receiver)) return i;
			i = accumulator.applyAsInt(i, receiver.value);
		}
	}

	private static <E extends Exception, A, R> R collect(NextSupplier<E> next,
		Functions.Supplier<A> supplier, Functions.ObjIntConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		if (supplier == null || finisher == null) return null;
		var container = supplier.get();
		if (accumulator != null && container != null)
			next.forEach(i -> accumulator.accept(container, i));
		return finisher.apply(container);
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(int[] array, int offset,
		int length) {
		var counter = Counter.of(0);
		return c -> {
			if (counter.get() >= length) return false;
			c.accept(array[offset + counter.preInc(1)]);
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E> sliceSupplier(int offset, int length) {
		var counter = Counter.of(0);
		return c -> {
			if (counter.get() >= length) return false;
			c.accept(offset + counter.preInc(1));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		iteratorSupplier(PrimitiveIterator.OfInt iterator, Stream.ExAdapter<E> exAdapter) {
		return c -> {
			try {
				if (!iterator.hasNext()) return false;
				c.accept(iterator.next());
				return true;
			} catch (RuntimeException e) {
				ExAdapter.adapt(exAdapter, e);
				throw e;
			}
		};
	}

	private static <E extends Exception> NextSupplier<E>
		spliteratorSupplier(Spliterator.OfInt spliterator, Stream.ExAdapter<E> exAdapter) {
		var receiver = new NextSupplier.Receiver<RuntimeException>();
		return c -> {
			try {
				if (!spliterator.tryAdvance(receiver)) return false;
				c.accept(receiver.value);
				return true;
			} catch (RuntimeException e) {
				ExAdapter.adapt(exAdapter, e);
				throw e;
			}
		};
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, ? extends IntStream<E>> supplier) {
		var streamReceiver = new Stream.NextSupplier.Receiver<E, IntStream<E>>();
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
		var array = collect(supplier, DynamicArray::ints, DynamicArray.OfInt::accept, t -> t);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
	}

	private static <E extends Exception> PrimitiveIterator.OfInt
		iterator(NextSupplier<E> supplier) {
		var receiver = new NextSupplier.Receiver<E>();
		Excepts.BoolSupplier<E> next = () -> supplier.next(receiver);
		return new PrimitiveIterator.OfInt() {
			private boolean fetch = true;
			private boolean active = true;

			@Override
			public boolean hasNext() {
				if (fetch) active = ExceptionAdapter.runtime.getBool(next);
				fetch = false;
				return active;
			}

			@Override
			public int nextInt() {
				if (!hasNext()) throw new NoSuchElementException();
				fetch = true;
				return receiver.value;
			}
		};
	}
}
