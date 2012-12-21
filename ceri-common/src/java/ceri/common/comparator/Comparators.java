/**
 * Created on Aug 25, 2007
 */
package ceri.common.comparator;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import ceri.common.util.BasicUtil;

/**
 * Comparators for primitives and other objects, handling null cases.
 */
public class Comparators {
	private static final Comparator<Comparable<Comparable<?>>> COMPARABLE =
		new ComparableComparator<>();
	public static final Comparator<Double> DOUBLE = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Float> FLOAT = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Byte> BYTE = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Short> SHORT = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Integer> INTEGER = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Long> LONG = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Boolean> BOOLEAN = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Character> CHARACTER = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<String> STRING = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Date> DATE = BasicUtil.uncheckedCast(COMPARABLE);
	public static final Comparator<Locale> LOCALE = byString();

	private static final Comparator<?> BY_STRING = new BaseComparator<Object>() {
		@Override
		protected int compareNonNull(Object lhs, Object rhs) {
			return STRING.compare(String.valueOf(lhs), String.valueOf(rhs));
		}
	};

	private static final Comparator<?> NULL = new Comparator<Object>() {
		@Override
		public int compare(Object lhs, Object rhs) {
			BasicUtil.unused(lhs, rhs);
			return 0;
		}
	};

	private static final Comparator<?> NON_NULL = new BaseComparator<Object>() {
		@Override
		protected int compareNonNull(Object lhs, Object rhs) {
			BasicUtil.unused(lhs, rhs);
			return 0;
		}
	};

	private Comparators() {}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> byComparable() {
		return BasicUtil.<Comparator<T>>uncheckedCast(COMPARABLE);
	}

	/**
	 * Comparator for string representations of objects.
	 */
	public static <T> Comparator<T> byString() {
		return BasicUtil.<Comparator<T>>uncheckedCast(BY_STRING);
	}

	/**
	 * Null comparator treats everything as equal.
	 */
	public static <T> Comparator<T> nullComparator() {
		return BasicUtil.<Comparator<T>>uncheckedCast(NULL);
	}

	/**
	 * Non-null comparator treats null as inferior, everything else equal.
	 */
	public static <T> Comparator<T> nonNullComparator() {
		return BasicUtil.<Comparator<T>>uncheckedCast(NON_NULL);
	}

}
