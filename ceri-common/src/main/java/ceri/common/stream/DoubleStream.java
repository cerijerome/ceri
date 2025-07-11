package ceri.common.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import ceri.common.function.Excepts.DoubleConsumer;
import ceri.common.function.Excepts.DoubleFunction;
import ceri.common.function.Excepts.DoubleOperator;
import ceri.common.function.Excepts.DoublePredicate;
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
		return from(java.util.stream.DoubleStream.of(values));
	}

	/**
	 * Returns a stream from a standard stream iterator.
	 */
	public static <E extends Exception> DoubleStream<E> from(java.util.stream.DoubleStream stream) {
		return from(stream.iterator());
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
	public DoubleStream<E> filter(DoublePredicate<E> filter) {
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
	public DoubleStream<E> map(DoubleOperator<E> mapper) {
		Objects.requireNonNull(mapper);
		stream.map(mapper::applyAsDouble);
		return this;
	}

	/**
	 * Maps stream elements to a new type.
	 */
	public <T> Stream<E, T> mapToObj(DoubleFunction<E, T> mapper) {
		Objects.requireNonNull(mapper);
		return stream.map(mapper::apply);
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
	public void forEach(DoubleConsumer<E> action) throws E {
		Objects.requireNonNull(action);
		stream.forEach(action::accept);
	}

	/**
	 * Collects elements into an array.
	 */
	public double[] toArray() throws E {
		return stream.toList().stream().mapToDouble(i -> i).toArray();
	}

	/**
	 * Limits the number of elements.
	 */
	public DoubleStream<E> limit(long size) {
		stream.limit(size);
		return this;
	}

	/**
	 * Returns true if any element matches.
	 */
	public boolean anyMatch(DoublePredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.anyMatch(predicate::test);
	}

	/**
	 * Returns true if all elements match.
	 */
	public boolean allMatch(DoublePredicate<E> predicate) throws E {
		Objects.requireNonNull(predicate);
		return stream.allMatch(predicate::test);
	}

	/**
	 * Returns true if no elements match.
	 */
	public boolean noneMatch(DoublePredicate<E> predicate) throws E {
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
