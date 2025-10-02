package ceri.common.log;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.common.time.Dates;
import ceri.common.util.Basics;

/**
 * Simple logger for when a logging framework cannot be used. A logger is built with a global lookup
 * key. When requesting a log, if the key is not found, the default logger is returned. Loggers can
 * specify the default output consumer. An optional error output can be specified, with a level at
 * which to write instead of the standard output. Log format is specified with Formatter syntax.
 * FormatFlags can be used to optimize unused log fields. The format field indexes are:
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
	private static final LocalDateTime NULL_DATE = Dates.UTC_EPOCH;
	public static final String FORMAT = "%1$tF %1$tT.%1$tL [%2$s] %3$-5s %4$s - %5$s";
	public static final Functions.Consumer<String> STDOUT = s -> System.out.println(s);
	public static final Functions.Consumer<String> STDERR = s -> System.err.println(s);
	private static final Map<Object, Logger> loggers = Maps.concurrent();
	private static final Logger DEFAULT = new Builder(null).build();
	public final Functions.BiConsumer<Level, String> err;
	public final Functions.BiConsumer<Level, String> out;
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
		Functions.BiConsumer<Level, String> err = bi(STDERR);
		Functions.BiConsumer<Level, String> out = bi(STDOUT);
		Level minLevel = Level.INFO;
		Level errLevel = Level.ERROR;
		String format = FORMAT;
		int threadMax = -1;
		final Collection<FormatFlag> flags = Sets.link();

		Builder(Object key) {
			this.key = key;
		}

		public Builder err(Functions.Consumer<String> err) {
			return err(bi(err));
		}

		public Builder err(Functions.BiConsumer<Level, String> err) {
			this.err = err;
			return this;
		}

		public Builder out(Functions.Consumer<String> out) {
			return out(bi(out));
		}

		public Builder out(Functions.BiConsumer<Level, String> out) {
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
		err = Basics.def(builder.err, out);
		minLevel = builder.minLevel;
		errLevel = builder.errLevel;
		format = builder.format;
		threadMax = builder.threadMax;
		flags = Immutable.set(builder.flags);
	}

	public <T> T trace(String format, Object... args) {
		return log(Level.TRACE, null, format, args);
	}

	public <T> T debug(String format, Object... args) {
		return log(Level.DEBUG, null, format, args);
	}

	public <T> T info(String format, Object... args) {
		return log(Level.INFO, null, format, args);
	}

	public <T> T warn(String format, Object... args) {
		return log(Level.WARN, null, format, args);
	}

	public <T> T error(String format, Object... args) {
		return log(Level.ERROR, null, format, args);
	}

	public <T> T log(Level level, String format, Object... args) {
		return log(level, null, format, args);
	}

	public <T> T catching(Throwable t) {
		return log(Level.ERROR, t, null);
	}

	public <T> T log(Level level, Throwable t) {
		return log(level, t, null);
	}

	private <T> T log(Level level, Throwable t, String format, Object... args) {
		if (!minLevel.valid(level)) return null;
		apply(level, formatMessage(t, format, args), consumer(level));
		return null;
	}

	private void apply(Level level, String message, Functions.BiConsumer<Level, String> consumer) {
		if (consumer == null) return;
		var line = formatLine(format, level, message);
		consumer.accept(level, line);
	}

	private String formatLine(String format, Level level, String message) {
		return String.format(format, date(), thread(), level, classLine(), message);
	}

	private Functions.BiConsumer<Level, String> consumer(Level level) {
		if (level == Level.ALL) return out;
		return errLevel.valid(level) ? err : out;
	}

	private LocalDateTime date() {
		if (flag(Logger.FormatFlag.noDate)) return NULL_DATE;
		return LocalDateTime.now();
	}

	private String thread() {
		if (flag(Logger.FormatFlag.noThread) || threadMax == 0) return "";
		var s = Thread.currentThread().getName();
		if (threadMax > 0 && s.length() > threadMax) s = s.substring(0, threadMax);
		return s;
	}

	private String classLine() {
		if (flag(Logger.FormatFlag.noStackTrace)) return "";
		var classLine = Reflect.previousClassLine(STACK_OFFSET);
		if (flag(Logger.FormatFlag.abbreviatePackage))
			classLine = Reflect.abbreviatePackages(classLine);
		return classLine;
	}

	private String formatMessage(Throwable t, String format, Object... args) {
		if (t != null) return Strings.trim(Exceptions.stackTrace(t));
		return Strings.format(format, args);
	}

	private boolean flag(FormatFlag flag) {
		return flags.contains(flag);
	}

	private static Functions.BiConsumer<Level, String> bi(Functions.Consumer<String> consumer) {
		return consumer == null ? null : (_, s) -> consumer.accept(s);
	}
}
