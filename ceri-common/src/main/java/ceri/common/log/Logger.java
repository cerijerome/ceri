package ceri.common.log;

import static ceri.common.log.Level.ALL;
import static ceri.common.log.Level.DEBUG;
import static ceri.common.log.Level.ERROR;
import static ceri.common.log.Level.INFO;
import static ceri.common.log.Level.TRACE;
import static ceri.common.log.Level.WARN;
import static ceri.common.log.Logger.FormatFlag.abbreviatePackage;
import static ceri.common.log.Logger.FormatFlag.noDate;
import static ceri.common.log.Logger.FormatFlag.noStackTrace;
import static ceri.common.log.Logger.FormatFlag.noThread;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ceri.common.collection.ImmutableUtil;
import ceri.common.date.DateUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
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
 * 
 * Log methods always return null. This allows for callers to simplify multi-stage evaluations where
 * a failed evaluation results in null, and requires a message to be logged.
 */
public class Logger {
	private static final int STACK_OFFSET = 5;
	private static final LocalDateTime NULL_DATE = DateUtil.UTC_EPOCH;
	public static final String FORMAT = "%1$tF %1$tT.%1$tL [%2$s] %3$-5s %4$s - %5$s";
	public static final Consumer<String> STDOUT = s -> System.out.println(s); // allow for stdio
	public static final Consumer<String> STDERR = s -> System.err.println(s); // replacement
	private static final Map<Object, Logger> loggers = new ConcurrentHashMap<>();
	private static final Logger DEFAULT = new Builder(null).build();
	public final BiConsumer<Level, String> err;
	public final BiConsumer<Level, String> out;
	public final Level minLevel;
	public final Level errLevel;
	public final String format;
	public final int threadMax;
	public final Set<FormatFlag> flags;

	/**
	 * Flags to optimize message creation.
	 */
	public static enum FormatFlag {
		noDate, // don't evaluate date
		noThread, // don't evaluate thread
		noStackTrace, // don't evaluate stack trace
		abbreviatePackage; // abbreviate class:line package name
	}

	public static class Builder {
		Object key;
		BiConsumer<Level, String> err = bi(STDERR);
		BiConsumer<Level, String> out = bi(STDOUT);
		Level minLevel = Level.INFO;
		Level errLevel = Level.ERROR;
		String format = FORMAT;
		int threadMax = -1;
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

		public Builder threadMax(int threadMax) {
			this.threadMax = threadMax;
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

	/**
	 * Returns the default logger.
	 */
	public static Logger logger() {
		return DEFAULT;
	}

	/**
	 * Returns the logger with given key, or the default logger if the logger does not exist.
	 */
	public static Logger logger(Object key) {
		return loggers.getOrDefault(key, DEFAULT);
	}

	/**
	 * Removes a registered logger by its key. Returns false if no logger found.
	 */
	public static boolean removeLogger(Object key) {
		return loggers.remove(key) != null;
	}

	Logger(Builder builder) {
		out = builder.out;
		err = BasicUtil.defaultValue(builder.err, out);
		minLevel = builder.minLevel;
		errLevel = builder.errLevel;
		format = builder.format;
		threadMax = builder.threadMax;
		flags = ImmutableUtil.copyAsSet(builder.flags);
	}

	public <T> T trace(String format, Object... args) {
		return log(TRACE, null, format, args);
	}

	public <T> T debug(String format, Object... args) {
		return log(DEBUG, null, format, args);
	}

	public <T> T info(String format, Object... args) {
		return log(INFO, null, format, args);
	}

	public <T> T warn(String format, Object... args) {
		return log(WARN, null, format, args);
	}

	public <T> T error(String format, Object... args) {
		return log(ERROR, null, format, args);
	}

	public <T> T log(Level level, String format, Object... args) {
		return log(level, null, format, args);
	}

	public <T> T catching(Throwable t) {
		return log(ERROR, t, null);
	}

	public <T> T log(Level level, Throwable t) {
		return log(level, t, null);
	}

	private <T> T log(Level level, Throwable t, String format, Object... args) {
		if (!minLevel.valid(level)) return null;
		apply(level, formatMessage(t, format, args), consumer(level));
		return null;
	}

	private void apply(Level level, String message, BiConsumer<Level, String> consumer) {
		if (consumer == null) return;
		String line = formatLine(format, level, message);
		consumer.accept(level, line);
	}

	private String formatLine(String format, Level level, String message) {
		return String.format(format, date(), thread(), level, classLine(), message);
	}

	private BiConsumer<Level, String> consumer(Level level) {
		if (level == ALL) return out;
		return errLevel.valid(level) ? err : out;
	}

	private LocalDateTime date() {
		if (flag(noDate)) return NULL_DATE;
		return LocalDateTime.now();
	}

	private String thread() {
		if (flag(noThread) || threadMax == 0) return "";
		String s = Thread.currentThread().getName();
		if (threadMax > 0 && s.length() > threadMax) s = s.substring(0, threadMax);
		return s;
	}

	private String classLine() {
		if (flag(noStackTrace)) return "";
		String classLine = ReflectUtil.previousClassLine(STACK_OFFSET);
		if (flag(abbreviatePackage)) classLine = BasicUtil.abbreviatePackages(classLine);
		return classLine;
	}

	private String formatMessage(Throwable t, String format, Object... args) {
		if (t != null) return StringUtil.trim(ExceptionUtil.stackTrace(t));
		return StringUtil.format(format, args);
	}

	private boolean flag(FormatFlag flag) {
		return flags.contains(flag);
	}

	private static BiConsumer<Level, String> bi(Consumer<String> consumer) {
		return consumer == null ? null : (l, s) -> consumer.accept(s);
	}

}
