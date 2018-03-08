package ceri.ent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.factory.Factory;
import ceri.common.filter.Filter;
import ceri.common.filter.Filters;

/**
 * An in-memory data service with persistence.
 */
public class PersistentService<K extends Comparable<K>, V> implements Persistable {
	private static final int UNLIMITED_COUNT = -1;
	private final PersistentStore<Collection<V>> store;
	private final SafeReadWrite safe = new SafeReadWrite();
	private final Map<K, V> map = new TreeMap<>();
	private final Factory<K, V> identifier;
	private boolean modified = false;

	public PersistentService(PersistentStore<Collection<V>> store, Factory<K, V> identifier) {
		this.store = store;
		this.identifier = identifier;
	}

	public V findById(K id) {
		return safe.read(() -> map.get(id));
	}

	protected Collection<V> findAll() {
		return find(Filters._true());
	}

	public Collection<V> find(Filter<V> filter) {
		return find(filter, UNLIMITED_COUNT);
	}

	public Collection<V> find(Filter<V> filter, int maxCount) {
		return safe.read(() -> {
			Collection<V> values = new HashSet<>();
			for (V value : this.map.values()) {
				if (!filter.filter(value)) continue;
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
		Collection<V> values = safe.read(() -> new ArrayList<>(this.map.values()));
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

	private Map<K, V> toMap(Iterable<V> values) {
		if (values == null) return Collections.emptyMap();
		Map<K, V> map = new HashMap<>();
		for (V value : values) {
			K id = identifier.create(value);
			map.put(id, value);
		}
		return map;
	}

}
