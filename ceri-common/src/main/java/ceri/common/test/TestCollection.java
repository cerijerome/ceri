package ceri.common.test;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import ceri.common.collect.Iterators;
import ceri.common.collect.Lists;
import ceri.common.util.Basics;

/**
 * Collection implementations to assist testing.
 */
public class TestCollection {
	private TestCollection() {}

	/**
	 * An iterable wrapper that returns a test iterator.
	 */
	public interface Iterable<T> extends java.lang.Iterable<T> {
		@Override
		Iterator<T> iterator();
	}

	private static class BaseIterator<I extends java.util.Iterator<T>, T>
		implements java.util.Iterator<T> {
		public final I iterator;
		public final ErrorGen hasNext = ErrorGen.of();
		public final ErrorGen next = ErrorGen.of();
		public final ErrorGen remove = ErrorGen.of();

		protected BaseIterator(I iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			hasNext.call();
			return iterator.hasNext();
		}

		@Override
		public T next() {
			next.call();
			return iterator.next();
		}

		@Override
		public void remove() {
			remove.call();
			iterator.remove();
		}
	}

	/**
	 * An iterator wrapper that allows generated exceptions.
	 */
	public static class Iterator<T> extends BaseIterator<java.util.Iterator<T>, T> {
		protected Iterator(java.util.Iterator<T> iterator) {
			super(Basics.def(iterator, Iterators.ofNull()));
		}
	}

	/**
	 * An iterator wrapper that allows generated exceptions.
	 */
	public static class IntIterator extends BaseIterator<PrimitiveIterator.OfInt, Integer>
		implements PrimitiveIterator.OfInt {

		protected IntIterator(PrimitiveIterator.OfInt iterator) {
			super(Basics.def(iterator, Iterators.nullInt));
		}

		@Override
		public int nextInt() {
			next.call();
			return iterator.nextInt();
		}
	}

	/**
	 * An iterator wrapper that allows generated exceptions.
	 */
	public static class LongIterator extends BaseIterator<PrimitiveIterator.OfLong, Long>
		implements PrimitiveIterator.OfLong {

		protected LongIterator(PrimitiveIterator.OfLong iterator) {
			super(Basics.def(iterator, Iterators.nullLong));
		}

		@Override
		public long nextLong() {
			next.call();
			return iterator.nextLong();
		}
	}

	/**
	 * An iterator wrapper that allows generated exceptions.
	 */
	public static class DoubleIterator extends BaseIterator<PrimitiveIterator.OfDouble, Double>
		implements PrimitiveIterator.OfDouble {

		protected DoubleIterator(PrimitiveIterator.OfDouble iterator) {
			super(Basics.def(iterator, Iterators.nullDouble));
		}

		@Override
		public double nextDouble() {
			next.call();
			return iterator.nextDouble();
		}
	}

	private static abstract class BaseSpliterator<S extends java.util.Spliterator<T>, T>
		implements java.util.Spliterator<T> {
		public final S spliterator;
		public final ErrorGen tryAdvance = ErrorGen.of();
		public final ErrorGen trySplit = ErrorGen.of();

		protected BaseSpliterator(S spliterator) {
			this.spliterator = spliterator;
		}

		@Override
		public int characteristics() {
			return spliterator.characteristics();
		}

		@Override
		public long estimateSize() {
			return spliterator.estimateSize();
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			tryAdvance.call();
			return spliterator.tryAdvance(action);
		}
	}

	/**
	 * A spliterator wrapper that allows generated exceptions.
	 */
	public static class Spliterator<T> extends BaseSpliterator<java.util.Spliterator<T>, T> {
		protected Spliterator(java.util.Spliterator<T> spliterator) {
			super(Basics.def(spliterator, Spliterators.emptySpliterator()));
		}

		@Override
		public Spliterator<T> trySplit() {
			trySplit.call();
			var split = spliterator.trySplit();
			return split == null ? null : new Spliterator<>(split);
		}
	}

	/**
	 * A spliterator wrapper that allows generated exceptions.
	 */
	public static class IntSpliterator extends BaseSpliterator<java.util.Spliterator.OfInt, Integer>
		implements Spliterator.OfInt {
		protected IntSpliterator(java.util.Spliterator.OfInt spliterator) {
			super(Basics.def(spliterator, Spliterators.emptyIntSpliterator()));
		}

		@Override
		public boolean tryAdvance(IntConsumer action) {
			tryAdvance.call();
			return spliterator.tryAdvance(action);
		}

		@Override
		public IntSpliterator trySplit() {
			trySplit.call();
			var split = spliterator.trySplit();
			return split == null ? null : new IntSpliterator(split);
		}
	}

	/**
	 * A spliterator wrapper that allows generated exceptions.
	 */
	public static class LongSpliterator extends BaseSpliterator<java.util.Spliterator.OfLong, Long>
		implements Spliterator.OfLong {
		protected LongSpliterator(java.util.Spliterator.OfLong spliterator) {
			super(Basics.def(spliterator, Spliterators.emptyLongSpliterator()));
		}

		@Override
		public boolean tryAdvance(LongConsumer action) {
			tryAdvance.call();
			return spliterator.tryAdvance(action);
		}

		@Override
		public LongSpliterator trySplit() {
			trySplit.call();
			var split = spliterator.trySplit();
			return split == null ? null : new LongSpliterator(split);
		}
	}

	/**
	 * A spliterator wrapper that allows generated exceptions.
	 */
	public static class DoubleSpliterator extends
		BaseSpliterator<java.util.Spliterator.OfDouble, Double> implements Spliterator.OfDouble {
		protected DoubleSpliterator(java.util.Spliterator.OfDouble spliterator) {
			super(Basics.def(spliterator, Spliterators.emptyDoubleSpliterator()));
		}

		@Override
		public boolean tryAdvance(DoubleConsumer action) {
			tryAdvance.call();
			return spliterator.tryAdvance(action);
		}

		@Override
		public DoubleSpliterator trySplit() {
			trySplit.call();
			var split = spliterator.trySplit();
			return split == null ? null : new DoubleSpliterator(split);
		}
	}

	/**
	 * Returns a test iterable for the value sequence.
	 */
	@SafeVarargs
	public static <T> Iterable<T> iterableOf(T... values) {
		return iterable(Lists.ofAll(values));
	}

	/**
	 * Returns a test iterable from the iterable type.
	 */
	public static <T> Iterable<T> iterable(java.lang.Iterable<T> iterable) {
		return () -> iterator(iterable);
	}

	/**
	 * Returns a test iterable from the iterator.
	 */
	public static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
		return () -> iterator(iterator);
	}

	/**
	 * Returns a test iterator for the value sequence.
	 */
	@SafeVarargs
	public static <T> Iterator<T> iteratorOf(T... values) {
		return iterator(Lists.ofAll(values));
	}

	/**
	 * Returns a test iterator from the iterable type.
	 */
	public static <T> Iterator<T> iterator(java.lang.Iterable<T> iterable) {
		return iterator(iterable.iterator());
	}

	/**
	 * Returns a test iterator from the given iterator.
	 */
	public static <T> Iterator<T> iterator(java.util.Iterator<T> iterator) {
		return new Iterator<>(Basics.def(iterator, Iterators.ofNull()));
	}

	/**
	 * Returns a test iterator for the value sequence.
	 */
	@SafeVarargs
	public static IntIterator intIterator(int... values) {
		return new IntIterator(Arrays.stream(values).iterator());
	}

	/**
	 * Returns a test iterator for the value sequence.
	 */
	@SafeVarargs
	public static LongIterator longIterator(long... values) {
		return new LongIterator(Arrays.stream(values).iterator());
	}

	/**
	 * Returns a test iterator for the value sequence.
	 */
	@SafeVarargs
	public static DoubleIterator doubleIterator(double... values) {
		return new DoubleIterator(Arrays.stream(values).iterator());
	}

	/**
	 * Returns a test spliterator for the value sequence.
	 */
	@SafeVarargs
	public static <T> Spliterator<T> spliteratorOf(T... values) {
		return spliterator(Lists.ofAll(values));
	}

	/**
	 * Returns a test spliterator from the iterable type.
	 */
	public static <T> Spliterator<T> spliterator(java.lang.Iterable<T> iterable) {
		return spliterator(iterable.spliterator());
	}

	/**
	 * Returns a test spliterator from the given spliterator.
	 */
	public static <T> Spliterator<T> spliterator(java.util.Spliterator<T> spliterator) {
		return new Spliterator<>(spliterator);
	}

	/**
	 * Returns a test spliterator for the value sequence.
	 */
	public static IntSpliterator intSpliterator(int... values) {
		return new IntSpliterator(Arrays.stream(values).spliterator());
	}

	/**
	 * Returns a test spliterator for the value sequence.
	 */
	public static LongSpliterator longSpliterator(long... values) {
		return new LongSpliterator(Arrays.stream(values).spliterator());
	}

	/**
	 * Returns a test spliterator for the value sequence.
	 */
	public static DoubleSpliterator doubleSpliterator(double... values) {
		return new DoubleSpliterator(Arrays.stream(values).spliterator());
	}
}
