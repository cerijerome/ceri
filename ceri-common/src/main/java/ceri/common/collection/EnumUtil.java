package ceri.common.collection;

import static ceri.common.exception.ExceptionUtil.illegalArg;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.BasicUtil;
import ceri.common.validation.ValidationUtil;

public class EnumUtil {
	private static final Map<Class<?>, List<?>> cache = new ConcurrentHashMap<>();

	private EnumUtil() {}

	/**
	 * Throws an exception if type is not one of the given allowed types.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> T verifyAllowed(T t, T... allowed) {
		Objects.requireNonNull(t);
		for (var allow : allowed)
			if (t == allow) return t;
		throw illegalArg("%s must be one of %s: %s", ReflectUtil.className(t),
			Arrays.toString(allowed), t);
	}

	/**
	 * Throws an exception if type is null, or one of the given disallowed types.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> T verifyDisallowed(T t, T... disallowed) {
		Objects.requireNonNull(t);
		for (var disallow : disallowed)
			if (t == disallow) throw illegalArg("%s cannot be one of %s: %s",
				ReflectUtil.className(t), Arrays.toString(disallowed), t);
		return t;
	}

	/**
	 * Returns the enum class type.
	 */
	public static <T extends Enum<T>> Class<T> enumClass(T en) {
		Objects.requireNonNull(en);
		return BasicUtil.<Class<T>>uncheckedCast(en.getClass());
	}
	
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
	 * Convenience method that calls Enum.valueOf and returns default value if no match.
	 * Enum class is determined by the default value, which cannot be null. 
	 */
	public static <T extends Enum<T>> T valueOf(String value, T def) {
		Objects.requireNonNull(def);
		return valueOf(enumClass(def), value, def);
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
		ValidationUtil.validateIndex(enums.length, ordinal);
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
