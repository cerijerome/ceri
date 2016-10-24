package ceri.common.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

	private DateUtil() {}

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
