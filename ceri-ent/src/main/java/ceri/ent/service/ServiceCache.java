package ceri.ent.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.FixedSizeCache;
import com.google.gson.reflect.TypeToken;

public class ServiceCache<K, V> implements Service<K, V>, Persistable {
	private static final Logger logger = LogManager.getLogger();
	public final long cacheDurationMs;
	private final int retries;
	private final boolean cacheNulls;
	private final Service<K, V> service;
	private final PersistentStore<Collection<Entry<K, V>>> store;
	private final SafeReadWrite safe = new SafeReadWrite();
	private final Map<K, Entry<K, V>> cache;

	public static <K, V> ServiceCache<K, V> create(Service<K, V> service,
		ServiceProperties properties, TypeToken<Collection<Entry<K, V>>> typeToken) {
		ServiceCache.Builder<K, V> builder = ServiceCache.builder(service);
		Long cacheDurationMs = properties.cacheDurationMs();
		if (cacheDurationMs != null) builder.cacheDurationMs(cacheDurationMs);
		Integer maxEntries = properties.maxEntries();
		if (maxEntries != null) builder.maxEntries(maxEntries);
		Boolean cacheNulls = properties.cacheNulls();
		if (cacheNulls != null) builder.cacheNulls(cacheNulls);
		Integer retries = properties.retries();
		if (retries != null) builder.retries(retries);
		File cacheFile = properties.cacheFile();
		if (cacheFile != null && typeToken != null) builder.store(typeToken, cacheFile);
		return builder.build();
	}

	public static class Builder<K, V> {
		final Service<K, V> service;
		long cacheDurationMs = TimeUnit.DAYS.toMillis(1);
		int maxEntries = 100000;
		int retries = 1;
		boolean cacheNulls = false;
		PersistentStore<Collection<Entry<K, V>>> store;

		Builder(Service<K, V> service) {
			this.service = service;
		}

		public Builder<K, V> cacheDurationMs(long cacheDurationMs) {
			this.cacheDurationMs = cacheDurationMs;
			return this;
		}

		public Builder<K, V> maxEntries(int maxEntries) {
			this.maxEntries = maxEntries;
			return this;
		}

		public Builder<K, V> retries(int retries) {
			this.retries = retries;
			return this;
		}

		public Builder<K, V> cacheNulls(boolean cacheNulls) {
			this.cacheNulls = cacheNulls;
			return this;
		}

		public Builder<K, V> store(TypeToken<Collection<Entry<K, V>>> typeToken, File file) {
			return store(JsonFileStore.create(typeToken, file));
		}

		public Builder<K, V> store(PersistentStore<Collection<Entry<K, V>>> store) {
			this.store = store;
			return this;
		}

		public ServiceCache<K, V> build() {
			return new ServiceCache<>(this);
		}
	}

	public static <K, V> Builder<K, V> builder(Service<K, V> service) {
		return new Builder<>(service);
	}

	ServiceCache(Builder<K, V> builder) {
		service = builder.service;
		cacheDurationMs = builder.cacheDurationMs;
		retries = builder.retries;
		cacheNulls = builder.cacheNulls;
		store = builder.store;
		cache = new FixedSizeCache<>(builder.maxEntries);
	}

	@Override
	public void load() throws IOException {
		if (store == null) return;
		Collection<Entry<K, V>> entries = store.load();
		if (entries == null) return;
		Map<K, Entry<K, V>> map = new HashMap<>();
		for (Iterator<Entry<K, V>> i = entries.iterator(); i.hasNext();) {
			Entry<K, V> entry = i.next();
			map.put(entry.key, entry);
		}
		cache.putAll(map);
	}

	public Collection<Entry<K, V>> entries() {
		return safe.read(() -> new HashSet<>(cache.values()));
	}

	@Override
	public void save() throws IOException {
		if (store == null) return;
		store.save(entries());
	}

	@Override
	public V retrieve(K key) throws ServiceException {
		Entry<K, V> entry = readFromCache(key);
		if (entry != null) return entry.value;
		V value = retrieveFromService(key);
		writeToCache(key, value);
		return value;
	}

	private boolean writeToCache(K key, V value) {
		if (value == null && !cacheNulls) return false;
		long expiration = System.currentTimeMillis() + cacheDurationMs;
		safe.write(() -> cache.put(key, new Entry<>(key, value, expiration)));
		return true;
	}

	private Entry<K, V> readFromCache(K key) {
		Entry<K, V> entry = safe.read(() -> cache.get(key));
		if (entry != null) {
			logger.debug("Entry found in cache: {}", key);
			if (!entry.expired()) return entry;
			logger.debug("Entry expired: {}", key);
		}
		return null;
	}

	private V retrieveFromService(K key) throws ServiceException {
		int retries = this.retries;
		while (true) {
			try {
				return service.retrieve(key);
			} catch (RuntimeException | ServiceException e) {
				if (retries <= 0) throw e;
				logger.warn("Service failed, retrying " + key, e);
				retries--;
			}
		}
	}

	public void clear(K key) {
		safe.write(() -> cache.remove(key));
	}

	public void clear() {
		safe.write(() -> cache.clear());
	}

}