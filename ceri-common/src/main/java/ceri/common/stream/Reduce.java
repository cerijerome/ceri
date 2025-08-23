package ceri.common.stream;

import java.util.Comparator;
import ceri.common.function.Excepts;
import ceri.common.util.BasicUtil;

public class Reduce {
	private static final Excepts.BinFunction<?, ?, ?> MIN =	min(Comparator.naturalOrder());
	private static final Excepts.BinFunction<?, ?, ?> MAX =	max(Comparator.naturalOrder());
	private Reduce() {}

	public static class Ints {
		private Ints() {}
	}

	public static class Longs {
		private Longs() {}
	}

	public static class Doubles {
		private Doubles() {}
	}

	/**
	 * Comparable min.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.BinFunction<E, T, T>
		min() {
		return BasicUtil.unchecked(MIN);
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
		return BasicUtil.unchecked(MAX);
	}

	/**
	 * Comparator max.
	 */
	public static <E extends Exception, T> Excepts.BinFunction<E, T, T>
		max(Comparator<? super T> comparator) {
		return (l, r) -> comparator.compare(l, r) >= 0 ? l : r;
	}
}
