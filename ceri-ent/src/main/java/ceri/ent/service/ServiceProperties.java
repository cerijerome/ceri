package ceri.ent.service;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import ceri.common.property.TypedProperties;

/**
 * Properties to configure service cache settings.
 */
public class ServiceProperties extends TypedProperties.Ref {
	private static final String CACHE_KEY = "cache";
	private static final String DURATION_KEY = "duration";
	private static final String RANDOMIZE_KEY = "randomize";
	private static final String HOURS_KEY = "hours";
	private static final String DAYS_KEY = "days";
	private static final String MAX_ENTRIES_KEY = "max.entries";
	private static final String RETRIES_KEY = "retries";
	private static final String NULLS_KEY = "nulls";
	private static final String ALWAYS_SAVE_KEY = "always.save";
	private static final String FILE_KEY = "file";

	public ServiceProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public Path cacheFile() {
		return parse(CACHE_KEY, FILE_KEY).to(Path::of);
	}

	public Long cacheDurationMs() {
		Long hours = parse(CACHE_KEY, DURATION_KEY, HOURS_KEY).toLong();
		if (hours != null) return TimeUnit.HOURS.toMillis(hours);
		Long days = parse(CACHE_KEY, DURATION_KEY, DAYS_KEY).toLong();
		if (days != null) return TimeUnit.DAYS.toMillis(days);
		return null;
	}

	public Long cacheRandomizeMs() {
		Long hours = parse(CACHE_KEY, RANDOMIZE_KEY, HOURS_KEY).toLong();
		if (hours != null) return TimeUnit.HOURS.toMillis(hours);
		Long days = parse(CACHE_KEY, RANDOMIZE_KEY, DAYS_KEY).toLong();
		if (days != null) return TimeUnit.DAYS.toMillis(days);
		return null;
	}

	public Integer retries() {
		return parse(RETRIES_KEY).toInt();
	}

	public Integer maxEntries() {
		return parse(MAX_ENTRIES_KEY).toInt();
	}

	public Boolean cacheNulls() {
		return parse(CACHE_KEY, NULLS_KEY).toBool();
	}

	public Boolean alwaysSave() {
		return parse(ALWAYS_SAVE_KEY).toBool();
	}
}
