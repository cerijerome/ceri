package ceri.ent.service;

import java.io.File;
import java.util.concurrent.TimeUnit;
import ceri.common.property.BaseProperties;

/**
 * Properties to configure service cache settings.
 */
public class ServiceProperties extends BaseProperties {
	private static final String CACHE_KEY = "cache";
	private static final String DURATION_KEY = "duration";
	private static final String HOURS_KEY = "hours";
	private static final String DAYS_KEY = "days";
	private static final String MAX_ENTRIES_KEY = "max.entries";
	private static final String RETRIES_KEY = "retries";
	private static final String NULLS_KEY = "nulls";
	private static final String FILE_KEY = "file";
	
	public ServiceProperties(BaseProperties properties, String...groups) {
		super(properties, groups);
	}

	public File cacheFile() {
		String fileName = value(CACHE_KEY, FILE_KEY);
		if (fileName == null) return null;
		return new File(fileName);
	}
	
	public Long cacheDurationMs() {
		Long hours = cacheDurationHours();
		if (hours != null) return TimeUnit.HOURS.toMillis(hours);
		Long days = cacheDurationDays();
		if (days != null) return TimeUnit.DAYS.toMillis(days);
		return null;
	}
	
	private Long cacheDurationDays() {
		return longValue(CACHE_KEY, DURATION_KEY, DAYS_KEY);
	}

	private Long cacheDurationHours() {
		return longValue(CACHE_KEY, DURATION_KEY, HOURS_KEY);
	}

	public Integer retries() {
		return intValue(RETRIES_KEY);
	}

	public Integer maxEntries() {
		return intValue(MAX_ENTRIES_KEY);
	}

	public Boolean cacheNulls() {
		return booleanValue(CACHE_KEY, NULLS_KEY);
	}
	
}