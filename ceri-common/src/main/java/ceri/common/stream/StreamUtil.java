package ceri.common.stream;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.collection.CollectionSupplier;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.IteratorUtil;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.reflect.ReflectUtil;
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
	 * Collects a stream of int code points into a string.
	 */
	public static String toString(IntStream codePointStream) {
		return codePointStream.collect(StringBuilder::new, StringBuilder::appendCodePoint, //
			StringBuilder::append).toString();
	}

	public static String toString(Stream<?> stream, CharSequence delimiter) {
		return toString(stream, "", delimiter, "");
	}

	public static String toString(Stream<?> stream, CharSequence prefix, CharSequence delimiter,
		CharSequence suffix) {
		if (stream == null) return null;
		int prefixLen = prefix.length();
		return stream.collect(() -> new StringBuilder(prefix), (b, t) -> {
			if (b.length() > prefixLen) b.append(delimiter);
			b.append(t);
		}, StringBuilder::append).append(suffix).toString();
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static <T> Iterable<T> iterable(Stream<T> stream) {
		return IteratorUtil.iterable(stream.iterator());
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static Iterable<Integer> iterable(IntStream stream) {
		return IteratorUtil.iterable(stream.iterator());
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static Iterable<Long> iterable(LongStream stream) {
		return IteratorUtil.iterable(stream.iterator());
	}

	/**
	 * Makes a stream sequential then applies simple collection.
	 */
	public static <T, R> R collect(Stream<T> stream, Supplier<R> supplier,
		BiConsumer<R, ? super T> accumulator) {
		return stream.sequential().collect(supplier, accumulator, badCombiner());
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
	 * Returns the first instance of the given class in the stream , or null if no match.
	 */
	public static <T, U extends T> U firstOf(Stream<T> stream, Class<U> cls) {
		return BasicUtil.unchecked(first(stream.filter(t -> cls.isInstance(t))));
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
	 * Convert a stream to an identity hash set.
	 */
	public static <T> Set<T> toIdentitySet(Stream<T> stream) {
		return toSet(stream, CollectionUtil::identityHashSet);
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
	 * Convert a stream to a map of collections.
	 */
	public static <K, T, C extends Collection<T>> Map<K, C> toMapOfCollections(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Supplier<C> collectionSupplier) {
		return toMapOfCollections(stream, keyFn, t -> t, collectionSupplier);
	}

	/**
	 * Convert a stream to a map of collections.
	 */
	public static <K, V, T, C extends Collection<V>> Map<K, C> toMapOfCollections(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<C> collectionSupplier) {
		return toMapOfCollections(stream, keyFn, valueFn, supplier.map(), collectionSupplier);
	}

	/**
	 * Convert a stream to a map of collections.
	 */
	public static <K, V, T, C extends Collection<V>> Map<K, C> toMapOfCollections(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, C>> mapSupplier, Supplier<C> collectionSupplier) {
		return stream.collect(Collectors.groupingBy(keyFn, mapSupplier,
			Collectors.mapping(valueFn, Collectors.toCollection(collectionSupplier))));
	}

	/**
	 * Convert a stream to a map of sets.
	 */
	public static <K, T> Map<K, Set<T>> toMapOfSets(Stream<T> stream,
		Function<? super T, ? extends K> keyFn) {
		return toMapOfSets(stream, keyFn, t -> t);
	}

	/**
	 * Convert a stream to a map of sets.
	 */
	public static <K, V, T> Map<K, Set<V>> toMapOfSets(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return toMapOfSets(stream, keyFn, valueFn, supplier.map());
	}

	/**
	 * Convert a stream to a map of sets.
	 */
	public static <K, V, T> Map<K, Set<V>> toMapOfSets(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, Set<V>>> mapSupplier) {
		return toMapOfCollections(stream, keyFn, valueFn, mapSupplier, supplier.set());
	}

	/**
	 * Convert a stream to a map of lists.
	 */
	public static <K, T> Map<K, List<T>> toMapOfLists(Stream<T> stream,
		Function<? super T, ? extends K> keyFn) {
		return toMapOfLists(stream, keyFn, t -> t);
	}

	/**
	 * Convert a stream to a map of lists.
	 */
	public static <K, V, T> Map<K, List<V>> toMapOfLists(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return toMapOfLists(stream, keyFn, valueFn, supplier.map());
	}

	/**
	 * Convert a stream to a map of lists.
	 */
	public static <K, V, T> Map<K, List<V>> toMapOfLists(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, List<V>>> mapSupplier) {
		return toMapOfCollections(stream, keyFn, valueFn, mapSupplier, supplier.list());
	}

	/**
	 * Convert a map entry stream back to a map.
	 */
	public static <K, V> Map<K, V> toEntryMap(Stream<Map.Entry<K, V>> stream) {
		return toEntryMap(stream, supplier.map());
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
		return entryCollector(mergeError(), supplier.map());
	}

	/**
	 * Collector for map entry back to map.
	 */
	public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>>
		entryCollector(BinaryOperator<V> mergeFn, Supplier<Map<K, V>> mapSupplier) {
		return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFn, mapSupplier);
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

	/**
	 * Convert a map to an object stream.
	 */
	public static <K, V, T> Stream<T> stream(Map<K, V> map,
		BiFunction<? super K, ? super V, ? extends T> mapFn) {
		Function<Map.Entry<K, V>, T> fn = (entry -> mapFn.apply(entry.getKey(), entry.getValue()));
		return map.entrySet().stream().map(fn);
	}

	/**
	 * Construct an ordered stream from hasNext and next functions.
	 */
	public static <T> Stream<T> stream(BooleanSupplier hasNextFn, Supplier<T> nextFn) {
		return stream(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.get());
			return true;
		});
	}

	/**
	 * Construct an ordered stream from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true.
	 */
	public static <T> Stream<T> stream(Predicate<Consumer<? super T>> tryAdvanceFn) {
		Spliterator<T> spliterator =
			IteratorUtil.spliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false);
	}

	/**
	 * Construct an ordered stream from hasNext and next functions.
	 */
	public static IntStream intStream(BooleanSupplier hasNextFn, IntSupplier nextFn) {
		return intStream(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.getAsInt());
			return true;
		});
	}

	/**
	 * Construct an ordered stream from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true.
	 */
	public static IntStream intStream(Predicate<IntConsumer> tryAdvanceFn) {
		Spliterator.OfInt spliterator =
			IteratorUtil.intSpliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
		return StreamSupport.intStream(spliterator, false);
	}

	/**
	 * Construct an ordered stream from hasNext and next functions.
	 */
	public static LongStream longStream(BooleanSupplier hasNextFn, LongSupplier nextFn) {
		return longStream(action -> {
			if (hasNextFn == null || !hasNextFn.getAsBoolean()) return false;
			if (nextFn != null) action.accept(nextFn.getAsLong());
			return true;
		});
	}

	/**
	 * Construct an ordered stream from a try-advance method. The method returns false if no more
	 * values, otherwise it passes the next value to the action, and returns true.
	 */
	public static LongStream longStream(Predicate<LongConsumer> tryAdvanceFn) {
		Spliterator.OfLong spliterator =
			IteratorUtil.longSpliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
		return StreamSupport.longStream(spliterator, false);
	}

	/**
	 * Streams the collection elements.
	 */
	@SafeVarargs
	public static <T> Stream<T> streamAll(Collection<? extends T>... collections) {
		return Stream.of(collections).filter(Objects::nonNull).flatMap(Collection::stream);
	}

	/**
	 * Returns a stream for an iterator.
	 */
	public static <T> Stream<T> stream(Iterator<T> i) {
		var spliterator = Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false);
	}

	/**
	 * Returns a stream for an iterable type.
	 */
	public static <T> Stream<T> stream(Iterable<T> i) {
		return StreamSupport.stream(i.spliterator(), false);
	}

	/**
	 * Returns a stream for an int iterator.
	 */
	public static IntStream intStream(PrimitiveIterator.OfInt i) {
		var spliterator = Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED);
		return StreamSupport.intStream(spliterator, false);
	}

	/**
	 * Returns a stream for an Enumeration.
	 */
	public static <T> Stream<T> stream(Enumeration<T> e) {
		return stream(e::hasMoreElements, e::nextElement);
	}

	/**
	 * Stream subarray. The case missing from Arrays.stream
	 */
	public static <T> Stream<T> stream(T[] array, int offset) {
		return Arrays.stream(array, offset, array.length);
	}

	/**
	 * Returns a stream for all enum values. The enum array is created each time; for cached enum
	 * values use <code>EnumUtil.enums(enumCls).stream()</code>.
	 */
	public static <T extends Enum<T>> Stream<T> stream(Class<T> enumCls) {
		return Stream.of(enumCls.getEnumConstants());
	}

	/**
	 * Split a stream into map entries, with split value as key, original value as value. Useful for
	 * creating maps of types that have collection fields.
	 */
	public static <T, K> Stream<Map.Entry<K, T>> flatInvert(Stream<T> stream,
		Function<T, Collection<K>> fn) {
		return stream.flatMap(t -> fn.apply(t).stream().map(k -> new SimpleEntry<>(k, t)));
	}
}
