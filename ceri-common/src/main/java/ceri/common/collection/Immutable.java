package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import ceri.common.array.RawArray;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

/**
 * Support for immutable types.
 */
public class Immutable {
	private Immutable() {}

	/**
	 * Immutable two-way map of keys and values.
	 */
	public static class BiMap<K, V> {
		public final Map<K, V> keys;
		public final Map<V, K> values;

		private BiMap(Map<K, V> keys, Map<V, K> values) {
			this.keys = keys;
			this.values = values;
		}

		/**
		 * Gets the values for the key.
		 */
		public V value(K key) {
			return keys.get(key);
		}

		public K key(V value) {
			return values.get(value);
		}
	}

	/**
	 * Provides unmodifiable collection wrappers and compatible mutable collections.
	 */
	public static class Wrap<T> implements Functions.Function<T, T> {
		private static final Wrap<Collection<?>> COLLECT =
			new Wrap<>(Collections::unmodifiableCollection, Collections::emptyList, ArrayList::new);
		private static final Wrap<SequencedCollection<?>> SEQ_COLLECT = new Wrap<>(
			Collections::unmodifiableSequencedCollection, Collections::emptyList, ArrayList::new);
		private static final Wrap<List<?>> LIST =
			new Wrap<>(Collections::unmodifiableList, Collections::emptyList, ArrayList::new);
		private static final Wrap<List<?>> LINK_LIST = LIST.to(LinkedList::new);
		private static final Wrap<Set<?>> SET =
			new Wrap<>(Collections::unmodifiableSet, Collections::emptySet, HashSet::new);
		private static final Wrap<SequencedSet<?>> SEQ_SET = new Wrap<>(
			Collections::unmodifiableSequencedSet, Collections::emptySortedSet, LinkedHashSet::new);
		private static final Wrap<SortedSet<?>> SORT_SET = new Wrap<>(
			Collections::unmodifiableSortedSet, Collections::emptySortedSet, Mutable::treeSet);
		private static final Wrap<NavigableSet<?>> NAV_SET =
			new Wrap<>(Collections::unmodifiableNavigableSet, Collections::emptyNavigableSet,
				Mutable::treeSet);
		private static final Wrap<Set<?>> ID_SET = SET.to(Mutable::idSet);
		private static final Wrap<Map<?, ?>> MAP =
			new Wrap<>(Collections::unmodifiableMap, Collections::emptyMap, HashMap::new);
		private static final Wrap<SequencedMap<?, ?>> SEQ_MAP = new Wrap<>(
			Collections::unmodifiableSequencedMap, Collections::emptySortedMap, LinkedHashMap::new);
		private static final Wrap<SortedMap<?, ?>> SORT_MAP = new Wrap<>(
			Collections::unmodifiableSortedMap, Collections::emptySortedMap, Mutable::treeMap);
		private static final Wrap<NavigableMap<?, ?>> NAV_MAP =
			new Wrap<>(Collections::unmodifiableNavigableMap, Collections::emptyNavigableMap,
				Mutable::treeMap);
		private static final Wrap<Map<?, ?>> ID_MAP = MAP.to(IdentityHashMap::new);
		private final Functions.Operator<T> wrapper;
		private final Functions.Supplier<? extends T> supplier;
		private final Functions.Supplier<? extends T> emptySupplier;

		/**
		 * Provides an immutable collection wrapper, using an underlying array list.
		 */
		public static <T> Wrap<Collection<T>> collect() {
			return BasicUtil.unchecked(COLLECT);
		}

		/**
		 * Provides an immutable sequenced collection wrapper, using an underlying array list.
		 */
		public static <T> Wrap<SequencedCollection<T>> seqCollect() {
			return BasicUtil.unchecked(SEQ_COLLECT);
		}

		/**
		 * Provides an immutable list wrapper, using an underlying array list.
		 */
		public static <T> Wrap<List<T>> list() {
			return BasicUtil.unchecked(LIST);
		}

		/**
		 * Provides an immutable list wrapper, using an underlying linked list.
		 */
		public static <T> Wrap<List<T>> linkList() {
			return BasicUtil.unchecked(LINK_LIST);
		}

		/**
		 * Provides an immutable set wrapper, using an underlying hash set.
		 */
		public static <T> Wrap<Set<T>> set() {
			return BasicUtil.unchecked(SET);
		}

		/**
		 * Provides an immutable sequenced set wrapper, using an underlying linked hash set.
		 */
		public static <T> Wrap<SequencedSet<T>> seqSet() {
			return BasicUtil.unchecked(SEQ_SET);
		}

		/**
		 * Provides an immutable sorted set wrapper, using an underlying tree set.
		 */
		public static <T extends Comparable<? super T>> Wrap<SortedSet<T>> sortSet() {
			return BasicUtil.unchecked(SORT_SET);
		}

		/**
		 * Provides an immutable sorted set wrapper, using an underlying tree set.
		 */
		public static <T> Wrap<SortedSet<T>> sortSet(Comparator<? super T> comparator) {
			return BasicUtil.unchecked(SORT_SET.to(() -> new TreeSet<>(Comparators.of(comparator))));
		}

		/**
		 * Provides an immutable navigable set wrapper, using an underlying tree set.
		 */
		public static <T extends Comparable<? super T>> Wrap<NavigableSet<T>> navSet() {
			return BasicUtil.unchecked(NAV_SET);
		}

		/**
		 * Provides an immutable navigable set wrapper, using an underlying tree set.
		 */
		public static <T> Wrap<NavigableSet<T>> navSet(Comparator<? super T> comparator) {
			return BasicUtil.unchecked(NAV_SET.to(() -> new TreeSet<>(Comparators.of(comparator))));
		}

		/**
		 * Provides an immutable set wrapper, using an underlying identity hash set.
		 */
		public static <T> Wrap<Set<T>> idSet() {
			return BasicUtil.unchecked(ID_SET);
		}

		/**
		 * Provides an immutable map wrapper, using an underlying hash map.
		 */
		public static <K, V> Wrap<Map<K, V>> map() {
			return BasicUtil.unchecked(MAP);
		}

		/**
		 * Provides an immutable sequenced map wrapper, using an underlying linked hash map.
		 */
		public static <K, V> Wrap<SequencedMap<K, V>> seqMap() {
			return BasicUtil.unchecked(SEQ_MAP);
		}

		/**
		 * Provides an immutable sorted map wrapper, using an underlying tree map.
		 */
		public static <K extends Comparable<? super K>, V> Wrap<SortedMap<K, V>> sortMap() {
			return BasicUtil.unchecked(SORT_MAP);
		}

		/**
		 * Provides an immutable sorted map wrapper, using an underlying tree map.
		 */
		public static <K, V> Wrap<SortedMap<K, V>> sortMap(Comparator<? super K> comparator) {
			return BasicUtil
				.unchecked(SORT_MAP.to(() -> new TreeMap<>(Comparators.of(comparator))));
		}

		/**
		 * Provides an immutable navigable map wrapper, using an underlying tree map.
		 */
		public static <K extends Comparable<? super K>, V> Wrap<NavigableMap<K, V>> navMap() {
			return BasicUtil.unchecked(NAV_MAP);
		}

		/**
		 * Provides an immutable navigable map wrapper, using an underlying tree map.
		 */
		public static <K, V> Wrap<NavigableMap<K, V>> navMap(Comparator<? super K> comparator) {
			return BasicUtil.unchecked(NAV_MAP.to(() -> new TreeMap<>(Comparators.of(comparator))));
		}

		/**
		 * Provides an immutable map wrapper, using an underlying identity hash map.
		 */
		public static <K, V> Wrap<Map<K, V>> idMap() {
			return BasicUtil.unchecked(ID_MAP);
		}

		private Wrap(Functions.Operator<T> wrapper, Functions.Supplier<? extends T> emptySupplier,
			Functions.Supplier<? extends T> supplier) {
			this.wrapper = wrapper;
			this.supplier = supplier;
			this.emptySupplier = emptySupplier;
		}

		/**
		 * Creates a copy of the wrapper with a new collection supplier.
		 */
		public Wrap<T> to(Functions.Supplier<? extends T> supplier) {
			if (supplier == null) return this;
			return new Wrap<>(wrapper, emptySupplier, supplier);
		}

		/**
		 * Creates a new mutable collection.
		 */
		public T mutable() {
			return supplier.get();
		}

		/**
		 * Provides an empty immutable collection.
		 */
		public T empty() {
			return emptySupplier.get();
		}

		/**
		 * Applies the unmodifiable wrapper to the collection.
		 */
		@Override
		public T apply(T type) {
			return type == null ? empty() : wrapper.apply(type);
		}

		/**
		 * Wraps a new type as unmodifiable, after applying the populator.
		 */
		public <E extends Exception> T wrap(Excepts.Consumer<E, ? super T> populator) throws E {
			if (populator == null) return empty();
			var type = mutable();
			populator.accept(type);
			return apply(type);
		}
	}

	/**
	 * Wraps a type as unmodifiable.
	 */
	public static <T> T wrap(Wrap<T> wrap, T type) {
		if (wrap == null) return null;
		return wrap.apply(type);
	}

	// wrapping

	/**
	 * Wraps a list as unmodifiable.
	 */
	public static <T> List<T> wrap(List<T> list) {
		return wrap(Wrap.list(), list);
	}

	/**
	 * Wraps values as an unmodifiable list.
	 */
	@SafeVarargs
	public static <T> List<T> wrapListOf(T... values) {
		if (values == null || values.length == 0) return List.of();
		return wrap(Arrays.asList(values));
	}

	/**
	 * Wraps an array region as an unmodifiable list.
	 */
	public static <T> List<T> wrapList(T[] array, int offset) {
		return wrapList(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Wraps an array region as an unmodifiable list.
	 */
	public static <T> List<T> wrapList(T[] array, int offset, int length) {
		if (array == null || array.length == 0) return List.of();
		return RawArray.applySlice(array, offset, length,
			(o, l) -> wrap(Arrays.asList(array).subList(o, o + l)));
	}

	/**
	 * Wraps a set as unmodifiable.
	 */
	public static <T> Set<T> wrap(Set<T> set) {
		return wrap(Wrap.set(), set);
	}

	/**
	 * Wraps a map as unmodifiable.
	 */
	public static <K, V> Map<K, V> wrap(Map<K, V> map) {
		return of(Wrap.map(), map);
	}

	/**
	 * Wraps a two-way map of keys and values as unmodifiable.
	 */
	public static <K, V> BiMap<K, V> wrapBiMap(Map<K, V> map) {
		return new BiMap<>(wrap(map), invertMap(map));
	}

	// typed wrappers

	/**
	 * Creates an immutable collection, copied from value vararg array.
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C ofAll(Wrap<C> wrap, T... values) {
		return of(wrap, values, 0);
	}

	/**
	 * Creates an immutable collection, copied from array region.
	 */
	public static <T, C extends Collection<T>> C of(Wrap<C> wrap, T[] array, int offset) {
		return of(wrap, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable collection, copied from array region.
	 */
	public static <T, C extends Collection<T>> C of(Wrap<C> wrap, T[] array, int offset,
		int length) {
		if (wrap == null) return null;
		return wrap.apply(Mutable.add(wrap.mutable(), array, offset, length));
	}

	/**
	 * Creates an immutable collection, copied from iterable values.
	 */
	public static <T, C extends Collection<T>> C of(Wrap<C> wrap, Iterable<? extends T> values) {
		if (wrap == null) return null;
		return wrap.apply(Mutable.add(wrap.mutable(), values));
	}

	/**
	 * Creates an immutable copy of the map.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap,
		Map<? extends K, ? extends V> map) {
		if (wrap == null) return null;
		return wrap.apply(Mutable.put(wrap.mutable(), map));
	}

	/**
	 * Creates an immutable map from the key and value.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap, K k, V v) {
		if (wrap == null) return null;
		return wrap.wrap(m -> Mutable.put(m, k, v));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap, K k0, V v0, K k1, V v1) {
		if (wrap == null) return null;
		return wrap.wrap(m -> Mutable.put(m, k0, v0, k1, v1));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap, K k0, V v0, K k1, V v1, K k2,
		V v2) {
		if (wrap == null) return null;
		return wrap.wrap(m -> Mutable.put(m, k0, v0, k1, v1, k2, v2));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap, K k0, V v0, K k1, V v1, K k2, V v2,
		K k3, V v3) {
		if (wrap == null) return null;
		return wrap.wrap(m -> Mutable.put(m, k0, v0, k1, v1, k2, v2, k3, v3));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V, M extends Map<K, V>> M of(Wrap<M> wrap, K k0, V v0, K k1, V v1, K k2, V v2,
		K k3, V v3, K k4, V v4) {
		if (wrap == null) return null;
		return wrap.wrap(m -> Mutable.put(m, k0, v0, k1, v1, k2, v2, k3, v3, k4, v4));
	}

	/**
	 * Creates an immutable collection from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAll(Wrap<C> wrap,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adapt(wrap, mapper, values, 0);
	}

	/**
	 * Creates an immutable collection from transformed array values.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adapt(Wrap<C> wrap,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset) throws E {
		return adapt(wrap, mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable collection from transformed array values.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adapt(Wrap<C> wrap,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		if (wrap == null) return null;
		return wrap.apply(Mutable.adaptAdd(wrap.mutable(), mapper, array, offset, length));
	}

	/**
	 * Creates an immutable collection from transformed iterable values.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adapt(Wrap<C> wrap,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<T> values) throws E {
		return wrap.apply(Mutable.adaptAdd(wrap.mutable(), mapper, values));
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adapt(Wrap<M> wrap,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> map) throws E {
		if (wrap == null) return null;
		return wrap.apply(Mutable.adaptPut(wrap.mutable(), keyMapper, valueMapper, map));
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdapt(Wrap<M> wrap,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> map) throws E {
		if (wrap == null) return null;
		return wrap.apply(Mutable.biAdaptPut(wrap.mutable(), keyMapper, valueMapper, map));
	}

	/**
	 * Creates an immutable collection from transformed map entries.
	 */
	public static <E extends Exception, K, V, T, C extends Collection<T>> C convert(Wrap<C> wrap,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		if (wrap == null) return null;
		return wrap.apply(Mutable.convertAdd(wrap.mutable(), unmapper, map));
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	@SafeVarargs
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(Wrap<M> wrap,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T... values) throws E {
		return convert(wrap, keyMapper, valueMapper, values, 0);
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(Wrap<M> wrap,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] values, int offset)
		throws E {
		return convert(wrap, keyMapper, valueMapper, values, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(Wrap<M> wrap,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] values, int offset,
		int length) throws E {
		if (wrap == null) return null;
		return wrap.apply(
			Mutable.convertPut(wrap.mutable(), keyMapper, valueMapper, values, offset, length));
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(Wrap<M> wrap,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> values)
		throws E {
		if (wrap == null) return null;
		return wrap.apply(Mutable.convertPut(wrap.mutable(), keyMapper, valueMapper, values));
	}

	// lists

	/**
	 * Creates an immutable list, copied from value vararg array.
	 */
	@SafeVarargs
	public static <T> List<T> listOf(T... values) {
		return ofAll(Wrap.list(), values);
	}

	/**
	 * Creates an immutable list, copied from value vararg array.
	 */
	@SafeVarargs
	public static <T> List<T> listOfAll(Functions.Supplier<? extends List<T>> supplier,
		T... values) {
		if (supplier == null) return listOf(values);
		return wrap(Mutable.addAll(supplier.get(), values));
	}

	/**
	 * Creates an immutable list, copied from array region.
	 */
	public static <T> List<T> list(T[] array, int offset) {
		return list(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable list, copied from array region.
	 */
	public static <T> List<T> list(T[] array, int offset, int length) {
		return of(Wrap.list(), array, offset, length);
	}

	/**
	 * Creates an immutable list, copied from array region.
	 */
	public static <T> List<T> list(Functions.Supplier<? extends List<T>> supplier, T[] array,
		int offset, int length) {
		if (supplier == null) return list(array, offset, length);
		return wrap(Mutable.add(supplier.get(), array, offset, length));
	}

	/**
	 * Creates an immutable list, copied from iterable values.
	 */
	public static <T> List<T> list(Iterable<? extends T> values) {
		return of(Wrap.list(), values);
	}

	/**
	 * Creates an immutable list, copied from iterable values.
	 */
	public static <T> List<T> list(Functions.Supplier<? extends List<T>> supplier,
		Iterable<? extends T> values) {
		if (supplier == null) return list(values);
		return wrap(Mutable.add(supplier.get(), values));
	}

	/**
	 * Creates an immutable list from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> List<U> adaptListOf(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adaptAll(Wrap.list(), mapper, values);
	}

	/**
	 * Creates an immutable list from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> List<U> adaptListOfAll(
		Functions.Supplier<? extends List<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		if (supplier == null) return adaptListOf(mapper, values);
		return wrap(Mutable.adaptAddAll(supplier.get(), mapper, values));
	}

	/**
	 * Creates an immutable list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
		throws E {
		return adaptList(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return adapt(Wrap.list(), mapper, array, offset, length);
	}

	/**
	 * Creates an immutable list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Functions.Supplier<? extends List<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		if (supplier == null) return adaptList(mapper, array, offset, length);
		return wrap(Mutable.adaptAdd(supplier.get(), mapper, array, offset, length));
	}

	/**
	 * Creates an immutable list from transformed iterable values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<T> values) throws E {
		return adapt(Wrap.list(), mapper, values);
	}

	/**
	 * Creates an immutable list from transformed iterable values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Functions.Supplier<? extends List<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<T> values) throws E {
		if (supplier == null) return adaptList(mapper, values);
		return wrap(Mutable.adaptAdd(supplier.get(), mapper, values));
	}

	/**
	 * Creates an immutable list from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> List<T> convertList(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return convert(Wrap.list(), unmapper, map);
	}

	/**
	 * Creates an immutable list from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> List<T> convertList(
		Functions.Supplier<? extends List<T>> supplier,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		if (supplier == null) return convertList(unmapper, map);
		return wrap(Mutable.convertAdd(supplier.get(), unmapper, map));
	}

	// sets

	/**
	 * Creates an immutable set, copied from value vararg array.
	 */
	@SafeVarargs
	public static <T> Set<T> setOf(T... values) {
		return ofAll(Wrap.set(), values);
	}

	/**
	 * Creates an immutable set, copied from value vararg array.
	 */
	@SafeVarargs
	public static <T> Set<T> setOfAll(Functions.Supplier<? extends Set<T>> supplier, T... values) {
		if (supplier == null) return setOf(values);
		return wrap(Mutable.addAll(supplier.get(), values));
	}

	/**
	 * Creates an immutable set, copied from array region.
	 */
	public static <T> Set<T> set(T[] array, int offset) {
		return set(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable set, copied from array region.
	 */
	public static <T> Set<T> set(T[] array, int offset, int length) {
		return of(Wrap.set(), array, offset, length);
	}

	/**
	 * Creates an immutable set, copied from array region.
	 */
	public static <T> Set<T> set(Functions.Supplier<? extends Set<T>> supplier, T[] array,
		int offset, int length) {
		if (supplier == null) return set(array, offset, length);
		return wrap(Mutable.add(supplier.get(), array, offset, length));
	}

	/**
	 * Creates an immutable set, copied from iterable values.
	 */
	public static <T> Set<T> set(Iterable<? extends T> values) {
		return of(Wrap.set(), values);
	}

	/**
	 * Creates an immutable set, copied from iterable values.
	 */
	public static <T> Set<T> set(Functions.Supplier<? extends Set<T>> supplier,
		Iterable<? extends T> values) {
		if (supplier == null) return set(values);
		return wrap(Mutable.add(supplier.get(), values));
	}

	/**
	 * Creates an immutable set from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Set<U> adaptSetOf(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adaptAll(Wrap.set(), mapper, values);
	}

	/**
	 * Creates an immutable set from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Set<U> adaptSetOfAll(
		Functions.Supplier<? extends Set<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		if (supplier == null) return adaptSetOf(mapper, values);
		return wrap(Mutable.adaptAddAll(supplier.get(), mapper, values));
	}

	/**
	 * Creates an immutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
		throws E {
		return adaptSet(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates an immutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return adapt(Wrap.set(), mapper, array, offset, length);
	}

	/**
	 * Creates an immutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Functions.Supplier<? extends Set<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		if (supplier == null) return adaptSet(mapper, array, offset, length);
		return wrap(Mutable.adaptAdd(supplier.get(), mapper, array, offset, length));
	}

	/**
	 * Creates an immutable set from transformed iterable values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return adapt(Wrap.set(), mapper, values);
	}

	/**
	 * Creates an immutable set from transformed iterable values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Functions.Supplier<? extends Set<U>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		if (supplier == null) return adaptSet(mapper, values);
		return wrap(Mutable.adaptAdd(supplier.get(), mapper, values));
	}

	/**
	 * Creates an immutable set from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> Set<T> convertSet(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return convert(Wrap.set(), unmapper, map);
	}

	/**
	 * Creates an immutable set from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> Set<T> convertSet(
		Functions.Supplier<? extends Set<T>> supplier,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		if (supplier == null) return convertSet(unmapper, map);
		return wrap(Mutable.convertAdd(supplier.get(), unmapper, map));
	}

	// maps

	/**
	 * Creates an immutable two-way map of keys and values.
	 */
	public static <K, V> BiMap<K, V> biMap(Map<K, V> map) {
		return wrapBiMap(map(map));
	}

	/**
	 * Creates an immutable map from the key and value.
	 */
	public static <K, V> Map<K, V> mapOf(K k, V v) {
		return of(Wrap.map(), k, v);
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1) {
		return of(Wrap.map(), k0, v0, k1, v1);
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2) {
		return of(Wrap.map(), k0, v0, k1, v1, k2, v2);
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		return of(Wrap.map(), k0, v0, k1, v1, k2, v2, k3, v3);
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4,
		V v4) {
		return of(Wrap.map(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
	}

	/**
	 * Creates an immutable map from the key and value.
	 */
	public static <K, V> Map<K, V> mapOf(Functions.Supplier<? extends Map<K, V>> supplier, K k,
		V v) {
		if (supplier == null) return mapOf(k, v);
		return wrap(Mutable.put(supplier.get(), k, v));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(Functions.Supplier<? extends Map<K, V>> supplier, K k0,
		V v0, K k1, V v1) {
		if (supplier == null) return mapOf(k0, v0, k1, v1);
		return wrap(Mutable.put(supplier.get(), k0, v0, k1, v1));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(Functions.Supplier<? extends Map<K, V>> supplier, K k0,
		V v0, K k1, V v1, K k2, V v2) {
		if (supplier == null) return mapOf(k0, v0, k1, v1, k2, v2);
		return wrap(Mutable.put(supplier.get(), k0, v0, k1, v1, k2, v2));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(Functions.Supplier<? extends Map<K, V>> supplier, K k0,
		V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		if (supplier == null) return mapOf(k0, v0, k1, v1, k2, v2, k3, v3);
		return wrap(Mutable.put(supplier.get(), k0, v0, k1, v1, k2, v2, k3, v3));
	}

	/**
	 * Creates an immutable map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(Functions.Supplier<? extends Map<K, V>> supplier, K k0,
		V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
		if (supplier == null) return mapOf(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
		return wrap(Mutable.put(supplier.get(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4));
	}

	/**
	 * Creates an immutable copy of the map.
	 */
	public static <K, V> Map<K, V> map(Map<? extends K, ? extends V> map) {
		return of(Wrap.map(), map);
	}

	/**
	 * Creates an immutable copy of the map.
	 */
	public static <K, V> Map<K, V> map(Functions.Supplier<? extends Map<K, V>> supplier,
		Map<? extends K, ? extends V> map) {
		if (supplier == null) return map(map);
		return wrap(Mutable.put(supplier.get(), map));
	}

	/**
	 * Creates an immutable map by mapping each element to a key.
	 */
	public static <E extends Exception, T, K> Map<K, T> convertMap(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return convertMap(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> convertMap(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> values)
		throws E {
		return convert(Wrap.map(), keyMapper, valueMapper, values);
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> convertMap(
		Functions.Supplier<? extends Map<K, V>> supplier,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> values)
		throws E {
		if (supplier == null) return convertMap(keyMapper, valueMapper, values);
		return wrap(Mutable.convertPut(supplier.get(), keyMapper, valueMapper, values));
	}

	/**
	 * Creates an immutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> adaptMap(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper, Map<K, ? extends V> map)
		throws E {
		return adaptMap(keyMapper, v -> v, map);
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> adaptMap(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> map) throws E {
		return adapt(Wrap.map(), keyMapper, valueMapper, map);
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> adaptMap(
		Functions.Supplier<? extends Map<T, U>> supplier,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> map) throws E {
		if (supplier == null) return adaptMap(keyMapper, valueMapper, map);
		return wrap(Mutable.adaptPut(supplier.get(), keyMapper, valueMapper, map));
	}

	/**
	 * Creates an immutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> biAdaptMap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Map<K, ? extends V> map) throws E {
		return biAdaptMap(keyMapper, (_, v) -> v, map);
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> biAdaptMap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> map) throws E {
		return biAdapt(Wrap.map(), keyMapper, valueMapper, map);
	}

	/**
	 * Creates an immutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> biAdaptMap(
		Functions.Supplier<? extends Map<T, U>> supplier,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> map) throws E {
		if (supplier == null) return biAdaptMap(keyMapper, valueMapper, map);
		return wrap(Mutable.biAdaptMap(keyMapper, valueMapper, map));
	}

	/**
	 * Creates an immutable map, inverting the keys and values.
	 */
	public static <K, V> Map<V, K> invertMap(Map<? extends K, ? extends V> map) {
		return invertMap(Wrap.map(), map);
	}

	/**
	 * Creates an immutable map, inverting the keys and values.
	 */
	public static <K, V> Map<V, K> invertMap(Functions.Supplier<? extends Map<V, K>> supplier,
		Map<? extends K, ? extends V> map) {
		if (supplier == null) return invertMap(map);
		return wrap(Mutable.putInvert(supplier.get(), map));
	}

	/**
	 * Creates an immutable map, inverting the keys and values.
	 */
	public static <K, V, R extends Map<V, K>> R invertMap(Wrap<R> wrap,
		Map<? extends K, ? extends V> map) {
		if (wrap == null) return null;
		return wrap.apply(Mutable.putInvert(wrap.mutable(), map));
	}
}
