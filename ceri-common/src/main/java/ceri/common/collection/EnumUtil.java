package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.validateIndex;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class EnumUtil {

	private EnumUtil() {}

	/**
	 * Convenience method that calls Enum.valueOf and returns null if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value) {
		return valueOf(cls, value, null);
	}

	/**
	 * Convenience method that calls Enum.valueOf and returns default value if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value, T def) {
		if (value == null || cls == null) return def;
		try {
			return Enum.valueOf(cls, value);
		} catch (IllegalArgumentException e) {
			return def;
		}
	}

	/**
	 * Finds the first enum matching the filter; returns null if no match.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter) {
		return find(cls, filter, null);
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter, T def) {
		return enums(cls).stream().filter(filter).findFirst().orElse(def);
	}

	/**
	 * Convenience method that returns all enum constants as a list.
	 */
	public static <T extends Enum<T>> List<T> enums(Class<T> cls) {
		return Arrays.asList(cls.getEnumConstants());
	}

	/**
	 * Convenience method that returns all enum constants as a list in reverse order.
	 */
	public static <T extends Enum<T>> List<T> enumsReversed(Class<T> cls) {
		List<T> list = enums(cls);
		Collections.reverse(list);
		return list;
	}

	/**
	 * Look up enum from ordinal value. Returns null if out of ordinal range.
	 */
	public static <T extends Enum<T>> T fromOrdinal(Class<T> cls, int ordinal) {
		T[] enums = cls.getEnumConstants();
		if (ordinal < 0 || ordinal >= enums.length) return null;
		return enums[ordinal];
	}

	/**
	 * Look up enum from ordinal value. Throws exception if out of ordinal range.
	 */
	public static <T extends Enum<T>> T fromOrdinalValid(Class<T> cls, int ordinal) {
		T[] enums = cls.getEnumConstants();
		validateIndex(enums.length, ordinal);
		return enums[ordinal];
	}
}
