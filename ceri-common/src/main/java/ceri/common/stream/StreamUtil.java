package ceri.common.stream;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.CollectionSupplier;
import ceri.common.collection.CollectionUtil;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
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
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Stream<T> stream,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		for (var i = stream.iterator(); i.hasNext();)
			consumer.accept(i.next());
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception> void forEach(IntStream stream,
		Excepts.IntConsumer<E> consumer) throws E {
		for (var i = stream.iterator(); i.hasNext();)
			consumer.accept(i.nextInt());
	}

	/**
	 * Makes a stream sequential then applies simple collection.
	 */
	public static <T, R> R collect(Stream<T> stream, Supplier<R> supplier,
		BiConsumer<R, ? super T> accumulator) {
		return stream.sequential().collect(supplier, accumulator, badCombiner());
	}

	/**
	 * Returns the first entry in the stream, or null if empty.
	 */
	public static <T> T first(Stream<T> stream) {
		return stream.findFirst().orElse(null);
	}

	/**
	 * Returns true if the stream is empty.
	 */
	public static boolean isEmpty(Stream<?> stream) {
		return stream.findAny().isEmpty();
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
	 * Join streams of collections.
	 */
	public static <T> Set<T> joinToSet(Stream<? extends Collection<? extends T>> stream) {
		return joinToSet(stream, supplier.set());
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
		return joinToList(stream, supplier.list());
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
		return toSet(stream, supplier.set());
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
