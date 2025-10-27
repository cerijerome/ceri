package ceri.common.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.Assert;

public class DatesTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Dates.class);
	}

	@Test
	public void testTimeUnitFromSymbol() {
		Assert.equal(Dates.timeUnit("xs"), null);
		Assert.equal(Dates.timeUnit("us"), TimeUnit.MICROSECONDS);
		Assert.equal(Dates.timeUnit("\u00b5s"), TimeUnit.MICROSECONDS);
		Assert.equal(Dates.timeUnit("\u00b5S"), TimeUnit.MICROSECONDS);
		Assert.equal(Dates.timeUnit("H"), TimeUnit.HOURS);
	}

	@Test
	public void testJvmUptime() {
		Assert.yes(Dates.jvmUptimeMs() > 0);
		Assert.yes(Dates.jvmUptimeMs() < TimeUnit.DAYS.toMillis(30 * 365));
	}

	@Test
	public void testSecondsFromInstant() {
		Assert.equal(Dates.seconds(Instant.ofEpochMilli(1000)), 1.0);
		Assert.equal(Dates.seconds(Instant.ofEpochMilli(123456789)), 123456.789);
	}

	@Test
	public void testSecondsFromDuration() {
		Assert.equal(Dates.seconds(Duration.ofSeconds(1, 0)), 1.0);
		Assert.equal(Dates.seconds(Duration.ofSeconds(123456, 123456789)), 123456.123456789);
	}

	@Test
	public void testMicrosExactFromInstant() {
		Assert.equal(Dates.microsExact(Instant.ofEpochMilli(1000)), 1000000L);
		Assert.equal(Dates.microsExact(Instant.ofEpochSecond(12345, 123456789)), 12345123456L);
		Assert.thrown(() -> Dates.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		Assert.thrown(
			() -> Dates.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMicrosExactFromDuration() {
		Assert.equal(Dates.microsExact(Duration.ofMillis(1000)), 1000000L);
		Assert.equal(Dates.microsExact(Duration.ofSeconds(12345, 123456789)), 12345123456L);
		Assert.thrown(() -> Dates.microsExact(Duration.ofSeconds(Long.MAX_VALUE)));
		Assert.thrown(
			() -> Dates.microsExact(Duration.ofSeconds(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMillisExactFromInstant() {
		Assert.equal(Dates.millisExact(Instant.ofEpochSecond(1000)), 1000000L);
		Assert.equal(Dates.millisExact(Instant.ofEpochSecond(12345, 123456789)), 12345123L);
		Assert.thrown(() -> Dates.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		Assert.thrown(
			() -> Dates.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testMillisExactFromDuration() {
		Assert.equal(Dates.millisExact(Duration.ofSeconds(1000)), 1000000L);
		Assert.equal(Dates.millisExact(Duration.ofSeconds(12345, 123456789)), 12345123L);
		Assert.thrown(() -> Dates.millisExact(Duration.ofSeconds(Long.MAX_VALUE)));
		Assert
			.thrown(() -> Dates.millisExact(Duration.ofSeconds(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testEpochMilli() {
		long t0 = Dates.epochMilli(LocalDateTime.now());
		long t1 = System.currentTimeMillis();
		Assert.yes(t1 - t0 < 1000);
	}

	@Test
	public void testDateTime() {
		LocalDateTime t0 = LocalDateTime.now();
		LocalDateTime t1 = Dates.dateTime(System.currentTimeMillis());
		LocalDateTime t2 = Dates.dateTimeSec(System.currentTimeMillis());
		Assert.yes(Duration.between(t0, t1).toMillis() < 1000);
		Assert.yes(Duration.between(t2, t1).toMillis() < 1000);
		Assert.equal(t2.getNano(), 0);
	}

	@Test
	public void testDateTimePattern() {
		// EEEE, MMMM d, y 'at' h:mm:ss a zzzz (eclipse)
		// EEEE, MMMM d, y, h:mm:ss\u202fa zzzz (jdk)
		Assert.match(Dates.dateTimePattern(FormatStyle.FULL, FormatStyle.FULL, Locale.US),
			"EEEE, MMMM d, y(,| 'at') h:mm:ss\\p{Zs}a zzzz");
	}

	@Test
	public void testTruncSec() {
		long millis = 1666666666666L;
		var instant = Instant.ofEpochMilli(millis);
		var zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		Assert.equal(instant.getNano(), 666000000);
		Assert.equal(Dates.truncSec(instant).getNano(), 0);
		Assert.equal(zdt.getNano(), 666000000);
		Assert.equal(Dates.truncSec(zdt).getNano(), 0);
	}

	@Test
	public void testNowSec() {
		Assert.equal(Dates.nowSec().get(ChronoField.MILLI_OF_SECOND), 0);
		Assert.equal(Dates.nowSec().get(ChronoField.MICRO_OF_SECOND), 0);
	}

	@Test
	public void testUtcDateTimeSec() {
		long millis = 1666666666666L;
		var t0 = Dates.utcDateTime(millis);
		var t1 = Dates.utcDateTimeSec(millis);
		Assert.equal(t0.getSecond(), 46);
		Assert.equal(t1.getSecond(), 46);
		Assert.equal(t0.getNano(), 666000000);
		Assert.equal(t1.getNano(), 0);
	}

	@Test
	public void testFormatIso() {
		Assert.equal(Dates.formatIso(0), "1970-01-01T00:00:00Z");
		Assert.equal(Dates.formatIso(-1000), "1969-12-31T23:59:59Z");
		Assert.equal(Dates.formatIso(-999), "1969-12-31T23:59:59Z");
		Assert.equal(Dates.formatIso(999), "1970-01-01T00:00:00Z");
		Assert.equal(Dates.formatIso(1000), "1970-01-01T00:00:01Z");
	}

	@Test
	public void testFormatLocalIso() {
		int offset = TimeZone.getDefault().getOffset(0);
		if (offset < 0) Assert.yes(Dates.formatLocalIso(0).startsWith("1969-12-31"));
		else Assert.yes(Dates.formatLocalIso(0).startsWith("1970-01-01"));
	}

	@Test
	public void testTimeToDuration() {
		Assert.equal(Dates.toDuration(LocalTime.of(2, 15)), Duration.ofMinutes(135));
		Assert.isNull(Dates.toDuration(null));
	}

	@Test
	public void testDurationToTime() {
		Assert.equal(Dates.toTime(Duration.ofMinutes(135)), LocalTime.of(2, 15));
		Assert.isNull(Dates.toTime(null));
	}
}
