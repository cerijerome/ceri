package ceri.common.collection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
	public static <K, V> Map<K, V> toMap(Stream<V> stream, Function<V, K> keyFn) {
		return stream.collect(Collectors.toMap(keyFn, Function.identity(), mergeError(),
			LinkedHashMap::new));
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
			throw new IllegalArgumentException();
		};
	}

}
