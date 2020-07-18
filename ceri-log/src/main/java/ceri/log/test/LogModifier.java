package ceri.log.test;

import static ceri.log.util.LogUtil.loggerName;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionRunnable;

/**
 * Use to temporarily change log level during tests, to prevent noise.
 */
public class LogModifier implements AutoCloseable {
	private final Map<String, Level> saved;

	/**
	 * Sets logger level, execute runnable, then resets level.
	 */
	public static <E extends Exception> void run(Class<?> logger, Level level,
		ExceptionRunnable<E> runnable) throws E {
		run(loggerName(logger), level, runnable);
	}

	/**
	 * Sets logger level, execute runnable, then resets level.
	 */
	public static <E extends Exception> void run(String logger, Level level,
		ExceptionRunnable<E> runnable) throws E {
		try (var modifier = builder().set(logger, level).build()) {
			runnable.run();
		}
	}

	public static class Builder {
		final Map<String, Level> levels = new LinkedHashMap<>();

		Builder() {}

		public Builder set(Class<?> cls, Level level) {
			return set(loggerName(cls), level);
		}

		public Builder set(String logger, Level level) {
			levels.put(logger, level);
			return this;
		}

		public LogModifier build() {
			return new LogModifier(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	LogModifier(Builder builder) {
		saved = saveLevels(builder.levels.keySet());
		setLevels(builder.levels);
	}

	@Override
	public void close() {
		setLevels(saved);
	}

	private Map<String, Level> saveLevels(Set<String> loggers) {
		return ImmutableUtil.convertAsMap(logger -> logger,
			logger -> LogManager.getLogger(logger).getLevel(), loggers);
	}

	private void setLevels(Map<String, Level> levels) {
		levels.forEach((logger, level) -> {
			Configurator.setLevel(logger, level);
		});
	}

}
