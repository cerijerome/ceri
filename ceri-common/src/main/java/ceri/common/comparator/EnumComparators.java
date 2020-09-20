package ceri.common.comparator;

import java.util.Comparator;
import ceri.common.util.BasicUtil;

/**
 * Comparators for enum types.
 */
public class EnumComparators {
	private static final Comparator<Enum<?>> NAME =
		Comparators.nonNull((lhs, rhs) -> Comparators.STRING.compare(lhs.name(), rhs.name()));

	private EnumComparators() {}

	public static <T extends Enum<T>> Comparator<T> name() {
		return BasicUtil.uncheckedCast(NAME);
	}

	public static <T extends Enum<T>> Comparator<T> ordinal() {
		return Comparators.comparable();
	}

}
