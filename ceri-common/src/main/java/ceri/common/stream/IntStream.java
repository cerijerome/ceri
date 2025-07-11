package ceri.common.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import ceri.common.function.Excepts.IntBiOperator;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.function.Excepts.IntOperator;
import ceri.common.function.Excepts.IntPredicate;
import ceri.common.util.BasicUtil;

/**
 * A simple stream that allows checked exceptions. Modifiers change the current stream rather than
 * create a new instance. Not thread-safe.
 */
public class IntStream<E extends Exception> {
	private static final IntStream<RuntimeException> EMPTY = new IntStream<>(Stream.empty());
	private Stream<E, Integer> stream;

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> IntStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <E extends Exception> IntStream<E> of(int... values) {
		if (values == null || values.length == 0) return empty();
		return from(java.util.stream.IntStream.of(values));
	}

	/**
	 * Returns a stream from a standard stream iterator.
	 */
	public static <E extends Exception> IntStream<E> from(java.util.stream.IntStream stream) {
		return from(stream.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> IntStream<E> from(Iterator<Integer> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return IntStream.of(Stream.from(iterator));
	}

	static <E extends Exception> IntStream<E> of(Stream<E, Integer> stream) {
		return new IntStream<>(stream);
	}

	private IntStream(Stream<E, Integer> stream) {
		this.stream = stream;
	}

	/**
	 * Only streams elements that match the filter.
	 */
	public IntStream<E> filter(IntPredicate<E> filter) {
		Objects.requireNonNull(filter);
		stream.filter(filter::test);
		return this;
	}

	/**
	 * Return stream of boxed ints.
	 */
	public Stream<E, Integer> boxed() {
		return stream;
	}

	/**
	 * Maps stream elements to new values.
	 */
	public IntStream<E> map(IntOperator<E> mapper) {
		Objects.requireNonNull(mapper);
		stream.map(mapper::applyAsInt);
		return this;
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(IntFunction<E, T> mapper) {
		Objects.requireNonNull(mapper);
		return stream.map(mapper::apply);
	}

	/**
	 * Limits the number of elements.
	 */
	public IntStream<E> limit(long size) {
		stream.limit(size);
		return this;
	}

	/**
	 * Streams distinct elements, by first collecting into a linked set.
	 */
	public IntStream<E> distinct() throws E {
		stream.distinct();
		return this;
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public IntStream<E> sorted() throws E {
		stream.sorted(Comparator.naturalOrder());
		return this;
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(IntConsumer<E> action) throws E {
		Objects.requireNonNull(action);
		stream.forEach(action::accept);
	}

	/**
	 * Collects elements into an array.
	 */
	public int[] toArray() throws E {
		return stream.toList().stream().mapToInt(i -> i).toArray();
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
	public Integer min() throws E {
		return stream.min(Comparator.naturalOrder());
	}

	/**
	 * Returns the minimum value or default.
	 */
	public int min(int def) throws E {
		return BasicUtil.defInt(min(), def);
	}

	/**
	 * Returns the minimum value or null.
	 */
	public Integer max() throws E {
		return stream.max(Comparator.naturalOrder());
	}

	/**
	 * Returns the maximum value or default.
	 */
	public int max(int def) throws E {
		return BasicUtil.defInt(max(), def);
	}

	/**
	 * Reduces stream to an element.
	 */
	public Integer reduce(int identity, IntBiOperator<E> accumulator) throws E {
		return stream.reduce(identity, accumulator::applyAsInt);
	}

	/**
	 * Returns true if any element matches.
	 */
	public boolean anyMatch(IntPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.anyMatch(predicate::test);
	}

	/**
	 * Returns true if all elements match.
	 */
	public boolean allMatch(IntPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.allMatch(predicate::test);
	}

	/**
	 * Returns true if no elements match.
	 */
	public boolean noneMatch(IntPredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.noneMatch(predicate::test);
	}

	/**
	 * Returns the next element or null.
	 */
	public Integer next() throws E {
		return stream.next();
	}

	/**
	 * Returns the next element or default.
	 */
	public int next(int def) throws E {
		return stream.next(def);
	}
}
