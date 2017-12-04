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
import ceri.common.math.MathUtil;
import ceri.ent.json.JsonCoder;

public class ServiceCache<K, V> implements Service<K, V>, Persistable {
	private static final Logger logger = LogManager.getLogger();
	private final String logName;
	private final long cacheDurationMs;
	private final long cacheRandomizeMs;
	private final int retries;
	private final boolean cacheNulls;
	private final Service<K, V> service;
	private final PersistentStore<Collection<Entry<K, V>>> store;
	private final SafeReadWrite safe = new SafeReadWrite();
	private final Map<K, Entry<K, V>> cache;

	public static <K, V> ServiceCache<K, V> create(Service<K, V> service,
		ServiceProperties properties, JsonCoder<Collection<Entry<K, V>>> coder) {
		return create(service, null, properties, coder);
	}

	public static <K, V> ServiceCache<K, V> create(Service<K, V> service, String logName,
		ServiceProperties properties, JsonCoder<Collection<Entry<K, V>>> coder) {
		ServiceCache.Builder<K, V> builder = ServiceCache.builder(service);
		if (logName != null) builder.logName(logName);
		Long cacheDurationMs = properties.cacheDurationMs();
		if (cacheDurationMs != null) builder.cacheDurationMs(cacheDurationMs);
		Long cacheRandomizeMs = properties.cacheRandomizeMs();
		if (cacheRandomizeMs != null) builder.cacheRandomizeMs(cacheRandomizeMs);
		Integer maxEntries = properties.maxEntries();
		if (maxEntries != null) builder.maxEntries(maxEntries);
		Boolean cacheNulls = properties.cacheNulls();
		if (cacheNulls != null) builder.cacheNulls(cacheNulls);
		Integer retries = properties.retries();
		if (retries != null) builder.retries(retries);
		File cacheFile = properties.cacheFile();
		if (cacheFile != null && coder != null) builder.store(coder, cacheFile);
		return builder.build();
	}

	public static class Builder<K, V> {
		final Service<K, V> service;
		String logName;
		long cacheDurationMs = TimeUnit.DAYS.toMillis(1);
		long cacheRandomizeMs = cacheDurationMs;
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

		public Builder<K, V> cacheRandomizeMs(long cacheRandomizeMs) {
			this.cacheRandomizeMs = cacheRandomizeMs;
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

		public Builder<K, V> logName(String logName) {
			this.logName = logName;
			return this;
		}

		public Builder<K, V> store(JsonCoder<Collection<Entry<K, V>>> coder, File file) {
			return store(JsonFileStore.create(coder, file));
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
		logName = logName(service, builder.logName);
		cacheDurationMs = builder.cacheDurationMs;
		cacheRandomizeMs = builder.cacheRandomizeMs;
		retries = builder.retries;
		cacheNulls = builder.cacheNulls;
		store = builder.store;
		cache = new FixedSizeCache<>(builder.maxEntries);
	}

	private String logName(Service<K, V> service, String logName) {
		if (logName != null) return logName;
		return service.getClass().getSimpleName();
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
		safe.write(() -> cache.put(key, new Entry<>(key, value, expiration())));
		return true;
	}

	private long expiration() {
		return System.currentTimeMillis() + cacheDurationMs + MathUtil.random(0, cacheRandomizeMs);
	}

	private Entry<K, V> readFromCache(K key) {
		Entry<K, V> entry = safe.read(() -> cache.get(key));
		if (entry != null) {
			logger.debug("{} entry found in cache: {}", logName, key);
			if (!entry.expired()) return entry;
			logger.debug("{} entry expired: {}", logName, key);
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
				logger.warn("{} request failed, retrying {}: {}", logName, key, e);
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
