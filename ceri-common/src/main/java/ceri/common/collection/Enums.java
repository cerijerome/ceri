package ceri.common.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.stream.IntStream;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Enum type support.
 */
public class Enums {
	private static final Map<Class<?>, List<?>> cache = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Integer> namePrefixLens = new ConcurrentHashMap<>();

	private Enums() {}

	/**
	 * Enum comparators.
	 */
	public static class Comparators {
		public static final Comparator<Enum<?>> NAME =
			Comparator.nullsFirst(Comparator.comparing(Enums::name));
		public static final Comparator<Enum<?>> ORDINAL =
			Comparator.nullsFirst(Comparator.comparing(Enum::ordinal));

		private Comparators() {}

		public static <T extends Enum<T>> Comparator<T> name() {
			return BasicUtil.unchecked(NAME);
		}

		public static <T extends Enum<T>> Comparator<T> name(Comparator<String> nameComparator) {
			return Comparator.nullsFirst(Comparator.comparing(Enums::name, nameComparator));
		}

		public static <T extends Enum<T>> Comparator<T> ordinal() {
			return BasicUtil.unchecked(ORDINAL);
		}
	}

	/**
	 * Enum predicates.
	 */
	public static class Predicates {
		private Predicates() {}

		/**
		 * Predicate to match enum name.
		 */
		public static <T extends Enum<T>> Functions.Predicate<T> name(String name) {
			if (name == null) return ceri.common.function.Predicates.isNull();
			return t -> t != null && name.equals(t.name());
		}

		/**
		 * Predicate applied to enum name.
		 */
		public static <E extends Exception, T extends Enum<T>> Excepts.Predicate<E, T>
			name(Excepts.Predicate<? extends E, ? super String> predicate) {
			return ceri.common.function.Predicates.testing(Enum::name, predicate);
		}
	}

	/**
	 * Returns enum constants as an immutable list, using a lookup cache.
	 */
	public static <T> List<T> of(Class<T> cls) {
		return BasicUtil.unchecked(
			cache.computeIfAbsent(cls, c -> Immutable.wrapListOf(c.getEnumConstants())));
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
		if (en == null) return StringUtil.NULL;
		var cls = Reflect.getClass(en);
		int index = namePrefixLens.computeIfAbsent(cls, _ -> prefixLen(of(cls)));
		return en.name().substring(index);
	}

	/**
	 * Returns the enum class type.
	 */
	public static <T extends Enum<T>> Class<T> type(T en) {
		if (en == null) return null;
		return BasicUtil.<Class<T>>unchecked(en.getClass());
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
	 * Create an immutable enum set.
	 */
	public static <T extends Enum<T>> Set<T> set(Class<T> cls) {
		return Collections.unmodifiableSet(EnumSet.copyOf(of(cls)));
	}

	/**
	 * Create an immutable enum set from values.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> Set<T> set(T... values) {
		if (values == null || values.length == 0) return Set.of();
		var set = EnumSet.of(values[0]);
		for (int i = 1; i < values.length; i++)
			set.add(values[i]);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Create an immutable lookup map by mapping each enum to a key.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T>
		map(Excepts.Function<? extends E, T, K> keyMapper, Class<T> cls) throws E {
		return Immutable.convertMap(keyMapper, of(cls));
	}

	/**
	 * Create an immutable lookup map by mapping each enum to a key, if absent.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T>
		mapIfAbsent(Excepts.Function<? extends E, T, K> keyMapper, Class<T> cls) throws E {
		return Immutable.wrap(Mutable.convertPutIfAbsent(Mutable.map(), keyMapper, t -> t, of(cls)));
	}

	/**
	 * Creates an immutable map by providing a collection for each enum and mapping each element of
	 * the collection back to the enum.
	 */
	public static <E extends Exception, K, T extends Enum<T>> Map<K, T>
		inverseMap(Excepts.Function<E, T, Collection<K>> mapper, Class<T> cls) throws E {
		return Collections
			.unmodifiableMap(Iterables.inverseMap(mapper, CollectionUtil.supplier.map(), of(cls)));
	}

	// support methods

	private static Integer prefixLen(List<? extends Enum<?>> enums) {
		if (enums == null || enums.isEmpty()) return null;
		int min =
			Streams.from(enums).mapToInt(e -> e.name().length()).reduce(IntStream.Reduce.min());
		int i = commonPrefixLen(min, enums);
		if (i == min) i--;
		var name = enums.get(0).name();
		for (; i > 0; i--)
			if (isNameBoundary(name, i)) return i;
		return i;
	}

	private static boolean isNameBoundary(CharSequence s, int i) {
		char l = s.charAt(i - 1);
		char r = s.charAt(i);
		if (Character.isLetter(l) != Character.isLetter(r)) return true;
		if (Character.isDigit(l) != Character.isDigit(r)) return true;
		if (Character.isLowerCase(l) && Character.isUpperCase(r)) return true;
		return false;
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
