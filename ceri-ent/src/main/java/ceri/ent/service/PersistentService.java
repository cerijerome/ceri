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
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.function.Predicates;

/**
 * An in-memory data service with persistence.
 */
public class PersistentService<K extends Comparable<K>, V> implements Persistable {
	private static final int UNLIMITED_COUNT = -1;
	private final PersistentStore<Collection<V>> store;
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final Map<K, V> map = new TreeMap<>();
	private final Function<V, K> idFn;
	private boolean modified = false;

	public PersistentService(PersistentStore<Collection<V>> store, Function<V, K> idFn) {
		this.store = store;
		this.idFn = idFn;
	}

	public V findById(K id) {
		return safe.read(() -> map.get(id));
	}

	protected Collection<V> findAll() {
		return find(Predicates.yes());
	}

	public V findFirst(Predicate<V> filter) {
		Collection<V> collection = find(filter, 1);
		return collection.isEmpty() ? null : collection.iterator().next();
	}

	public Collection<V> find(Predicate<V> filter) {
		return find(filter, UNLIMITED_COUNT);
	}

	public Collection<V> find(Predicate<V> filter, int maxCount) {
		return safe.read(() -> {
			Collection<V> values = new HashSet<>();
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

	protected int size() {
		return safe.read(map::size);
	}

	private Map<K, V> toMap(Iterable<V> values) {
		if (values == null) return Collections.emptyMap();
		Map<K, V> map = new HashMap<>();
		for (V value : values) {
			K id = idFn.apply(value);
			map.put(id, value);
		}
		return map;
	}

}
