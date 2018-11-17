package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;

/**
 * Convenience shortcuts for common stream methods.
 */
public class StreamUtil {
	private static final IntBinaryOperator intBitwiseAnd = (lhs, rhs) -> lhs & rhs;
	private static final IntBinaryOperator intBitwiseOr = (lhs, rhs) -> lhs | rhs;
	private static final IntBinaryOperator intBitwiseXor = (lhs, rhs) -> lhs ^ rhs;
	
	private StreamUtil() {}

	public static IntStream toInt(Stream<? extends Number> stream) {
		return stream.mapToInt(Number::intValue);
	}
	
	public static int bitwiseAnd(IntStream stream) {
		return stream.reduce(intBitwiseAnd).orElse(0);
	}
	
	public static int bitwiseOr(IntStream stream) {
		return stream.reduce(0, intBitwiseOr);
	}
	
	public static int bitwiseXor(IntStream stream) {
		return stream.reduce(0, intBitwiseXor);
	}
	
	/**
	 * Filters objects of given type and casts the stream.
	 */
	public static <T> Stream<T> castAny(Stream<?> stream, Class<T> cls) {
		return stream.map(obj -> BasicUtil.castOrNull(cls, obj)).filter(Objects::nonNull);
	}

	/**
	 * Collects a stream of int code points into a string.
	 */
	public static String toString(IntStream codePointStream) {
		return codePointStream.collect(StringBuilder::new, StringBuilder::appendCodePoint, //
			StringBuilder::append).toString();
	}

	/**
	 * Returns the first matching non-null entry in the stream, or null if no match.
	 */
	public static <T> T findFirstNonNull(Stream<T> stream, Predicate<? super T> predicate) {
		return first(stream.filter(Objects::nonNull).filter(predicate));
	}

	/**
	 * Returns the first non-null entry in the stream, or null.
	 */
	public static <T> T firstNonNull(Stream<T> stream) {
		return first(stream.filter(Objects::nonNull));
	}

	/**
	 * Returns the first entry in the stream, or null if empty.
	 */
	public static <T> T first(Stream<T> stream) {
		return stream.findFirst().orElse(null);
	}

	/**
	 * Returns the max entry in the stream, or null if empty.
	 */
	public static <T extends Comparable<T>> T max(Stream<T> stream) {
		return max(stream, Comparators.comparable());
	}

	/**
	 * Returns the max entry in the stream, or null if empty.
	 */
	public static <T> T max(Stream<T> stream, Comparator<? super T> comparator) {
		return stream.max(comparator).orElse(null);
	}

	/**
	 * Returns the min entry in the stream, or null if empty.
	 */
	public static <T extends Comparable<T>> T min(Stream<T> stream) {
		return min(stream, Comparators.comparable());
	}

	/**
	 * Returns the min entry in the stream, or null if empty.
	 */
	public static <T> T min(Stream<T> stream, Comparator<? super T> comparator) {
		return stream.min(comparator).orElse(null);
	}

	/**
	 * Convert a map to an object stream.
	 */
	public static <K, V, T> Stream<T> stream(Map<K, V> map,
		BiFunction<? super K, ? super V, ? extends T> mapFn) {
		Function<Map.Entry<K, V>, T> fn = (entry -> mapFn.apply(entry.getKey(), entry.getValue()));
		return map.entrySet().stream().map(fn);
	}

	/**
	 * Join streams of collections.
	 */
	public static <T> Set<T> joinToSet(Stream<? extends Collection<? extends T>> stream) {
		return joinToSet(stream, LinkedHashSet::new);
	}

	/**
	 * Join streams of collections.
	 */
	public static <T> Set<T> joinToSet(Stream<? extends Collection<? extends T>> stream,
		Supplier<Set<T>> supplier) {
		return stream.collect(supplier, Set::addAll, Set::addAll);
	}

	/**
	 * Join streams of collections.
	 */
	public static <T> List<T> joinToList(Stream<? extends Collection<? extends T>> stream) {
		return joinToList(stream, ArrayList::new);
	}

	/**
	 * Join streams of collections.
	 */
	public static <T> List<T> joinToList(Stream<? extends Collection<? extends T>> stream,
		Supplier<List<T>> supplier) {
		return stream.collect(supplier, List::addAll, List::addAll);
	}

	/**
	 * Convert a stream to a LinkedHashSet.
	 */
	public static <T> Set<T> toSet(Stream<T> stream) {
		return toSet(stream, LinkedHashSet::new);
	}

	/**
	 * Convert a stream to a LinkedHashSet.
	 */
	public static <T> Set<T> toSet(Stream<? extends T> stream, Supplier<Set<T>> supplier) {
		return stream.collect(Collectors.toCollection(supplier));
	}

	/**
	 * Convert a stream to a list.
	 */
	public static <T> List<T> toList(Stream<T> stream) {
		return stream.collect(Collectors.toList());
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V> Map<K, V> toMap(Stream<V> stream, Function<? super V, ? extends K> keyFn) {
		return toMap(stream, keyFn, (Supplier<Map<K, V>>) LinkedHashMap::new);
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V, T> Map<K, V> toMap(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return toMap(stream, keyFn, valueFn, LinkedHashMap::new);
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V> Map<K, V> toMap(Stream<V> stream, Function<? super V, ? extends K> keyFn,
		Supplier<Map<K, V>> mapSupplier) {
		return toMap(stream, keyFn, Function.identity(), mapSupplier);
	}

	/**
	 * Convert a stream to a map and don't allow duplicate keys.
	 */
	public static <K, V, T> Map<K, V> toMap(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, V>> mapSupplier) {
		return stream.collect(mapSupplier, (m, t) -> m.put(keyFn.apply(t), valueFn.apply(t)),
			Map::putAll);
	}

	/**
	 * Convert a map entry stream back to a map.
	 */
	public static <K, V> Map<K, V> toEntryMap(Stream<Map.Entry<K, V>> stream) {
		return toEntryMap(stream, LinkedHashMap::new);
	}

	/**
	 * Convert a map entry stream back to a map.
	 */
	public static <K, V> Map<K, V> toEntryMap(Stream<Map.Entry<K, V>> stream,
		Supplier<Map<K, V>> mapSupplier) {
		return stream.collect(entryCollector(mergeSecond(), mapSupplier));
	}

	/**
	 * Collector for map entry back to map.
	 */
	public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryCollector() {
		return entryCollector(mergeError(), LinkedHashMap::new);
	}

	/**
	 * Collector for map entry back to map.
	 */
	public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
		entryCollector(BinaryOperator<V> mergeFn, Supplier<Map<K, V>> mapSupplier) {
		return Collectors.toMap(e -> e.getKey(), e -> e.getValue(), mergeFn, mapSupplier);
	}

	/**
	 * When merging keys, only keep the original key.
	 */
	public static <T> BinaryOperator<T> mergeFirst() {
		return (first, second) -> first;
	}

	/**
	 * When merging keys, only keep the new key.
	 */
	public static <T> BinaryOperator<T> mergeSecond() {
		return (first, second) -> second;
	}

	/**
	 * Throw an IllegalArgumentException for duplicate keys.
	 */
	public static <T> BinaryOperator<T> mergeError() {
		return (first, second) -> {
			throw new IllegalArgumentException("Duplicate keys: " + first + ", " + second);
		};
	}

	/**
	 * Stream subarray. The case missing from Arrays.stream
	 */
	public static <T> Stream<T> stream(T[] array, int offset) {
		return Arrays.stream(array, offset, array.length);
	}

	/**
	 * Returns a stream for all enum values.
	 */
	public static <T extends Enum<T>> Stream<T> stream(Class<T> enumCls) {
		return BasicUtil.enums(enumCls).stream();
	}
	
	/**
	 * Returns a stream for an Enumeration.
	 */
	public static <T> Stream<T> stream(Enumeration<T> e) {
		return StreamSupport.stream(new EnumerationSpliterator<>(e), false);
	}

	private static class EnumerationSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
		private final Enumeration<T> e;

		public EnumerationSpliterator(Enumeration<T> e) {
			super(Long.MAX_VALUE, Spliterator.ORDERED);
			this.e = e;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (!e.hasMoreElements()) return false;
			action.accept(e.nextElement());
			return true;
		}

		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			while (e.hasMoreElements())
				action.accept(e.nextElement());
		}
	}

}
