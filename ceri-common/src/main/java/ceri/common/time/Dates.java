package ceri.common.time;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;

/**
 * Utility methods for dates and times.
 */
public class Dates {
	public static final int MICRO_NANOS = 1000;
	public static final int MILLI_MICROS = 1000;
	public static final int MILLI_NANOS = 1000000;
	public static final int SEC_MILLIS = 1000;
	public static final int SEC_MICROS = 1000000;
	public static final int SEC_NANOS = 1000000000;
	public static final LocalDateTime UTC_EPOCH = utcDateTime(0);
	private static final Map<TimeUnit, String> TIME_SYMBOLS = Map.of(TimeUnit.DAYS, "d",
		TimeUnit.HOURS, "h", TimeUnit.MINUTES, "m", TimeUnit.SECONDS, "s", TimeUnit.MILLISECONDS,
		"ms", TimeUnit.MICROSECONDS, "\u00b5s", TimeUnit.NANOSECONDS, "ns");
	private static final Map<String, TimeUnit> SYMBOL_TIMES = symbolTimes();

	private Dates() {}

	/**
	 * Returns the symbol for the time unit.
	 */
	public static String symbol(TimeUnit unit) {
		return TIME_SYMBOLS.get(unit);
	}

	/**
	 * Looks up the time unit for the symbol.
	 */
	public static TimeUnit timeUnit(String symbol) {
		return SYMBOL_TIMES.get(symbol.toLowerCase());
	}

	/**
	 * Returns the JVM up-time in milliseconds.
	 */
	public static long jvmUptimeMs() {
		return ManagementFactory.getRuntimeMXBean().getUptime();
	}

	/**
	 * Returns the number of seconds, with nanoseconds as a partial second.
	 */
	public static double seconds(Instant instant) {
		return seconds(instant.getEpochSecond(), instant.getNano());
	}

	/**
	 * Returns the number of seconds, with nanoseconds as a partial second.
	 */
	public static double seconds(Duration duration) {
		return seconds(duration.getSeconds(), duration.getNano());
	}

	/**
	 * Returns the number of microseconds, including rounded nanoseconds. Throws exception if long
	 * value overflows.
	 */
	public static long microsExact(Instant instant) {
		return microsExact(instant.getEpochSecond(), instant.getNano());
	}

	/**
	 * Returns the number of microseconds, including rounded nanoseconds. Throws exception if long
	 * value overflows.
	 */
	public static long microsExact(Duration duration) {
		return microsExact(duration.getSeconds(), duration.getNano());
	}

	/**
	 * Returns the number of milliseconds, including rounded nanoseconds. Throws exception if long
	 * value overflows.
	 */
	public static long millisExact(Instant instant) {
		return millisExact(instant.getEpochSecond(), instant.getNano());
	}

	/**
	 * Returns the number of milliseconds, including rounded nanoseconds. Throws exception if long
	 * value overflows.
	 */
	public static long millisExact(Duration duration) {
		return millisExact(duration.getSeconds(), duration.getNano());
	}

	/**
	 * Returns the epoch milliseconds from LocalDateTime in the given ZoneId.
	 */
	public static long epochMilli(LocalDateTime dateTime, ZoneId zoneId) {
		return dateTime.atZone(zoneId).toInstant().toEpochMilli();
	}

	/**
	 * Returns the epoch milliseconds from LocalDateTime in the system ZoneId.
	 */
	public static long epochMilli(LocalDateTime dateTime) {
		return epochMilli(dateTime, ZoneId.systemDefault());
	}

	/**
	 * Returns the date/time pattern for a locale. Use null for unwanted style. Can be used to
	 * modify a local date pattern, keeping the ordering, but modifying the field size.
	 */
	public static String dateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle,
		Locale locale) {
		return DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, timeStyle,
			IsoChronology.INSTANCE, locale);
	}

	/**
	 * Truncate to seconds.
	 */
	public static Instant truncSec(Instant instant) {
		return instant.truncatedTo(ChronoUnit.SECONDS);
	}

	/**
	 * Truncate to seconds.
	 */
	public static LocalDateTime truncSec(LocalDateTime dateTime) {
		return dateTime.truncatedTo(ChronoUnit.SECONDS);
	}

	/**
	 * Truncate to seconds.
	 */
	public static ZonedDateTime truncSec(ZonedDateTime dateTime) {
		return dateTime.truncatedTo(ChronoUnit.SECONDS);
	}

	/**
	 * Return the current local time truncated to seconds.
	 */
	public static LocalDateTime nowSec() {
		return truncSec(LocalDateTime.now());
	}

	/**
	 * Returns a local date-time in UTC from epoch milliseconds.
	 */
	public static LocalDateTime utcDateTime(long millis) {
		return dateTime(millis, ZoneOffset.UTC);
	}

	/**
	 * Returns a local date-time in UTC truncated to seconds, from epoch milliseconds.
	 */
	public static LocalDateTime utcDateTimeSec(long millis) {
		return dateTimeSec(millis, ZoneOffset.UTC);
	}

	/**
	 * Returns a local date-time in given time zone from epoch milliseconds.
	 */
	public static LocalDateTime dateTime(long millis, ZoneId zone) {
		return Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime();
	}

	/**
	 * Returns a local date-time in given time zone truncated to seconds, from epoch milliseconds.
	 */
	public static LocalDateTime dateTimeSec(long millis, ZoneId zone) {
		return truncSec(Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime());
	}

	/**
	 * Returns a local date-time in the system time zone from epoch milliseconds.
	 */
	public static LocalDateTime dateTime(long millis) {
		return dateTime(millis, ZoneId.systemDefault());
	}

	/**
	 * Returns a local date-time in the system time zone truncated to seconds, from epoch
	 * milliseconds.
	 */
	public static LocalDateTime dateTimeSec(long millis) {
		return truncSec(dateTime(millis));
	}

	/**
	 * Converts a duration into local time starting from 00:00.
	 */
	public static LocalTime toTime(Duration duration) {
		if (duration == null) return null;
		return LocalTime.MIN.plus(duration);
	}

	/**
	 * Converts a local time from 00:00 to a duration.
	 */
	public static Duration toDuration(LocalTime time) {
		if (time == null) return null;
		return Duration.between(LocalTime.MIN, time);
	}

	/**
	 * Formats epoch milliseconds to ISO date-time in UTC.
	 */
	public static String formatIso(long t) {
		return format(t, ZoneOffset.UTC, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	/**
	 * Formats epoch milliseconds to ISO date-time in default time zone.
	 */
	public static String formatLocalIso(long t) {
		return formatLocal(t, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	/**
	 * Formats epoch milliseconds to given format in default time zone.
	 */
	public static String formatLocal(long t, DateTimeFormatter formatter) {
		return format(t, ZoneId.systemDefault(), formatter);
	}

	/**
	 * Formats epoch milliseconds to given format in given time zone.
	 */
	public static String format(long t, ZoneId zoneId, DateTimeFormatter formatter) {
		Instant instant = Instant.ofEpochMilli(t).truncatedTo(ChronoUnit.SECONDS);
		return ZonedDateTime.ofInstant(instant, zoneId).format(formatter);
	}

	private static double seconds(long seconds, int nanos) {
		return seconds + (double) nanos / SEC_NANOS;
	}

	private static long microsExact(long seconds, int nanos) {
		return Math.addExact(Math.multiplyExact(seconds, SEC_MICROS), nanos / MICRO_NANOS);
	}

	private static long millisExact(long seconds, int nanos) {
		return Math.addExact(Math.multiplyExact(seconds, SEC_MILLIS), nanos / MILLI_NANOS);
	}

	private static Map<String, TimeUnit> symbolTimes() {
		var map = Maps.<String, TimeUnit>of();
		TIME_SYMBOLS.forEach((u, s) -> map.put(s, u));
		map.put("us", TimeUnit.MICROSECONDS);
		return Immutable.wrap(map);
	}
}
