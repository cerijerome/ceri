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
public class DoubleStream<E extends Exception> {
	private static final DoubleStream<RuntimeException> EMPTY = new DoubleStream<>(Stream.empty());
	private Stream<E, Double> stream;

	/**
	 * Returns an empty stream.
	 */
	public static <E extends Exception> DoubleStream<E> empty() {
		return BasicUtil.unchecked(EMPTY);
	}

	/**
	 * Returns a stream of values.
	 */
	@SafeVarargs
	public static <E extends Exception> DoubleStream<E> of(double... values) {
		if (values == null || values.length == 0) return empty();
		return from(java.util.stream.DoubleStream.of(values).iterator());
	}

	/**
	 * Returns a stream of iterable values.
	 */
	public static <E extends Exception> DoubleStream<E> from(Iterable<Double> iterable) {
		return from(iterable.iterator());
	}

	/**
	 * Returns a stream of iterator values.
	 */
	public static <E extends Exception> DoubleStream<E> from(Iterator<Double> iterator) {
		if (iterator == null || !iterator.hasNext()) return empty();
		return DoubleStream.of(Stream.from(iterator));
	}

	static <E extends Exception> DoubleStream<E> of(Stream<E, Double> stream) {
		return new DoubleStream<>(stream);
	}

	private DoubleStream(Stream<E, Double> stream) {
		this.stream = stream;
	}

	/**
	 * Only streams elements that match the filter.
	 */
	public DoubleStream<E> filter(Excepts.DoublePredicate<E> filter) {
		Objects.requireNonNull(filter);
		stream.filter(filter::test);
		return this;
	}

	/**
	 * Return stream of boxed ints.
	 */
	public Stream<E, Double> boxed() {
		return stream;
	}

	/**
	 * Maps stream elements to new values.
	 */
	public DoubleStream<E> map(Excepts.DoubleOperator<E> mapper) {
		Objects.requireNonNull(mapper);
		stream.map(mapper::applyAsDouble);
		return this;
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(Excepts.DoubleFunction<E, T> mapper) {
		Objects.requireNonNull(mapper);
		return stream.map(mapper::apply);
	}

	/**
	 * Maps each element to a stream, and flattens the streams.
	 */
	public DoubleStream<E>
		flatMap(Excepts.DoubleFunction<? extends E, DoubleStream<? extends E>> mapper) {
		Objects.requireNonNull(mapper);
		stream.flatMap(t -> FunctionUtil.safeApply(mapper.apply(t), s -> s.stream));
		return this;
	}

	/**
	 * Limits the number of elements.
	 */
	public DoubleStream<E> limit(long size) {
		stream.limit(size);
		return this;
	}

	/**
	 * Streams distinct elements, by first collecting into a linked set.
	 */
	public DoubleStream<E> distinct() throws E {
		stream.distinct();
		return this;
	}

	/**
	 * Streams sorted elements, by first collecting into a sorted list.
	 */
	public DoubleStream<E> sorted() throws E {
		stream.sorted(Comparator.naturalOrder());
		return this;
	}

	/**
	 * Iterates elements with a consumer.
	 */
	public void forEach(Excepts.DoubleConsumer<E> action) throws E {
		Objects.requireNonNull(action);
		stream.forEach(action::accept);
	}

	/**
	 * Collects elements into an array.
	 */
	public double[] toArray() throws E {
		var array = DynamicArray.doubles();
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
	public Double min() throws E {
		return stream.min(Comparator.naturalOrder());
	}

	/**
	 * Returns the minimum value or default.
	 */
	public double min(double def) throws E {
		return BasicUtil.defDouble(min(), def);
	}

	/**
	 * Returns the minimum value or null.
	 */
	public Double max() throws E {
		return stream.max(Comparator.naturalOrder());
	}

	/**
	 * Returns the maximum value or default.
	 */
	public double max(double def) throws E {
		return BasicUtil.defDouble(max(), def);
	}

	/**
	 * Reduces stream to an element or null, using an accumulator.
	 */
	public Double reduce(Excepts.DoubleBiOperator<E> accumulator) throws E {
		return stream.reduce(accumulator::applyAsDouble);
	}

	/**
	 * Reduces stream to an element, using an identity and accumulator.
	 */
	public double reduce(double identity, Excepts.DoubleBiOperator<E> accumulator) throws E {
		return stream.reduce(identity, accumulator::applyAsDouble);
	}

	/**
	 * Returns true if any element matches.
	 */
	public boolean anyMatch(Excepts.DoublePredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.anyMatch(predicate::test);
	}

	/**
	 * Returns true if all elements match.
	 */
	public boolean allMatch(Excepts.DoublePredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.allMatch(predicate::test);
	}

	/**
	 * Returns true if no elements match.
	 */
	public boolean noneMatch(Excepts.DoublePredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.noneMatch(predicate::test);
	}

	/**
	 * Returns the next element or null.
	 */
	public Double next() throws E {
		return stream.next();
	}

	/**
	 * Returns the next element or default.
	 */
	public double next(double def) throws E {
		return stream.next(def);
	}
}
