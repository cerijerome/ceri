package ceri.common.log;

import static ceri.common.log.Level.DEBUG;
import static ceri.common.log.Level.ERROR;
import static ceri.common.log.Level.INFO;
import static ceri.common.log.Level.TRACE;
import static ceri.common.log.Level.WARN;
import static ceri.common.log.Logger.FormatFlag.abbreviatePackage;
import static ceri.common.log.Logger.FormatFlag.noDate;
import static ceri.common.log.Logger.FormatFlag.noStackTrace;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ceri.common.collection.ImmutableUtil;
import ceri.common.date.DateUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionUtil;

/**
 * Simple logger for when a logging framework cannot be used. A logger is built with a global lookup
 * key. When requesting a log, if the key is not found, the default logger is returned.
 * 
 * Loggers can be specify the default output consumer. An optional error output can be specified,
 * with a level at which to write instead of the standard output. Log format is specified with
 * Formatter syntax. FormatFlags can be used to optimize unused log fields. The format field indexes
 * are:
 * 
 * <pre>
 *  1 = local date-time
 *  2 = thread name
 *  3 = log level
 *  4 = class:line stack trace
 *  5 = message
 * </pre>
 */
public class Logger {
	private static final int STACK_OFFSET = 3;
	private static final LocalDateTime NULL_DATE = DateUtil.UTC_EPOCH;
	public static final String FORMAT = "%1$tF %1$tT.%1$tL [%2$s] %3$-5s %4$s - %5$s";
	public static final Consumer<String> STDOUT = System.out::println;
	public static final Consumer<String> STDERR = System.err::println;
	private static final Map<Object, Logger> loggers = new HashMap<>();
	private static final Logger DEFAULT = new Builder(null).build();
	private final BiConsumer<Level, String> err;
	private final BiConsumer<Level, String> out;
	private final Level minLevel;
	private final Level errLevel;
	private final String format;
	private final Set<FormatFlag> flags;

	public static void main(String[] args) {
		Logger logger = Logger.logger();
		logger.error("This is an error: %d", 100);
		logger.info("Info message");
		logger.catching(new IOException("Hello"));
	}

	/**
	 * Flags to optimize message creation.
	 */
	public static enum FormatFlag {
		noDate, // don't evaluate date
		noStackTrace, // don't evaluate stack trace
		abbreviatePackage; // abbreviate class:line package name
	}

	public static class Builder {
		Object key;
		BiConsumer<Level, String> err = null;
		BiConsumer<Level, String> out = bi(STDOUT);
		Level minLevel = Level.INFO;
		Level errLevel = Level.ERROR;
		String format = FORMAT;
		final Collection<FormatFlag> flags = new LinkedHashSet<>();

		Builder(Object key) {
			this.key = key;
		}

		public Builder err(Consumer<String> err) {
			return err(bi(err));
		}

		public Builder err(BiConsumer<Level, String> err) {
			this.err = err;
			return this;
		}

		public Builder out(Consumer<String> out) {
			return out(bi(out));
		}

		public Builder out(BiConsumer<Level, String> out) {
			this.out = out;
			return this;
		}

		public Builder minLevel(Level minLevel) {
			this.minLevel = minLevel;
			return this;
		}

		public Builder errLevel(Level errLevel) {
			this.errLevel = errLevel;
			return this;
		}

		/**
		 * A standard Formatter format string. If the format has no date, adding flag noDate will
		 * improve performance. Similarly, if the format has no class:line, consider flag adding
		 * noStackTrace.
		 */
		public Builder format(String format) {
			this.format = format;
			return this;
		}

		public Builder flags(FormatFlag... flags) {
			return flags(Arrays.asList(flags));
		}

		public Builder flags(Collection<FormatFlag> flags) {
			this.flags.addAll(flags);
			return this;
		}

		public Logger build() {
			Logger manager = new Logger(this);
			if (key != null) loggers.put(key, manager);
			return manager;
		}
	}

	public static Builder builder(Object key) {
		Objects.requireNonNull(key);
		return new Builder(key);
	}

	public static Logger logger() {
		return DEFAULT;
	}

	public static Logger logger(Object key) {
		return loggers.getOrDefault(key, DEFAULT);
	}

	Logger(Builder builder) {
		out = builder.out;
		err = BasicUtil.defaultValue(builder.err, out);
		minLevel = builder.minLevel;
		errLevel = builder.errLevel;
		format = builder.format;
		flags = ImmutableUtil.copyAsSet(builder.flags);
	}

	public void trace(String format, Object... args) {
		log(TRACE, null, format, args);
	}

	public void debug(String format, Object... args) {
		log(DEBUG, null, format, args);
	}

	public void info(String format, Object... args) {
		log(INFO, null, format, args);
	}

	public void warn(String format, Object... args) {
		log(WARN, null, format, args);
	}

	public void error(String format, Object... args) {
		log(ERROR, null, format, args);
	}

	public void catching(Throwable t) {
		log(ERROR, t, null);
	}

	public void log(Level level, Throwable t) {
		log(level, t, null);
	}

	private void log(Level level, Throwable t, String format, Object... args) {
		if (!minLevel.valid(level)) return;
		BiConsumer<Level, String> consumer = consumer(level);
		if (consumer == null) return;
		String message = String.format(this.format, date(), thread(), level, classLine(),
			format(t, format, args));
		consumer.accept(level, message);
	}

	private BiConsumer<Level, String> consumer(Level level) {
		return errLevel.valid(level) ? err : out;
	}

	private LocalDateTime date() {
		if (flag(noDate)) return NULL_DATE;
		return LocalDateTime.now();
	}

	private String thread() {
		return Thread.currentThread().getName();
	}

	private String classLine() {
		if (flag(noStackTrace)) return "";
		String classLine = ReflectUtil.previousClassLine(STACK_OFFSET);
		if (flag(abbreviatePackage)) classLine = BasicUtil.abbreviatePackages(classLine);
		return classLine;
	}

	private String format(Throwable t, String format, Object... args) {
		if (t != null) return ExceptionUtil.stackTrace(t).trim();
		if (args.length == 0) return format;
		return String.format(format, args);
	}

	private boolean flag(FormatFlag flag) {
		return flags.contains(flag);
	}

	private static BiConsumer<Level, String> bi(Consumer<String> consumer) {
		return (l, s) -> consumer.accept(s);
	}

}
