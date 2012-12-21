package ceri.common.comparator;

import java.util.Comparator;
import ceri.common.util.BasicUtil;

public class EnumComparators {

	private static final Comparator<Enum<?>> BY_NAME = new BaseComparator<Enum<?>>() {
		@Override
		protected int compareNonNull(Enum<?> o1, Enum<?> o2) {
			return Comparators.STRING.compare(o1.name(), o2.name());
		}
	};

	public static <T extends Enum<T>> Comparator<T> byName() {
		return BasicUtil.uncheckedCast(BY_NAME);
	}

	public static <T extends Enum<T>> Comparator<T> byOrdinal() {
		return Comparators.byComparable();
	}

}
