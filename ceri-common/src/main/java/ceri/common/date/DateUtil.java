package ceri.common.date;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
	public static final LocalDateTime UTC_EPOCH = utcDateTime(0);
		
	private DateUtil() {}

	public static LocalDateTime localDateTime(long millis) {
		return dateTime(millis, ZoneId.systemDefault());
	}
	
	public static LocalDateTime utcDateTime(long millis) {
		return dateTime(millis, ZoneOffset.UTC);
	}
	
	public static LocalDateTime dateTime(long millis, ZoneId zone) {
		return Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime();
	}
	
	public static LocalTime durationToTime(Duration duration) {
		if (duration == null) return null;
		return LocalTime.MIN.plus(duration);
	}

	public static Duration timeToDuration(LocalTime time) {
		if (time == null) return null;
		return Duration.between(LocalTime.MIN, time);
	}

	public static String formatIso(long t) {
		return format(t, ZoneOffset.UTC, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	public static String formatLocalIso(long t) {
		return formatLocal(t, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	public static String formatLocal(long t, DateTimeFormatter formatter) {
		return format(t, ZoneId.systemDefault(), formatter);
	}

	public static String format(long t, ZoneId zoneId, DateTimeFormatter formatter) {
		Instant instant = Instant.ofEpochMilli(t).truncatedTo(ChronoUnit.SECONDS);
		return ZonedDateTime.ofInstant(instant, zoneId).format(formatter);
	}

}
