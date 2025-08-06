package ceri.common.stream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import ceri.common.array.ArrayUtil;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArrays;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.Counter;

/**
 * A simple stream that allows checked exceptions. Where possible, modifiers change the current
 * stream rather than create a new instance. Not thread-safe.
 */
public class IntStream<E extends Exception> {
	private static final Excepts.IntConsumer<?> NULL_CONSUMER = _ -> {};
	private static final IntStream<RuntimeException> EMPTY = new IntStream<>(_ -> false);
	private NextSupplier<E> supplier;

	public static void main(String[] args) {
		var array = Streams.ints(-1, 1, 0, -2, 3).collect(Collect.array);
		System.out.println(Arrays.toString(array));
	}

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
		Functions.ObjIntConsumer<A> accumulator();

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
		public static final Collector<?, int[]> array =
			new Composed<>(DynamicArray::ints, DynamicArray.OfInt::accept, DynamicArray::truncate);
		/** Collects elements into a sorted primitive array. */
		public static final Collector<?, int[]> sortedArray = new Composed<>(DynamicArray::ints,
			DynamicArray.OfInt::accept, a -> ArrayUtil.ints.sort(a.truncate()));
		/** Collects chars into a string. */
		public static final Collector<?, String> chars = new Composed<>(StringBuilder::new,
			(b, i) -> b.append((char) i), StringBuilder::toString);
		/** Collects code points into a string. */
		public static final Collector<?, String> codePoints = new Composed<>(StringBuilder::new,
			StringBuilder::appendCodePoint, StringBuilder::toString);

		private Collect() {}

		/**
		 * A collector instance composed from its functions.
		 */
		record Composed<A, R>(Functions.Supplier<A> supplier,
			Functions.ObjIntConsumer<A> accumulator, Functions.Function<A, R> finisher)
			implements Collector<A, R> {}
	}

	/**
	 * Reduction operators.
	 */
	public static class Reduce {
		private Reduce() {}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> min() {
			return (l, r) -> Integer.compare(l, r) <= 0 ? l : r;
		}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> max() {
			return (l, r) -> Integer.compare(l, r) >= 0 ? l : r;
		}

		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> and() {
			return (l, r) -> l & r;
		}

		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> or() {
			return (l, r) -> l | r;
		}

		/**
		 * Bitwise reduction operation.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> xor() {
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
		class Receiver<E extends Exception> implements Excepts.IntConsumer<E> {
			public int value;

			@Override
			public void accept(int value) throws E {
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
		if (values == null) return empty();
		return RawArrays.applySlice(values, offset, length,
			(o, l) -> l == 0 ? empty() : ofSupplier(arraySupplier(values, o, l)));
	}

	/**
	 * Streams a range of values.
	 */
	public static <E extends Exception> IntStream<E> range(int offset, int length) {
		var counter = Counter.ofInt(0);
		return ofSupplier(c -> {
			if (counter.count() >= length) return false;
			c.accept(offset + counter.preInc(1));
			return true;
		});
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> IntStream<E> from(Iterable<? extends Number> iterable) {
		if (iterable == null) return empty();
		var iterator = iterable.iterator();
		if (iterator == null || !iterator.hasNext()) return empty();
		return Stream.<E, Number>from(iterator).mapToInt(Number::intValue);
	}

	/**
	 * Returns a stream for a primitive iterator.
	 */
	public static <E extends Exception> IntStream<E> from(PrimitiveIterator.OfInt iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return ofSupplier(c -> {
			if (!iterator.hasNext()) return false;
			c.accept(iterator.nextInt());
			return true;
		});
	}

	/**
	 * Creates a stream for the supplier.
	 */
	public static <E extends Exception> IntStream<E> ofSupplier(NextSupplier<E> supplier) {
		return new IntStream<>(supplier);
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
	 * Maps stream elements to boxed types.
	 */
	public Stream<E, Integer> boxed() {
		return mapToObj(Integer::valueOf);
	}

	/**
	 * Maps stream elements to unsigned ints.
	 */
	public LongStream<E> unsigned() {
		return mapToLong(MathUtil::uint);
	}

	/**
	 * Maps stream elements to new values.
	 */
	public IntStream<E> map(Excepts.IntOperator<? extends E> mapper) {
		if (noOp(mapper)) return empty();
		return update(mapSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to long values.
	 */
	public LongStream<E> mapToLong(Excepts.IntToLongFunction<? extends E> mapper) {
		if (noOp(mapper)) return LongStream.empty();
		return LongStream.ofSupplier(longSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to double values.
	 */
	public DoubleStream<E> mapToDouble(Excepts.IntToDoubleFunction<? extends E> mapper) {
		if (noOp(mapper)) return DoubleStream.empty();
		return DoubleStream.ofSupplier(doubleSupplier(supplier, mapper));
	}

	/**
	 * Maps stream elements to typed values.
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
		return update(flatSupplier(mapToObj(mapper).filter(Objects::nonNull).supplier()));
	}

	// manipulators

	/**
	 * Limits the number of elements.
	 */
	public IntStream<E> limit(long size) {
		var counter = Counter.ofLong(size);
		return update(
			preSupplier(supplier, () -> counter.preInc(-Long.signum(counter.count())) > 0L));
	}

	/**
	 * IntStreams distinct elements.
	 */
	public IntStream<E> distinct() {
		return filter(new HashSet<Integer>()::add);
	}

	/**
	 * IntStreams sorted elements, by first collecting into a sorted list.
	 */
	public IntStream<E> sorted() {
		if (emptyInstance()) return this;
		return update(adaptedSupplier(supplier, s -> sortedSupplier(s)));
	}

	// termination

	/**
	 * Returns the next element or default.
	 */
	public Integer next() throws E {
		var receiver = new NextSupplier.Receiver<E>();
		return supplier.next(receiver) ? receiver.value : null;
	}

	/**
	 * Returns the next element or default.
	 */
	public int next(int def) throws E {
		return BasicUtil.defInt(next(), def);
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
	public PrimitiveIterator.OfInt iterator() {
		return iterator(supplier);
	}

	/**
	 * Calls the consumer for each element.
	 */
	public void forEach(Excepts.IntConsumer<? extends E> consumer) throws E {
		supplier.forEach(consumer);
	}

	// collection

	/**
	 * Collects elements into an array.
	 */
	public int[] toArray() throws E {
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
	public <R> R collect(Functions.Supplier<R> supplier, Functions.ObjIntConsumer<R> accumulator)
		throws E {
		return collect(supplier, accumulator, r -> r);
	}

	/**
	 * Collect elements with container supplier, accumulator, and finisher.
	 */
	public <A, R> R collect(Functions.Supplier<A> supplier, Functions.ObjIntConsumer<A> accumulator,
		Functions.Function<A, R> finisher) throws E {
		return collect(this.supplier, supplier, accumulator, finisher);
	}

	// reduction

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public Integer reduce(Excepts.IntBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return null;
		var next = next();
		return next == null ? null : reduce(next, accumulator);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public int reduce(int identity, Excepts.IntBiOperator<? extends E> accumulator) throws E {
		return reduce(supplier, identity, accumulator);
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

	private static <E extends Exception, R> Stream.NextSupplier<E, R> objSupplier(
		NextSupplier<E> supplier, Excepts.IntFunction<? extends E, ? extends R> mapper) {
		var receiver = new NextSupplier.Receiver<E>();
		return c -> {
			if (!supplier.next(receiver)) return false;
			c.accept(mapper.apply(receiver.value));
			return true;
		};
	}

	private static <E extends Exception> NextSupplier<E>
		flatSupplier(Stream.NextSupplier<E, ? extends IntStream<E>> supplier) {
		var streamReceiver = new ceri.common.stream.Stream.NextSupplier.Receiver<E, IntStream<E>>();
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
		var array = collect(supplier, DynamicArray::ints, DynamicArray.OfInt::accept, t -> t);
		Arrays.sort(array.array(), 0, array.index());
		return arraySupplier(array.array(), 0, array.index());
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

	private static <E extends Exception> int reduce(NextSupplier<E> supplier, int identity,
		Excepts.IntBiOperator<? extends E> accumulator) throws E {
		if (accumulator == null) return identity;
		var receiver = new NextSupplier.Receiver<E>();
		for (int i = identity;;) {
			if (!supplier.next(receiver)) return i;
			i = accumulator.applyAsInt(i, receiver.value);
		}
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
