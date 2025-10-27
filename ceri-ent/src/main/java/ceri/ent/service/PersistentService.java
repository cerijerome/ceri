package ceri.ent.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;

/**
 * An in-memory data service with persistence.
 */
public class PersistentService<K extends Comparable<K>, V> implements Persistable {
	private static final int UNLIMITED_COUNT = -1;
	private final PersistentStore<Set<V>> store;
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final Map<K, V> map = Maps.tree();
	private final Functions.Function<V, K> idFn;
	private boolean modified = false;

	public PersistentService(PersistentStore<Set<V>> store, Functions.Function<V, K> idFn) {
		this.store = store;
		this.idFn = idFn;
	}

	public V findById(K id) {
		return safe.read(() -> map.get(id));
	}

	protected Set<V> findAll() {
		return find(Filters.yes());
	}

	public <E extends Exception> V findFirst(Excepts.Predicate<E, V> filter) throws E {
		var find = find(filter, 1);
		return find.isEmpty() ? null : find.iterator().next();
	}

	public <E extends Exception> Set<V> find(Excepts.Predicate<E, V> filter) throws E {
		return find(filter, UNLIMITED_COUNT);
	}

	public <E extends Exception> Set<V> find(Excepts.Predicate<E, V> filter, int maxCount) throws E {
		return safe.read(() -> {
			var values = Sets.<V>of();
			for (V value : this.map.values()) {
				if (!filter.test(value)) continue;
				values.add(value);
				if (maxCount > 0 && values.size() >= maxCount) break;
			}
			return values;
		});
	}

	@SuppressWarnings("unchecked")
	public int removeKeys(K... keys) {
		return removeKeys(Arrays.asList(keys));
	}

	public int removeKeys(Iterable<K> keys) {
		return safe.writeWithReturn(() -> {
			int removed = 0;
			for (K key : keys)
				if (map.remove(key) != null) removed++;
			if (removed > 0) modified = true;
			return removed;
		});
	}

	@SuppressWarnings("unchecked")
	protected void add(V... values) {
		add(Arrays.asList(values));
	}

	protected void add(Iterable<V> values) {
		if (values == null) return;
		Map<K, V> map = toMap(values);
		safe.write(() -> {
			safeAdd(map);
			modified = true;
		});
	}

	@Override
	public void load() throws IOException {
		if (store == null) return;
		Map<K, V> map = toMap(store.load());
		safe.write(() -> {
			safeAdd(map);
			modified = false;
		});
	}

	@Override
	public void save() throws IOException {
		if (!saveEntries()) return;
		var values = safe.read(() -> Sets.of(this.map.values()));
		store.save(values);
		safe.write(() -> modified = false); // but entries may have been written after read...
	}

	private boolean saveEntries() {
		if (store == null) return false;
		return safe.read(() -> modified);
	}

	protected void clear() {
		safe.write(() -> {
			if (!map.isEmpty()) modified = true;
			map.clear();
		});
	}

	protected void safeAdd(Map<K, V> map) {
		this.map.putAll(map);
	}

	protected int size() {
		return safe.read(map::size);
	}

	private Map<K, V> toMap(Iterable<V> values) {
		if (values == null) return Maps.of();
		return Maps.convert(idFn, values);
	}
}
