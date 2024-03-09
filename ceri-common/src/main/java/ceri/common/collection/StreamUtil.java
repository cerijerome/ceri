package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.listSupplier;
import static ceri.common.collection.CollectionUtil.mapSupplier;
import static ceri.common.collection.CollectionUtil.setSupplier;
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
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import ceri.common.comparator.Comparators;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.FunctionUtil;
import ceri.common.function.ObjIntFunction;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.BasicUtil;

/**
 * Convenience shortcuts for common stream methods.
 */
public class StreamUtil {
	private static final IntBinaryOperator intBitwiseAnd = (lhs, rhs) -> lhs & rhs;
	private static final IntBinaryOperator intBitwiseOr = (lhs, rhs) -> lhs | rhs;
	private static final IntBinaryOperator intBitwiseXor = (lhs, rhs) -> lhs ^ rhs;
	private static final LongBinaryOperator longBitwiseAnd = (lhs, rhs) -> lhs & rhs;
	private static final LongBinaryOperator longBitwiseOr = (lhs, rhs) -> lhs | rhs;
	private static final LongBinaryOperator longBitwiseXor = (lhs, rhs) -> lhs ^ rhs;
	private static final BiConsumer<?, ?> badCombiner = (r1, r2) -> {
		throw new IllegalStateException();
	};

	private StreamUtil() {}

	private static class Distinction<T> {
		public final Object key;
		public final T value;

		private Distinction(Object key, T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key);
		}

		@Override
		public boolean equals(Object obj) {
			// Only ever compared to other instances
			return Objects.equals(key, ((Distinction<?>) obj).key);
		}
	}

	/**
	 * Use when a combiner is required for a Stream method, but should not be invoked.
	 */
	public static <T> BiConsumer<T, T> badCombiner() {
		return BasicUtil.uncheckedCast(badCombiner);
	}

	public static <E extends Exception, T, R> R closeableApply(Stream<T> stream,
		ExceptionFunction<E, Stream<T>, R> fn) throws E {
		try (stream) {
			return fn.apply(stream);
		}
	}

	public static <E extends Exception, T> int closeableApplyAsInt(Stream<T> stream,
		ExceptionToIntFunction<E, Stream<T>> fn) throws E {
		try (stream) {
			return fn.applyAsInt(stream);
		}
	}

	public static <E extends Exception, T> void closeableAccept(Stream<T> stream,
		ExceptionConsumer<E, Stream<T>> consumer) throws E {
		try (stream) {
			consumer.accept(stream);
		}
	}

	public static <E extends Exception, T> void closeableForEach(Stream<T> stream,
		ExceptionConsumer<E, T> consumer) throws E {
		try (stream) {
			FunctionUtil.forEach(stream, consumer);
		}
	}

	public static DoubleStream unitRange(int steps) {
		return IntStream.range(0, steps).mapToDouble(i -> (double) i / (steps - 1));
	}

	public static IntStream toInt(Stream<? extends Number> stream) {
		return stream.mapToInt(Number::intValue);
	}

	public static LongStream toLong(Stream<? extends Number> stream) {
		return stream.mapToLong(Number::longValue);
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

	public static long bitwiseAnd(LongStream stream) {
		return stream.reduce(longBitwiseAnd).orElse(0L);
	}

	public static long bitwiseOr(LongStream stream) {
		return stream.reduce(0L, longBitwiseOr);
	}

	public static long bitwiseXor(LongStream stream) {
		return stream.reduce(0L, longBitwiseXor);
	}

	public static <T> Stream<Indexed<T>> range(int count, IntFunction<T> fn) {
		return range(0, count, fn);
	}

	public static <T> Stream<Indexed<T>> range(int from, int to, IntFunction<T> fn) {
		return IntStream.range(from, to).mapToObj(i -> Indexed.of(fn.apply(i), i));
	}

	public static <T> Stream<Indexed<T>> indexed(List<T> values) {
		return range(values.size(), values::get);
	}

	@SafeVarargs
	public static <T> Stream<Indexed<T>> indexed(T... array) {
		return range(array.length, i -> array[i]);
	}

	public static <T, R> Stream<R> map(Stream<Indexed<T>> indexStream, ObjIntFunction<T, R> mapFn) {
		return indexStream.map(i -> mapFn.apply(i.val, i.i));
	}

	public static <T, R> Stream<R> indexedMap(List<T> values, ObjIntFunction<T, R> mapFn) {
		return map(indexed(values), mapFn);
	}

	public static <T> void indexedForEach(List<T> values, ObjIntConsumer<T> consumer) {
		indexed(values).forEach(i -> consumer.accept(i.val, i.i));
	}

	public static <E extends Exception, T> WrappedStream<E, T> wrap(Stream<T> stream) {
		return WrappedStream.of(stream);
	}

	/**
	 * Filters objects of given type and casts the stream.
	 */
	public static <T> Stream<T> castAny(Stream<?> stream, Class<T> cls) {
		return stream.map(obj -> ReflectUtil.castOrNull(cls, obj)).filter(Objects::nonNull);
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
	 * Append items to a stream.
	 */
	@SafeVarargs
	public static <T> Stream<T> append(Stream<T> stream, T... ts) {
		return Stream.concat(stream, Stream.of(ts));
	}

	/**
	 * Prepend items to a stream.
	 */
	@SafeVarargs
	public static <T> Stream<T> prepend(Stream<T> stream, T... ts) {
		return Stream.concat(Stream.of(ts), stream);
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static <T> Iterable<T> iterable(Stream<T> stream) {
		return CollectionUtil.iterable(stream.iterator());
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static Iterable<Integer> iterable(IntStream stream) {
		return CollectionUtil.iterable(stream.iterator());
	}

	/**
	 * Make a stream compatible with a for-each loop.
	 */
	public static Iterable<Long> iterable(LongStream stream) {
		return CollectionUtil.iterable(stream.iterator());
	}

	/**
	 * Makes a stream sequential then applies simple collection.
	 */
	public static <T, R> R collect(Stream<T> stream, Supplier<R> supplier,
		BiConsumer<R, ? super T> accumulator) {
		return stream.sequential().collect(supplier, accumulator, badCombiner());
	}

	/**
	 * Applies distinct functionality to keys extracted from entries.
	 */
	public static <T, U> Stream<T> distinctBy(Stream<T> stream, Function<T, U> accessor) {
		return stream.map(t -> new Distinction<>(accessor.apply(t), t)).distinct()
			.map(e -> e.value);
	}

	/**
	 * Applies distinct functionality to identity hash codes.
	 */
	public static <T> Stream<T> distinctByIdentity(Stream<T> stream) {
		return distinctBy(stream, System::identityHashCode);
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
		return BasicUtil.uncheckedCast(first(stream.filter(t -> cls.isInstance(t))));
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
		return joinToSet(stream, setSupplier());
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
		return joinToList(stream, listSupplier());
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
		return toSet(stream, setSupplier());
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
		return toMap(stream, keyFn, mapSupplier());
	}

	/**
	 * Convert a stream to a LinkedHashMap and don't allow duplicate keys.
	 */
	public static <K, V, T> Map<K, V> toMap(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn) {
		return toMap(stream, keyFn, valueFn, mapSupplier());
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
		return toMapOfCollections(stream, keyFn, valueFn, mapSupplier(), collectionSupplier);
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
		return toMapOfSets(stream, keyFn, valueFn, mapSupplier());
	}

	/**
	 * Convert a stream to a map of sets.
	 */
	public static <K, V, T> Map<K, Set<V>> toMapOfSets(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, Set<V>>> mapSupplier) {
		return toMapOfCollections(stream, keyFn, valueFn, mapSupplier, setSupplier());
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
		return toMapOfLists(stream, keyFn, valueFn, mapSupplier());
	}

	/**
	 * Convert a stream to a map of lists.
	 */
	public static <K, V, T> Map<K, List<V>> toMapOfLists(Stream<T> stream,
		Function<? super T, ? extends K> keyFn, Function<? super T, ? extends V> valueFn,
		Supplier<Map<K, List<V>>> mapSupplier) {
		return toMapOfCollections(stream, keyFn, valueFn, mapSupplier, listSupplier());
	}

	/**
	 * Convert a map entry stream back to a map.
	 */
	public static <K, V> Map<K, V> toEntryMap(Stream<Map.Entry<K, V>> stream) {
		return toEntryMap(stream, mapSupplier());
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
		return entryCollector(mergeError(), mapSupplier());
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
			Iterators.spliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
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
			Iterators.intSpliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
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
			Iterators.longSpliterator(tryAdvanceFn, Long.MAX_VALUE, Spliterator.ORDERED);
		return StreamSupport.longStream(spliterator, false);
	}

	/**
	 * Returns a stream for an iterator.
	 */
	public static <T> Stream<T> stream(Iterator<T> i) {
		var spliterator = Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false);
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
	 * Returns a stream for all enum values.
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
