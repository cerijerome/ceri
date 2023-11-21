package ceri.ent.service;

import static ceri.common.function.FunctionUtil.safeAccept;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.CollectionUtil;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.math.MathUtil;
import ceri.ent.json.JsonCoder;

/**
 * A caching layer over a service, with persistence.
 */
public class ServiceCache<K, V> implements Service<K, V>, Persistable {
	private static final Logger logger = LogManager.getLogger();
	private final String logName;
	private final long cacheDurationMs;
	private final long cacheRandomizeMs;
	private final int retries;
	private final boolean cacheNulls;
	private final boolean alwaysSave;
	private final Service<K, V> service;
	private final PersistentStore<Collection<Entry<K, V>>> store;
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final Map<K, Entry<K, V>> cache;
	private boolean modified = false;

	public static <K, V> ServiceCache<K, V> create(Service<K, V> service,
		ServiceProperties properties, JsonCoder<Collection<Entry<K, V>>> coder) {
		return create(service, null, properties, coder);
	}

	public static <K, V> ServiceCache<K, V> create(Service<K, V> service, String logName,
		ServiceProperties properties, JsonCoder<Collection<Entry<K, V>>> coder) {
		ServiceCache.Builder<K, V> builder = ServiceCache.builder(service);
		if (logName != null) builder.logName(logName);
		safeAccept(properties.cacheDurationMs(), builder::cacheDurationMs);
		safeAccept(properties.cacheRandomizeMs(), builder::cacheRandomizeMs);
		safeAccept(properties.maxEntries(), builder::maxEntries);
		safeAccept(properties.cacheNulls(), builder::cacheNulls);
		safeAccept(properties.alwaysSave(), builder::alwaysSave);
		safeAccept(properties.retries(), builder::retries);
		if (coder != null) safeAccept( //
			properties.cacheFile(), cacheFile -> builder.store(coder, cacheFile));
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
		boolean alwaysSave = false;
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

		public Builder<K, V> alwaysSave(boolean alwaysSave) {
			this.alwaysSave = alwaysSave;
			return this;
		}

		public Builder<K, V> logName(String logName) {
			this.logName = logName;
			return this;
		}

		public Builder<K, V> store(JsonCoder<Collection<Entry<K, V>>> coder, Path file) {
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
		alwaysSave = builder.alwaysSave;
		store = builder.store;
		cache = CollectionUtil.fixedSizeCache(builder.maxEntries);
	}

	private String logName(Service<K, V> service, String logName) {
		if (logName != null) return logName;
		return service.getClass().getSimpleName();
	}

	@Override
	public void load() throws IOException {
		if (store == null) return;
		Map<K, Entry<K, V>> map = toMap(store.load());
		safe.write(() -> {
			cache.putAll(map);
			modified = false;
		});
	}

	public Collection<Entry<K, V>> entries() {
		return safe.read(() -> new HashSet<>(cache.values()));
	}

	@Override
	public void save() throws IOException {
		if (!saveEntries()) return;
		Collection<Entry<K, V>> values = safe.read(() -> new HashSet<>(cache.values()));
		store.save(values);
		safe.write(() -> modified = false); // but entries may have been written after read...
	}

	private boolean saveEntries() {
		if (store == null) return false;
		if (alwaysSave) return true;
		return safe.read(() -> modified);
	}

	@Override
	public V retrieve(K key) throws ServiceException {
		Entry<K, V> entry = readFromCache(key);
		if (entry != null) return entry.value;
		V value = retrieveFromService(key);
		writeToCache(key, value);
		return value;
	}

	private Map<K, Entry<K, V>> toMap(Collection<Entry<K, V>> entries) {
		if (entries == null) return Collections.emptyMap();
		Map<K, Entry<K, V>> map = new HashMap<>();
		for (Entry<K, V> entry : entries)
			map.put(entry.key, entry);
		return map;
	}

	private boolean writeToCache(K key, V value) {
		if (value == null && !cacheNulls) return false;
		safe.write(() -> {
			cache.put(key, new Entry<>(key, value, expiration()));
			modified = true;
		});
		return true;
	}

	private long expiration() {
		return System.currentTimeMillis() + cacheDurationMs + MathUtil.random(0, cacheRandomizeMs);
	}

	private Entry<K, V> readFromCache(K key) {
		Entry<K, V> entry = safe.read(() -> cache.get(key));
		if (entry != null) {
			logger.trace("{} entry found in cache: {}", logName, key);
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
		safe.write(() -> {
			if (cache.remove(key) != null) modified = true;
		});
	}

	public void clear() {
		safe.write(() -> {
			if (!cache.isEmpty()) modified = true;
			cache.clear();
		});
	}

}
