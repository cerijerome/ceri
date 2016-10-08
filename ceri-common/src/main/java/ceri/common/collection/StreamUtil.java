package ceri.common.collection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience shortcuts for common stream methods.
 */
public class StreamUtil {

	private StreamUtil() {}

	/**
	 * Returns the first entry in the stream, or null if empty.
	 */
	public static <T> T first(Stream<T> stream) {
		return stream.findFirst().orElse(null);
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
		return stream.collect(Collectors.toCollection(() -> new TreeSet<>()));
	}

	/**
	 * Convert a stream to a LinkedHashSet.
	 */
	public static <T> Set<T> toSet(Stream<T> stream) {
		return stream.collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
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
		return stream.collect(Collectors.toMap(keyFn, Function.identity(), mergeError(),
			LinkedHashMap::new));
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V, T> Map<K, V> toMap(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return stream.collect(Collectors.toMap(keyFn, valueFn, mergeError(), LinkedHashMap::new));
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
