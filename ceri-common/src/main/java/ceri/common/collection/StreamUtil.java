package ceri.common.collection;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.comparator.Comparators;

/**
 * Convenience shortcuts for common stream methods.
 */
public class StreamUtil {

	private StreamUtil() {}

	/**
	 * Collects a stream of int code points into a string.
	 */
	public static String toString(IntStream codePointStream) {
		return codePointStream.collect(StringBuilder::new, StringBuilder::appendCodePoint, //
			StringBuilder::append).toString();
	}

	/**
	 * Collects a stream from a map's key/value pairs.
	 */
	public static <K, V, T> Stream<T> map(Map<K, V> map, BiFunction<K, V, T> keyValueFn) {
		return map.entrySet().stream().map(e -> keyValueFn.apply(e.getKey(), e.getValue()));
	}

	/**
	 * Returns the first entry in the stream, or null if empty.
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
	 * Convert a stream to a TreeSet.
	 */
	public static <T> Set<T> toTreeSet(Stream<T> stream) {
		return stream.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Convert a stream to a LinkedHashSet.
	 */
	public static <T> Set<T> toSet(Stream<T> stream) {
		return stream.collect(Collectors.toCollection(LinkedHashSet::new));
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

}
