package ceri.common.collection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import ceri.common.util.BasicUtil;

/**
 * Provides typed collection constructors.
 */
public class CollectionSupplier {
	public static CollectionSupplier DEFAULT =
		of(LinkedHashMap::new, TreeMap::new, LinkedHashSet::new, TreeSet::new, ArrayList::new);
	private final Supplier<Map<?, ?>> map;
	private final Supplier<NavigableMap<?, ?>> navMap;
	private final Supplier<Set<?>> set;
	private final Supplier<NavigableSet<?>> navSet;
	private final Supplier<List<?>> list;
	private final Typed<?> typed = new Typed<>();
	private final TypedMap<?, ?> typedMap = new TypedMap<>();

	public class Typed<T> {
		private Typed() {}

		/**
		 * Typed set provider.
		 */
		public Supplier<Set<T>> set() {
			return BasicUtil.unchecked(set);
		}

		/**
		 * Typed navigable set provider.
		 */
		public Supplier<NavigableSet<T>> navSet() {
			return BasicUtil.unchecked(navSet);
		}

		/**
		 * Typed list provider.
		 */
		public Supplier<List<T>> list() {
			return BasicUtil.unchecked(list);
		}
	}

	public class TypedMap<K, V> {
		private TypedMap() {}

		/**
		 * Typed map provider.
		 */
		public Supplier<Map<K, V>> map() {
			return BasicUtil.unchecked(map);
		}

		/**
		 * Typed navigable map provider.
		 */
		public Supplier<NavigableMap<K, V>> navMap() {
			return BasicUtil.unchecked(navMap);
		}
	}

	public static CollectionSupplier of() {
		return DEFAULT;
	}

	public static CollectionSupplier of(Supplier<Map<?, ?>> map,
		Supplier<NavigableMap<?, ?>> navMap, Supplier<Set<?>> set, Supplier<NavigableSet<?>> navSet,
		Supplier<List<?>> list) {
		return new CollectionSupplier(map, navMap, set, navSet, list);
	}

	private CollectionSupplier(Supplier<Map<?, ?>> map, Supplier<NavigableMap<?, ?>> navMap,
		Supplier<Set<?>> set, Supplier<NavigableSet<?>> navSet, Supplier<List<?>> list) {
		this.map = map;
		this.navMap = navMap;
		this.set = set;
		this.navSet = navSet;
		this.list = list;
	}

	/**
	 * Typed access to collections.
	 */
	public <T> Typed<T> typed() {
		return BasicUtil.unchecked(typed);
	}

	/**
	 * Typed access to maps.
	 */
	public <K, V> TypedMap<K, V> typedMap() {
		return BasicUtil.unchecked(typedMap);
	}

	/**
	 * Map provider.
	 */
	public <K, V> Supplier<Map<K, V>> map() {
		return BasicUtil.unchecked(map);
	}

	/**
	 * Navigable map provider.
	 */
	public <K, V> Supplier<NavigableMap<K, V>> navMap() {
		return BasicUtil.unchecked(navMap);
	}

	/**
	 * Set provider.
	 */
	public <T> Supplier<Set<T>> set() {
		return BasicUtil.unchecked(set);
	}

	/**
	 * Navigable set provider.
	 */
	public <T> Supplier<NavigableSet<T>> navSet() {
		return BasicUtil.unchecked(navSet);
	}

	/**
	 * List provider.
	 */
	public <T> Supplier<List<T>> list() {
		return BasicUtil.unchecked(list);
	}
}
