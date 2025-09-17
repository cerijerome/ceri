package ceri.common.stream;

import java.util.Comparator;
import ceri.common.function.Excepts;
import ceri.common.reflect.Reflect;

public class Reduce {
	private static final Excepts.BinFunction<?, ?, ?> MIN =	min(Comparator.naturalOrder());
	private static final Excepts.BinFunction<?, ?, ?> MAX =	max(Comparator.naturalOrder());
	private Reduce() {}

	public static class Ints {
		private Ints() {}
		
		/**
		 * Minimum reduction.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> min() {
			return (l, r) -> Integer.compare(l, r) <= 0 ? l : r;
		}

		/**
		 * Maximum reduction.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> max() {
			return (l, r) -> Integer.compare(l, r) >= 0 ? l : r;
		}

		/**
		 * Summation, allowing overflow.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> sum() {
			return (l, r) -> l + r;
		}

		/**
		 * Summation, with failure for overflow.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> sumExact() {
			return (l, r) -> Math.addExact(l, r);
		}

		/**
		 * Bitwise and.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> and() {
			return (l, r) -> l & r;
		}

		/**
		 * Bitwise or.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> or() {
			return (l, r) -> l | r;
		}

		/**
		 * Bitwise xor.
		 */
		public static <E extends Exception> Excepts.IntBiOperator<E> xor() {
			return (l, r) -> l ^ r;
		}
	}

	public static class Longs {
		private Longs() {}
		
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
		 * Summation, allowing overflow.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> sum() {
			return (l, r) -> l + r;
		}

		/**
		 * Summation, with failure for overflow.
		 */
		public static <E extends Exception> Excepts.LongBiOperator<E> sumExact() {
			return (l, r) -> Math.addExact(l, r);
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

	public static class Doubles {
		private Doubles() {}
		
		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.DoubleBiOperator<E> min() {
			return (l, r) -> Double.compare(l, r) <= 0 ? l : r;
		}

		/**
		 * Comparator reduction.
		 */
		public static <E extends Exception> Excepts.DoubleBiOperator<E> max() {
			return (l, r) -> Double.compare(l, r) >= 0 ? l : r;
		}
		
		/**
		 * Summation, allowing overflow.
		 */
		public static <E extends Exception> Excepts.DoubleBiOperator<E> sum() {
			return (l, r) -> l + r;
		}
	}

	/**
	 * Comparable min.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.BinFunction<E, T, T>
		min() {
		return Reflect.unchecked(MIN);
	}

	/**
	 * Comparator min.
	 */
	public static <E extends Exception, T> Excepts.BinFunction<E, T, T>
		min(Comparator<? super T> comparator) {
		return (l, r) -> comparator.compare(l, r) <= 0 ? l : r;
	}

	/**
	 * Comparable max.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.BinFunction<E, T, T>
		max() {
		return Reflect.unchecked(MAX);
	}

	/**
	 * Comparator max.
	 */
	public static <E extends Exception, T> Excepts.BinFunction<E, T, T>
		max(Comparator<? super T> comparator) {
		return (l, r) -> comparator.compare(l, r) >= 0 ? l : r;
	}
}
