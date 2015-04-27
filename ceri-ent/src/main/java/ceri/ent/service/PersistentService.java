package ceri.ent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.factory.Factory;
import ceri.common.filter.Filter;

public class PersistentService<K, V> implements Persistable {
	private static final int UNLIMITED_COUNT = -1;
	private final PersistentStore<Collection<V>> store;
	private final SafeReadWrite safe = new SafeReadWrite();
	private final Map<K, V> map = new TreeMap<>();
	private final Factory<K, V> identifier;
	
	public PersistentService(PersistentStore<Collection<V>> store, Factory<K, V> identifier) {
		this.store = store;
		this.identifier = identifier;
	}

	public V findById(K id) {
		return safe.read(() -> map.get(id));
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
	
	public void add(@SuppressWarnings("unchecked") V... values) {
		add(Arrays.asList(values));
	}

	public void add(Iterable<V> values) {
		if (values == null) return;
		Map<K, V> map = new HashMap<>();
		for (V value : values) {
			K id = identifier.create(value);
			map.put(id, value);
		}
		safe.write(() -> this.map.putAll(map));
	}

	@Override
	public void load() throws IOException {
		if (store == null) return;
		add(store.load());
	}

	@Override
	public void save() throws IOException {
		if (store == null) return;
		Collection<V> values = safe.read(() -> new ArrayList<>(this.map.values()));
		store.save(values);
	}

	public void clear() {
		safe.write(() -> map.clear());
	}

}
