package ceri.common.stream;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.collection.CollectionSupplier;
import ceri.common.util.BasicUtil;

/**
 * Convenience shortcuts for common java stream methods.
 */
public class StreamUtil {
	private static final CollectionSupplier supplier = CollectionSupplier.DEFAULT;
	private static final BinaryOperator<?> MERGE_FIRST = (first, _) -> first;
	private static final BinaryOperator<?> MERGE_SECOND = (_, second) -> second;
	private static final BinaryOperator<?> MERGE_ERROR = (first, second) -> {
		throw new IllegalArgumentException("Duplicate keys: " + first + ", " + second);
	};
	private static final BiConsumer<?, ?> BAD_COMBINER = (_, _) -> {
		throw new IllegalStateException();
	};

	private StreamUtil() {}

	/**
	 * Use when a combiner is required for a Stream method, but should not be invoked.
	 */
	public static <T> BiConsumer<T, T> badCombiner() {
		return BasicUtil.unchecked(BAD_COMBINER);
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
		return toMap(stream, keyFn, supplier.map());
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V, T> Map<K, V> toMap(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return toMap(stream, keyFn, valueFn, supplier.map());
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
		return BasicUtil.unchecked(MERGE_FIRST);
	}

	/**
	 * When merging keys, only keep the new key.
	 */
	public static <T> BinaryOperator<T> mergeSecond() {
		return BasicUtil.unchecked(MERGE_SECOND);
	}

	/**
	 * Throw an IllegalArgumentException for duplicate keys.
	 */
	public static <T> BinaryOperator<T> mergeError() {
		return BasicUtil.unchecked(MERGE_ERROR);
	}

	/**
	 * Merge keys first, second, or throw IllegalArgumentException.
	 */
	public static <T> BinaryOperator<T> merge(Boolean first) {
		if (first == null) return mergeError();
		return first ? mergeFirst() : mergeSecond();
	}
}
