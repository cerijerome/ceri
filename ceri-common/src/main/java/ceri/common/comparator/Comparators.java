package ceri.common.comparator;

import java.util.Arrays;
import java.util.Collection;
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
	public static final Comparator<Locale> LOCALE = string();

	/**
	 * Comparator for comparable types that handles null cases.
	 */
	private static class ComparableComparator<T extends Comparable<? super T>> extends
		BaseComparator<T> {
		ComparableComparator() {}

		@Override
		protected int compareNonNull(T o1, T o2) {
			return o1.compareTo(o2);
		}
	}

	private static final Comparator<?> STRING_VALUE = new BaseComparator<Object>() {
		@Override
		protected int compareNonNull(Object lhs, Object rhs) {
			return STRING.compare(String.valueOf(lhs), String.valueOf(rhs));
		}
	};

	private static final Comparator<?> NULL = new Comparator<Object>() {
		@Override
		public int compare(Object lhs, Object rhs) {
			return 0;
		}
	};

	private static final Comparator<?> NON_NULL = new BaseComparator<Object>() {
		@Override
		protected int compareNonNull(Object lhs, Object rhs) {
			return 0;
		}
	};

	private Comparators() {}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> comparable() {
		return BasicUtil.<Comparator<T>>uncheckedCast(COMPARABLE);
	}

	/**
	 * Comparator for string representations of objects.
	 */
	public static <T> Comparator<T> string() {
		return BasicUtil.<Comparator<T>>uncheckedCast(STRING_VALUE);
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

	/**
	 * Create a comparator the checks comparators in sequence.
	 */
	@SafeVarargs
	public static <T> Comparator<T> sequence(Comparator<? super T>... comparators) {
		return sequence(Arrays.asList(comparators));
	}

	/**
	 * Create a comparator the checks comparators in sequence.
	 */
	public static <T> Comparator<T>
		sequence(Collection<? extends Comparator<? super T>> comparators) {
		return ComparatorSequence.<T>builder().add(comparators).build();
	}

	/**
	 * Comparator to group given items first, then apply the comparator.
	 */
	@SafeVarargs
	public static <T> Comparator<T> group(Comparator<? super T> comparator, T... ts) {
		return group(comparator, Arrays.asList(ts));
	}

	/**
	 * Comparator to group given items first.
	 */
	public static <T> Comparator<T> group(final Comparator<? super T> comparator,
		final Collection<T> ts) {
		return new BaseComparator<T>() {
			@Override
			protected int compareNonNull(T lhs, T rhs) {
				boolean lhsEq = ts.contains(lhs);
				boolean rhsEq = ts.contains(rhs);
				if (lhsEq && rhsEq) return comparator.compare(lhs, rhs);
				if (lhsEq) return -1;
				if (rhsEq) return 1;
				return comparator.compare(lhs, rhs);
			}
		};
	}

	/**
	 * Reverses a given comparator.
	 */
	public static <T> Comparator<T> reverse(final Comparator<T> comparator) {
		return new Comparator<T>() {
			@Override
			public int compare(T lhs, T rhs) {
				return -comparator.compare(lhs, rhs);
			}
		};
	}

}
