package ceri.common.collection;

import static ceri.common.exception.ExceptionUtil.illegalArg;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.validation.ValidationUtil;

public class EnumUtil {
	private static final Map<Class<?>, List<?>> cache = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Integer> namePrefixLens = new ConcurrentHashMap<>();

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
	 * Convenience method that calls Enum.valueOf and returns default value if no match. Enum class
	 * is determined by the default value, which cannot be null.
	 */
	public static <T extends Enum<T>> T valueOf(String value, T def) {
		Objects.requireNonNull(def);
		return valueOf(enumClass(def), value, def);
	}

	/**
	 * Finds the first enum matching the filter; returns null if no match.
	 */
	public static <T> T find(Class<T> cls, Predicate<T> filter) {
		return find(cls, filter, null);
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T> T find(Class<T> cls, Predicate<T> filter, T def) {
		return enums(cls).stream().filter(filter).findFirst().orElse(def);
	}

	/**
	 * Returns enum constants as an immutable list, using a lookup cache.
	 */
	public static <T> List<T> enums(Class<T> cls) {
		var list = cache.computeIfAbsent(cls, c -> {
			var enums = c.getEnumConstants();
			return enums == null ? null : ImmutableUtil.wrapAsList(c.getEnumConstants());
		});
		return list == null ? List.of() : BasicUtil.uncheckedCast(list);
	}

	/**
	 * Convenience method that returns all enum constants as a list in reverse order.
	 */
	public static <T> List<T> enumsReversed(Class<T> cls) {
		var enums = enums(cls);
		if (enums.isEmpty()) return enums;
		return ImmutableUtil.reverseAsList(enums(cls));
	}

	/**
	 * Look up enum from ordinal value. Returns null if out of ordinal range.
	 */
	public static <T> T fromOrdinal(Class<T> cls, int ordinal) {
		return CollectionUtil.getOrDefault(enums(cls), ordinal, null);
	}

	/**
	 * Look up enum from ordinal value. Throws exception if out of ordinal range.
	 */
	public static <T> T fromOrdinalValid(Class<T> cls, int ordinal) {
		var enums = enums(cls);
		ValidationUtil.validateIndex(enums.size(), ordinal);
		return enums.get(ordinal);
	}

	/**
	 * Return a random enum value.
	 */
	public static <T> T random(Class<T> cls) {
		var enums = enums(cls);
		return enums.get(ThreadLocalRandom.current().nextInt(enums.size()));
	}

	/**
	 * Returns the enum name, or null.
	 */
	public static String name(Enum<?> en) {
		return en == null ? null : en.name();
	}

	/**
	 * Returns the enum names with common prefix removed, up to last word boundary.
	 */
	public static List<String> shortNames(Enum<?>... enums) {
		return shortNames(Arrays.asList(enums));
	}

	/**
	 * Returns the enum names with common prefix removed, up to last word boundary.
	 */
	public static List<String> shortNames(Collection<? extends Enum<?>> enums) {
		return enums.stream().map(e -> shortName(e)).toList();
	}

	/**
	 * Returns the enum name with common prefix removed, up to last word boundary.
	 */
	public static String shortName(Enum<?> en) {
		if (en == null) return StringUtil.NULL_STRING;
		var cls = en.getClass();
		int index = namePrefixLens.computeIfAbsent(cls,
			_ -> prefixLen(enums(cls).stream().map(Enum::name).toList()));
		return en.name().substring(index);
	}

	// support methods

	private static int prefixLen(List<String> names) {
		int min = StringUtil.minLen(names);
		int i = StringUtil.commonPrefixLen(names);
		if (i == min) i--;
		var name = names.get(0);
		for (; i > 0; i--)
			if (StringUtil.nameBoundary(name, i)) return i;
		return i;
	}
}
