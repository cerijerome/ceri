package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.validateIndex;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import ceri.common.util.BasicUtil;

public class EnumUtil {
	private static final Map<Class<?>, List<?>> cache = new ConcurrentHashMap<>();

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
	 * Returns enum constants as an immutable list, using a lookup cache.
	 */
	public static <T extends Enum<T>> List<T> enums(Class<T> cls) {
		return BasicUtil.uncheckedCast(
			cache.computeIfAbsent(cls, c -> ImmutableUtil.wrapAsList(c.getEnumConstants())));
	}

	/**
	 * Convenience method that returns all enum constants as a list in reverse order.
	 */
	public static <T extends Enum<T>> List<T> enumsReversed(Class<T> cls) {
		return ImmutableUtil.wrapAsList(ArrayUtil.reverse(cls.getEnumConstants()));
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

	/**
	 * Return a random enum value.
	 */
	public static <T extends Enum<T>> T random(Class<T> cls) {
		var enums = enums(cls);
		return enums.get(ThreadLocalRandom.current().nextInt(enums.size()));
	}
}
