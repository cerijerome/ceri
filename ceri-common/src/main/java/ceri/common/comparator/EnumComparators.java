package ceri.common.comparator;

import java.util.Comparator;
import ceri.common.util.BasicUtil;

/**
 * Comparators for enum types.
 */
public class EnumComparators {

	private static final Comparator<Enum<?>> NAME = new BaseComparator<Enum<?>>() {
		@Override
		protected int compareNonNull(Enum<?> o1, Enum<?> o2) {
			return Comparators.STRING.compare(o1.name(), o2.name());
		}
	};

	public static <T extends Enum<T>> Comparator<T> name() {
		return BasicUtil.uncheckedCast(NAME);
	}

	public static <T extends Enum<T>> Comparator<T> ordinal() {
		return Comparators.comparable();
	}

}
