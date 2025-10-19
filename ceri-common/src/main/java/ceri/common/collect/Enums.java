package ceri.common.collect;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;

/**
 * Enum type support.
 */
public class Enums {
	private static final String VALUE_FIELD_NAME_DEF = "value";
	private static final Map<Class<?>, List<?>> cache = Maps.concurrent();
	private static final Map<Class<?>, Integer> namePrefixLens = Maps.concurrent();

	private Enums() {}

	/**
	 * Enum comparators.
	 */
	public static class Compare {
		/** Name comparator. */
		public static final Comparator<Enum<?>> name =
			Comparator.nullsFirst(Comparator.comparing(Enums::name));
		/** Ordinal comparator. */
		public static final Comparator<Enum<?>> ordinal =
			Comparator.nullsFirst(Comparator.comparing(Enum::ordinal));

		private Compare() {}

		/**
		 * Typed name comparator, with nulls first.
		 */
		public static <T extends Enum<T>> Comparator<T> name() {
			return Reflect.unchecked(name);
		}

		/**
		 * Typed applied name comparator, with nulls first.
		 */
		public static <T extends Enum<T>> Comparator<T> name(Comparator<String> nameComparator) {
			return Comparator.nullsFirst(Comparator.comparing(Enums::name, nameComparator));
		}

		/**
		 * Typed ordinal comparator, with nulls first.
		 */
		public static <T extends Enum<T>> Comparator<T> ordinal() {
			return Reflect.unchecked(ordinal);
		}
	}

	/**
	 * Enum filters.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Predicate to match enum name.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Enum<?>> name(String name) {
			return name(Filters.equal(name));
		}

		/**
		 * Predicate applied to enum name.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Enum<?>>
			name(Excepts.Predicate<? extends E, ? super String> predicate) {
			return Filters.as(Enum::name, predicate);
		}
	}

	/**
	 * Returns enum constants as an immutable list, using a lookup cache.
	 */
	public static <T> List<T> of(Class<T> cls) {
		if (cls == null) return List.of();
		return Reflect.unchecked(
			Maps.lazyCompute(cache, cls, _ -> Immutable.wrapListOf(cls.getEnumConstants())));
	}

	/**
	 * Create an immutable enum set.
	 */
	public static <T extends Enum<T>> Set<T> set(Class<T> cls) {
		var enums = of(cls);
		return enums.isEmpty() ? Set.of() : Immutable.wrap(EnumSet.copyOf(enums));
	}

	/**
	 * Create an immutable set from values.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> Set<T> set(T... values) {
		if (values == null || values.length == 0) return Set.of();
		for (var value : values)
			if (value == null) return Sets.ofAll(values);
		var set = EnumSet.of(values[0]);
		for (int i = 1; i < values.length; i++)
			set.add(values[i]);
		return Immutable.wrap(set);
	}

	/**
	 * Returns the enum name, or null.
	 */
	public static String name(Enum<?> en) {
		return en == null ? null : en.name();
	}

	/**
	 * Returns the enum name with common prefix removed, up to the last word boundary.
	 */
	public static String shortName(Enum<?> en) {
		if (en == null) return Strings.NULL;
		var cls = Reflect.getClass(en);
		int index = namePrefixLens.computeIfAbsent(cls, _ -> prefixLen(of(cls)));
		return en.name().substring(index);
	}

	/**
	 * Returns a function that provides an enum value from its field name.
	 */
	public static <T extends Enum<T>> Functions.Function<T, Long> valueAccessor(Class<T> cls) {
		return valueAccessor(cls, "");
	}

	/**
	 * Returns a function that provides an enum value from its field name.
	 */
	public static <T extends Enum<T>> Functions.Function<T, Long> valueAccessor(Class<T> cls,
		String valueFieldName) {
		var field = valueField(cls, valueFieldName);
		return t -> Maths.unsigned(Reflect.publicFieldValue(t, field, null));
	}

	/**
	 * Returns the enum class type.
	 */
	public static <T extends Enum<T>> Class<T> type(T en) {
		if (en == null) return null;
		return Reflect.unchecked(en.getClass());
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
	 * is determined by the default value.
	 */
	public static <T extends Enum<T>> T valueOf(String value, T def) {
		return valueOf(type(def), value, def);
	}

	/**
	 * Return a random enum value.
	 */
	public static <T> T random(Class<T> cls) {
		var enums = of(cls);
		return enums.get(ThreadLocalRandom.current().nextInt(enums.size()));
	}

	/**
	 * Finds the first enum matching the filter; returns null if no match.
	 */
	public static <E extends Exception, T> T find(Class<T> cls,
		Excepts.Predicate<? extends E, ? super T> filter) throws E {
		return find(cls, filter, null);
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <E extends Exception, T> T find(Class<T> cls,
		Excepts.Predicate<? extends E, ? super T> filter, T def) throws E {
		return Stream.<E, T>from(of(cls)).filter(filter).next(def);
	}

	/**
	 * Returns a stream of enum types in natural order.
	 */
	public static <T extends Enum<T>> Stream<RuntimeException, T> stream(Class<T> cls) {
		return Streams.from(of(cls));
	}

	/**
	 * Create an immutable lookup map by mapping each enum to a key.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T>
		map(Excepts.Function<? extends E, T, K> keyMapper, Class<T> cls) throws E {
		return map(Maps.Put.def, keyMapper, cls);
	}

	/**
	 * Create an immutable lookup map by mapping each enum to a key.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T> map(Maps.Put put,
		Excepts.Function<? extends E, T, K> keyMapper, Class<T> cls) throws E {
		return Immutable.wrap(Maps.convertPut(put, Maps.of(), keyMapper, t -> t, of(cls)));
	}

	/**
	 * Creates an immutable map by providing a collection for each enum and mapping each element of
	 * the collection back to the enum.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T>
		inverseMap(Excepts.Function<E, T, Iterable<K>> mapper, Class<T> cls) throws E {
		return Immutable.wrap(Maps.expandKeyPut(Maps.of(), mapper, t -> t, of(cls)));
	}

	// support methods

	private static <T extends Enum<T>> Field valueField(Class<T> cls, String fieldName) {
		if (!Reflect.isPublic(cls))
			throw Exceptions.illegalArg("Class is not public: ", Reflect.name(cls));
		if (Strings.isEmpty(fieldName)) fieldName = VALUE_FIELD_NAME_DEF;
		var field = Reflect.publicField(cls, fieldName);
		if (field != null && Reflect.isNumber(field.getType())) return field;
		if (field == null)
			throw Exceptions.illegalArg("No such field: %s.%s", cls.getSimpleName(), fieldName);
		throw Exceptions.illegalArg("Unsupported type for %s.%s: %s", cls.getSimpleName(),
			field.getName(), field.getType().getSimpleName());
	}

	private static Integer prefixLen(List<? extends Enum<?>> enums) {
		int min = Streams.from(enums).mapToInt(e -> e.name().length()).min(0);
		int i = commonPrefixLen(min, enums);
		if (i == min) i--;
		var name = enums.get(0).name();
		for (; i > 0; i--)
			if (Strings.isNameBoundary(name, i)) return i;
		return i;
	}

	private static int commonPrefixLen(int min, Iterable<? extends Enum<?>> enums) {
		for (int i = 0; i < min; i++) {
			int c = -1;
			for (var en : enums)
				if (c == -1) c = en.name().charAt(i);
				else if (c != en.name().charAt(i)) return i;
		}
		return min;
	}
}
