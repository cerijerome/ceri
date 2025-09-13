package ceri.common.stream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import ceri.common.array.ArrayUtil;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArray;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
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
		Functions.ObjLongConsumer<A> accumulator();

		/**
		 * Provides a finisher to complete the container.
		 */
		Functions.Function<A, R> finisher();
	}

	/**
	 * Collector support.
	 */
	public static class Collect {
		/** Collects elements into a primitive array. */
		public static final Collector<DynamicArray.OfLong, long[]> array =
			of(DynamicArray::longs, DynamicArray.OfLong::accept, DynamicArray::truncate);
		/** Collects elements into a sorted primitive array. */
		Collector<DynamicArray.OfLong, long[]> sortedArray = of(DynamicArray::longs,
			DynamicArray.OfLong::accept, a -> ArrayUtil.longs.sort(a.truncate()));

		private Collect() {}

		/**
		 * A collector instance composed from its functions.
		 */
		record Composed<A, R>(Functions.Supplier<A> supplier,
			Functions.ObjLongConsumer<A> accumulator, Functions.Function<A, R> finisher)
			implements Collector<A, R> {}

		/**
		 * Composes a collector without a finisher.
		 */
		static <A> Collector<A, A> of(Functions.Supplier<A> supplier,
			Functions.ObjLongConsumer<A> accumulator) {
			return new Composed<>(supplier, accumulator, t -> t);
		}

		/**
		 * Composes a collector from functions.
		 */
		static <A, R> Collector<A, R> of(Functions.Supplier<A> supplier,
			Functions.ObjLongConsumer<A> accumulator, Functions.Function<A, R> finisher) {
			return new Composed<>(supplier, accumulator, finisher);
		}
	}

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
		return Reflect.unchecked(EMPTY);
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
		return RawArray.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : new LongStream<E>(arraySupplier(values, o, l)));
	}

	/**
	 * Streams a range of values.
	 */
	public static <E extends Exception> LongStream<E> slice(long offset, long length) {
		var counter = Counter.of(0L);
		return ofSupplier(c -> {
			if (counter.get() >= length) return false;
			c.accept(offset + counter.preInc(1));
			return true;
		});
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> LongStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		var iterator = iterable.iterator();
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToLong(Number::longValue);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static <E extends Exception> LongStream<E> from(PrimitiveIterator.OfLong iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return ofSupplier(c -> {
			if (!iterator.hasNext()) return false;
			c.accept(iterator.nextLong());
			return true;
		});
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

	// filtration

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

	// mapping

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

	// manipulation

	/**
	 * Limits the number of elements.
	 */
	public LongStream<E> limit(long size) {
		var counter = Counter.of(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.get())) > 0L));
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

	// termination

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
		return Basics.defLong(next(), def);
	}

	/**
	 * Skips up to the given number of elements.
	 */
	public LongStream<E> skip(int count) throws E {
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
	public PrimitiveIterator.OfLong iterator() {
		return iterator(supplier);
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.LongConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	// collection

	/**
	 * Collects elements into an array.
	 */
	public long[] toArray() throws E {
		return collect(Collect.array);
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
	public <R> R collect(Functions.Supplier<R> supplier, Functions.ObjLongConsumer<R> accumulator)
		throws E {
		return collect(supplier, accumulator, r -> r);
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Functions.Supplier<A> supplier,
		Functions.ObjLongConsumer<A> accumulator, Functions.Function<A, R> finisher) throws E {
		return collect(this.supplier, supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Return the min value, or default.
	 */
	public long min(long def) throws E {
		return reduce(Reduce.min(), def);
	}

	/**
	 * Return the max value, or default.
	 */
	public long max(long def) throws E {
		return reduce(Reduce.max(), def);
	}

	/**
	 * Reduces stream to a value using an accumulator, or null.
	 */
	public Long reduce(Excepts.LongBiOperator<? extends E> accumulator) throws E {
		return reduce(supplier, accumulator);
	}

	/**
	 * Reduces stream to a value using an accumulator, or default.
	 */
	public long reduce(Excepts.LongBiOperator<? extends E> accumulator, long def) throws E {
		return Basics.defLong(reduce(accumulator), def);
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
		return Reflect.unchecked(NULL_CONSUMER);
	}

	private LongStream<E> update(NextSupplier<E> supplier) {
		if (!emptyInstance()) this.supplier = supplier;
		return this;
	}

	private static <E extends Exception> NextSupplier<E> arraySupplier(long[] array, int offset,
		int length) {
		var counter = Counter.of(0);
		return c -> {
			if (counter.get() >= length) return false;
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
		var array = collect(supplier, DynamicArray::longs, DynamicArray.OfLong::accept, t -> t);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
	}

	private static <E extends Exception, A, R> R collect(NextSupplier<E> next,
		Functions.Supplier<A> supplier, Functions.ObjLongConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		if (supplier == null || finisher == null) return null;
		var container = supplier.get();
		if (accumulator != null && container != null)
			next.forEach(l -> accumulator.accept(container, l));
		return finisher.apply(container);
	}

	private static <E extends Exception> Long reduce(NextSupplier<E> supplier,
		Excepts.LongBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return null;
		var receiver = new NextSupplier.Receiver<E>();
		if (!supplier.next(receiver)) return null;
		for (long l = receiver.value;;) {
			if (!supplier.next(receiver)) return l;
			l = accumulator.applyAsLong(l, receiver.value);
		}
	}

	private static <E extends Exception> PrimitiveIterator.OfLong
		iterator(NextSupplier<E> supplier) {
		var receiver = new NextSupplier.Receiver<E>();
		Excepts.BoolSupplier<E> next = () -> supplier.next(receiver);
		return new PrimitiveIterator.OfLong() {
			private boolean fetch = true;
			private boolean active = true;

			@Override
			public boolean hasNext() {
				if (fetch) active = ExceptionAdapter.runtime.getBool(next);
				fetch = false;
				return active;
			}

			@Override
			public long nextLong() {
				if (!hasNext()) throw new NoSuchElementException();
				fetch = true;
				return receiver.value;
			}
		};
	}
}
