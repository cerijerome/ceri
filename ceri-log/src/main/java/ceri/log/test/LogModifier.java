package ceri.log.test;

import static ceri.log.util.LogUtil.loggerName;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.Excepts.Runnable;

/**
 * Use to temporarily change log level during tests, to prevent noise.
 */
public class LogModifier implements AutoCloseable {
	private final Map<String, Level> saved;

	/**
	 * Sets logger level, without reverting. Useful for standalone tests.
	 */
	@SuppressWarnings("resource")
	public static void set(Level level, Class<?>... loggers) {
		of(level, loggers);
	}

	/**
	 * Sets logger level, execute runnable, then resets level.
	 */
	public static <E extends Exception> void run(Runnable<E> runnable, Level level,
		Class<?>... loggers) throws E {
		try (var _ = of(level, loggers)) {
			runnable.run();
		}
	}

	/**
	 * Sets logger level, execute runnable, then resets level.
	 */
	public static <E extends Exception> void run(Runnable<E> runnable, Level level,
		String... loggers) throws E {
		try (var _ = of(level, loggers)) {
			runnable.run();
		}
	}

	/**
	 * Returns a closeable instance that sets level for given loggers.
	 */
	public static LogModifier of(Level level, Class<?>... loggers) {
		return builder().set(level, loggers).build();
	}

	/**
	 * Returns a closeable instance that sets level for given loggers.
	 */
	public static LogModifier of(Level level, String... loggers) {
		return builder().set(level, loggers).build();
	}

	public static class Builder {
		final Map<String, Level> levels = new LinkedHashMap<>();

		Builder() {}

		public Builder set(Level level, Class<?>... classes) {
			for (Class<?> cls : classes)
				set(level, loggerName(cls));
			return this;
		}

		public Builder set(Level level, String... loggers) {
			for (String logger : loggers)
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
