package ceri.common.collect;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Map;
import ceri.common.function.Excepts;
import ceri.common.reflect.Reflect;

/**
 * A cache using a concurrent hash map, with identity equality, and weak keys that are automatically
 * removed when out of scope.
 */
public class IdentityCache<K, V> {
	private final Map<Key<K>, V> map = Maps.concurrent();
	private final Cleaner cleaner = Cleaner.create();

	/**
	 * Map key that stores a weak reference. Uses system hash and identity equality.
	 */
	private static class Key<K> {
		private final WeakReference<K> ref;
		private final int hash;

		private Key(K k) {
			this.ref = new WeakReference<>(k);
			hash = System.identityHashCode(k);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			return Reflect.same(ref, ((Key<?>) obj).ref);
		}
	}

	/**
	 * Creates a new cache.
	 */
	public static <K, V> IdentityCache<K, V> of() {
		return new IdentityCache<>();
	}

	private IdentityCache() {}

	/**
	 * Looks up the cached value; generates the value if not present.
	 */
	public <E extends Exception> V get(K k, Excepts.Function<E, ? super K, ? extends V> populator)
		throws E {
		if (k == null) return null;
		var key = new Key<>(k);
		var value = map.get(key);
		if (value != null || populator == null) return value;
		return populate(key, populator.apply(k));
	}

	private V populate(Key<K> key, V value) {
		if (value == null) return value;
		map.put(key, value); // ok to overwrite
		cleaner.register(key.ref.get(), () -> map.remove(key));
		return value;
	}
}
