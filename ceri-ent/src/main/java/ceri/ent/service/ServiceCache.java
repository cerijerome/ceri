package ceri.ent.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.Maps;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.common.property.TypedProperties;
import ceri.ent.json.JsonCoder;

/**
 * A caching layer over a service, with persistence.
 */
public class ServiceCache<K, V> implements Service<K, V>, Persistable {
	private static final Logger logger = LogManager.getLogger();
	public final String name;
	public final Config config;
	private final Service<K, V> service;
	private final PersistentStore<Collection<Entry<K, V>>> store;
	private final SafeReadWrite safe = SafeReadWrite.of();
	private final Map<K, Entry<K, V>> cache;
	private boolean modified = false;

	public static class Config {
		public final Path cacheFile;
		public final long cacheDurationMs;
		public final long cacheRandomizeMs;
		public final int maxEntries;
		public final int retries;
		public final boolean cacheNulls;
		public final boolean alwaysSave;

		public static class Builder {
			Path cacheFile = null;
			long cacheDurationMs = TimeUnit.DAYS.toMillis(1);
			long cacheRandomizeMs = cacheDurationMs;
			int maxEntries = 100000;
			int retries = 1;
			boolean cacheNulls = false;
			boolean alwaysSave = false;

			private Builder() {}

			public Builder cacheFile(Path cacheFile) {
				this.cacheFile = cacheFile;
				return this;
			}

			public Builder cacheDurationMs(long cacheDurationMs) {
				this.cacheDurationMs = cacheDurationMs;
				return this;
			}

			public Builder cacheRandomizeMs(long cacheRandomizeMs) {
				this.cacheRandomizeMs = cacheRandomizeMs;
				return this;
			}

			public Builder maxEntries(int maxEntries) {
				this.maxEntries = maxEntries;
				return this;
			}

			public Builder retries(int retries) {
				this.retries = retries;
				return this;
			}

			public Builder cacheNulls(boolean cacheNulls) {
				this.cacheNulls = cacheNulls;
				return this;
			}

			public Builder alwaysSave(boolean alwaysSave) {
				this.alwaysSave = alwaysSave;
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			cacheFile = builder.cacheFile;
			cacheDurationMs = builder.cacheDurationMs;
			cacheRandomizeMs = builder.cacheRandomizeMs;
			maxEntries = builder.maxEntries;
			retries = builder.retries;
			cacheNulls = builder.cacheNulls;
			alwaysSave = builder.alwaysSave;
		}

		public <T> PersistentStore<T> store(JsonCoder<T> coder) {
			if (coder == null || cacheFile == null) return null;
			return JsonFileStore.create(coder, cacheFile);
		}
	}

	public static class Properties extends TypedProperties.Ref {
		private static final String FILE_KEY = "file";
		private static final String CACHE_KEY = "cache";
		private static final String DURATION_KEY = "duration";
		private static final String RANDOMIZE_KEY = "randomize";
		private static final String HOURS_KEY = "hours";
		private static final String DAYS_KEY = "days";
		private static final String MAX_ENTRIES_KEY = "max.entries";
		private static final String RETRIES_KEY = "retries";
		private static final String NULLS_KEY = "nulls";
		private static final String ALWAYS_SAVE_KEY = "always.save";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			var b = Config.builder();
			parse(CACHE_KEY, FILE_KEY).as(Path::of).accept(b::cacheFile);
			cacheMs(DURATION_KEY, b::cacheDurationMs);
			cacheMs(RANDOMIZE_KEY, b::cacheRandomizeMs);
			parse(MAX_ENTRIES_KEY).asInt().accept(b::maxEntries);
			parse(RETRIES_KEY).asInt().accept(b::retries);
			parse(CACHE_KEY, NULLS_KEY).asBool().accept(b::cacheNulls);
			parse(ALWAYS_SAVE_KEY).asBool().accept(b::alwaysSave);
			return b.build();
		}

		private void cacheMs(String key, Functions.Consumer<Long> consumer) {
			if (parse(CACHE_KEY, key, HOURS_KEY).asLong()
				.accept(hours -> consumer.accept(TimeUnit.HOURS.toMillis(hours)))) return;
			parse(CACHE_KEY, key, DAYS_KEY).asLong()
				.accept(days -> consumer.accept(TimeUnit.DAYS.toMillis(days)));
		}
	}

	public static <K, V> ServiceCache<K, V> of(String name, Config config, Service<K, V> service,
		JsonCoder<Collection<Entry<K, V>>> coder) {
		return of(name, config, service, config.store(coder));
	}

	public static <K, V> ServiceCache<K, V> of(String name, Config config, Service<K, V> service,
		PersistentStore<Collection<Entry<K, V>>> store) {
		return new ServiceCache<>(name, config, service, store);
	}

	private ServiceCache(String name, Config config, Service<K, V> service,
		PersistentStore<Collection<Entry<K, V>>> store) {
		this.name = name;
		this.config = config;
		this.service = service;
		this.store = store;
		cache = Maps.cache(config.maxEntries);
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
		if (config.alwaysSave) return true;
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
		if (entries == null) return Map.of();
		Map<K, Entry<K, V>> map = new HashMap<>();
		for (Entry<K, V> entry : entries)
			map.put(entry.key, entry);
		return map;
	}

	private boolean writeToCache(K key, V value) {
		if (value == null && !config.cacheNulls) return false;
		safe.write(() -> {
			cache.put(key, new Entry<>(key, value, expiration()));
			modified = true;
		});
		return true;
	}

	private long expiration() {
		return System.currentTimeMillis() + config.cacheDurationMs
			+ MathUtil.random(0, config.cacheRandomizeMs);
	}

	private Entry<K, V> readFromCache(K key) {
		Entry<K, V> entry = safe.read(() -> cache.get(key));
		if (entry != null) {
			logger.trace("{} entry found in cache: {}", name, key);
			if (!entry.expired()) return entry;
			logger.debug("{} entry expired: {}", name, key);
		}
		return null;
	}

	private V retrieveFromService(K key) throws ServiceException {
		int retries = config.retries;
		while (true) {
			try {
				return service.retrieve(key);
			} catch (RuntimeException | ServiceException e) {
				if (retries <= 0) throw e;
				logger.warn("{} request failed, retrying {}: {}", name, key, e);
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
