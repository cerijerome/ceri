package ceri.common.date;

import static java.util.concurrent.TimeUnit.*;
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

/**
 * Utility methods for dates and times.
 */
public class DateUtil {
	public static final LocalDateTime UTC_EPOCH = utcDateTime(0);
	private static final Map<TimeUnit, String> TIME_SYMBOLS = Map.of(DAYS, "d", HOURS, "h", MINUTES,
		"m", SECONDS, "s", MILLISECONDS, "ms", MICROSECONDS, "\u00b5s", NANOSECONDS, "ns");

	private DateUtil() {}

	/**
	 * Returns the symbol for the time unit.
	 */
	public static String symbol(TimeUnit unit) {
		return TIME_SYMBOLS.get(unit);
	}

	/**
	 * Returns the JVM up-time in milliseconds.
	 */
	public static long jvmUptimeMs() {
		return ManagementFactory.getRuntimeMXBean().getUptime();
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
	 * Return the current local time truncated to seconds.
	 */
	public static LocalDateTime nowSec() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}
	
	/**
	 * Returns a local date-time in UTC from epoch milliseconds.
	 */
	public static LocalDateTime utcDateTime(long millis) {
		return dateTime(millis, ZoneOffset.UTC);
	}

	/**
	 * Returns a local date-time in given time zone from epoch milliseconds.
	 */
	public static LocalDateTime dateTime(long millis, ZoneId zone) {
		return Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime();
	}

	/**
	 * Returns a local date-time in the system time zone from epoch milliseconds.
	 */
	public static LocalDateTime dateTime(long millis) {
		return dateTime(millis, ZoneId.systemDefault());
	}

	/**
	 * Converts a duration into local time starting from 00:00.
	 */
	public static LocalTime durationToTime(Duration duration) {
		if (duration == null) return null;
		return LocalTime.MIN.plus(duration);
	}

	/**
	 * Converts a local time from 00:00 to a duration.
	 */
	public static Duration timeToDuration(LocalTime time) {
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

}
