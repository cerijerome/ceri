package ceri.common.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import ceri.common.array.DynamicArray;
import ceri.common.function.Excepts;
import ceri.common.function.FunctionUtil;
import ceri.common.util.BasicUtil;

/**
 * A simple stream that allows checked exceptions. Modifiers change the current stream rather than
 * create a new instance. Not thread-safe.
 */
public class LongStream<E extends Exception> {
	private static final LongStream<RuntimeException> EMPTY = new LongStream<>(Stream.empty());
	private Stream<E, Long> stream;

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> LongStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <E extends Exception> LongStream<E> of(long... values) {
		if (values == null || values.length == 0) return empty();
		return from(java.util.stream.LongStream.of(values).iterator());
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> LongStream<E> from(Iterable<Long> iterable) {
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> LongStream<E> from(Iterator<Long> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return LongStream.of(Stream.from(iterator));
	}

	static <E extends Exception> LongStream<E> of(Stream<E, Long> stream) {
		return new LongStream<>(stream);
	}

	private LongStream(Stream<E, Long> stream) {
		this.stream = stream;
	}

	/**
	 * Only streams elements that match the filter.
	 */
	public LongStream<E> filter(Excepts.LongPredicate<E> filter) {
		Objects.requireNonNull(filter);
		stream.filter(filter::test);
		return this;
	}

	/**
	 * Return stream of boxed ints.
	 */
	public Stream<E, Long> boxed() {
		return stream;
	}

	/**
	 * Maps stream elements to new values.
	 */
	public LongStream<E> map(Excepts.LongOperator<E> mapper) {
		Objects.requireNonNull(mapper);
		stream.map(mapper::applyAsLong);
		return this;
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(Excepts.LongFunction<E, T> mapper) {
		Objects.requireNonNull(mapper);
		return stream.map(mapper::apply);
	}

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public LongStream<E>
		flatMap(Excepts.LongFunction<? extends E, LongStream<? extends E>> mapper) {
		Objects.requireNonNull(mapper);
		stream.flatMap(t -> FunctionUtil.safeApply(mapper.apply(t), s -> s.stream));
		return this;
	}

	/**
	 * Limits the number of elements.
	 */
	public LongStream<E> limit(long size) {
		stream.limit(size);
		return this;
	}

	/**
	 * Streams distinct elements, by first collecting into a linked set.
	 */
	public LongStream<E> distinct() throws E {
		stream.distinct();
		return this;
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public LongStream<E> sorted() throws E {
		stream.sorted(Comparator.naturalOrder());
		return this;
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(Excepts.LongConsumer<E> action) throws E {
		Objects.requireNonNull(action);
		stream.forEach(action::accept);
	}

	/**
	 * Collects elements into an array.
	 */
	public long[] toArray() throws E {
		var array = DynamicArray.longs();
		stream.forEach(array::accept);
		return array.truncate();
	}

	/**
	 * Returns the element count.
	 */
	public long count() throws E {
		return stream.count();
	}

	/**
	 * Returns the minimum value or null.
	 */
	public Long min() throws E {
		return stream.min(Comparator.naturalOrder());
	}

	/**
	 * Returns the minimum value or default.
	 */
	public long min(long def) throws E {
		return BasicUtil.defLong(min(), def);
	}

	/**
	 * Returns the minimum value or null.
	 */
	public Long max() throws E {
		return stream.max(Comparator.naturalOrder());
	}

	/**
	 * Returns the maximum value or default.
	 */
	public long max(long def) throws E {
		return BasicUtil.defLong(max(), def);
	}

	/**
	 * Reduces stream to an element or null, using an accumulator.
	 */
	public Long reduce(Excepts.LongBiOperator<E> accumulator) throws E {
		return stream.reduce(accumulator::applyAsLong);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public long reduce(long identity, Excepts.LongBiOperator<E> accumulator) throws E {
		return stream.reduce(identity, accumulator::applyAsLong);
	}

	/**
	 * Returns true if any element matches.
	 */
	public boolean anyMatch(Excepts.LongPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.anyMatch(predicate::test);
	}

	/**
	 * Returns true if all elements match.
	 */
	public boolean allMatch(Excepts.LongPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.allMatch(predicate::test);
	}

	/**
	 * Returns true if no elements match.
	 */
	public boolean noneMatch(Excepts.LongPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.noneMatch(predicate::test);
	}

	/**
	 * Returns the next element or null.
	 */
	public Long next() throws E {
		return stream.next();
	}

	/**
	 * Returns the next element or default.
	 */
	public long next(long def) throws E {
		return stream.next(def);
	}
}
