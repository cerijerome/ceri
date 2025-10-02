package ceri.common.time;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
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

public class DatesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Dates.class);
	}

	@Test
	public void testTimeUnitFromSymbol() {
		assertEquals(Dates.timeUnit("xs"), null);
		assertEquals(Dates.timeUnit("us"), TimeUnit.MICROSECONDS);
		assertEquals(Dates.timeUnit("\u00b5s"), TimeUnit.MICROSECONDS);
		assertEquals(Dates.timeUnit("\u00b5S"), TimeUnit.MICROSECONDS);
		assertEquals(Dates.timeUnit("H"), TimeUnit.HOURS);
	}

	@Test
	public void testJvmUptime() {
		assertTrue(Dates.jvmUptimeMs() > 0);
		assertTrue(Dates.jvmUptimeMs() < TimeUnit.DAYS.toMillis(30 * 365));
	}

	@Test
	public void testSecondsFromInstant() {
		assertEquals(Dates.seconds(Instant.ofEpochMilli(1000)), 1.0);
		assertEquals(Dates.seconds(Instant.ofEpochMilli(123456789)), 123456.789);
	}

	@Test
	public void testSecondsFromDuration() {
		assertEquals(Dates.seconds(Duration.ofSeconds(1, 0)), 1.0);
		assertEquals(Dates.seconds(Duration.ofSeconds(123456, 123456789)), 123456.123456789);
	}

	@Test
	public void testMicrosExactFromInstant() {
		assertEquals(Dates.microsExact(Instant.ofEpochMilli(1000)), 1000000L);
		assertEquals(Dates.microsExact(Instant.ofEpochSecond(12345, 123456789)), 12345123456L);
		assertThrown(() -> Dates.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		assertThrown(() -> Dates
			.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMicrosExactFromDuration() {
		assertEquals(Dates.microsExact(Duration.ofMillis(1000)), 1000000L);
		assertEquals(Dates.microsExact(Duration.ofSeconds(12345, 123456789)), 12345123456L);
		assertThrown(() -> Dates.microsExact(Duration.ofSeconds(Long.MAX_VALUE)));
		assertThrown(
			() -> Dates.microsExact(Duration.ofSeconds(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMillisExactFromInstant() {
		assertEquals(Dates.millisExact(Instant.ofEpochSecond(1000)), 1000000L);
		assertEquals(Dates.millisExact(Instant.ofEpochSecond(12345, 123456789)), 12345123L);
		assertThrown(() -> Dates.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		assertThrown(
			() -> Dates.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testMillisExactFromDuration() {
		assertEquals(Dates.millisExact(Duration.ofSeconds(1000)), 1000000L);
		assertEquals(Dates.millisExact(Duration.ofSeconds(12345, 123456789)), 12345123L);
		assertThrown(() -> Dates.millisExact(Duration.ofSeconds(Long.MAX_VALUE)));
		assertThrown(
			() -> Dates.millisExact(Duration.ofSeconds(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testEpochMilli() {
		long t0 = Dates.epochMilli(LocalDateTime.now());
		long t1 = System.currentTimeMillis();
		assertTrue(t1 - t0 < 1000);
	}

	@Test
	public void testDateTime() {
		LocalDateTime t0 = LocalDateTime.now();
		LocalDateTime t1 = Dates.dateTime(System.currentTimeMillis());
		LocalDateTime t2 = Dates.dateTimeSec(System.currentTimeMillis());
		assertTrue(Duration.between(t0, t1).toMillis() < 1000);
		assertTrue(Duration.between(t2, t1).toMillis() < 1000);
		assertEquals(t2.getNano(), 0);
	}

	@Test
	public void testDateTimePattern() {
		// EEEE, MMMM d, y 'at' h:mm:ss a zzzz (eclipse)
		// EEEE, MMMM d, y, h:mm:ss\u202fa zzzz (jdk)
		assertMatch(Dates.dateTimePattern(FormatStyle.FULL, FormatStyle.FULL, Locale.US),
			"EEEE, MMMM d, y(,| 'at') h:mm:ss\\p{Zs}a zzzz");
	}

	@Test
	public void testTruncSec() {
		long millis = 1666666666666L;
		var instant = Instant.ofEpochMilli(millis);
		var zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		assertEquals(instant.getNano(), 666000000);
		assertEquals(Dates.truncSec(instant).getNano(), 0);
		assertEquals(zdt.getNano(), 666000000);
		assertEquals(Dates.truncSec(zdt).getNano(), 0);
	}

	@Test
	public void testNowSec() {
		assertEquals(Dates.nowSec().get(ChronoField.MILLI_OF_SECOND), 0);
		assertEquals(Dates.nowSec().get(ChronoField.MICRO_OF_SECOND), 0);
	}

	@Test
	public void testUtcDateTimeSec() {
		long millis = 1666666666666L;
		var t0 = Dates.utcDateTime(millis);
		var t1 = Dates.utcDateTimeSec(millis);
		assertEquals(t0.getSecond(), 46);
		assertEquals(t1.getSecond(), 46);
		assertEquals(t0.getNano(), 666000000);
		assertEquals(t1.getNano(), 0);
	}

	@Test
	public void testFormatIso() {
		assertEquals(Dates.formatIso(0), "1970-01-01T00:00:00Z");
		assertEquals(Dates.formatIso(-1000), "1969-12-31T23:59:59Z");
		assertEquals(Dates.formatIso(-999), "1969-12-31T23:59:59Z");
		assertEquals(Dates.formatIso(999), "1970-01-01T00:00:00Z");
		assertEquals(Dates.formatIso(1000), "1970-01-01T00:00:01Z");
	}

	@Test
	public void testFormatLocalIso() {
		int offset = TimeZone.getDefault().getOffset(0);
		if (offset < 0) assertTrue(Dates.formatLocalIso(0).startsWith("1969-12-31"));
		else assertTrue(Dates.formatLocalIso(0).startsWith("1970-01-01"));
	}

	@Test
	public void testTimeToDuration() {
		assertEquals(Dates.toDuration(LocalTime.of(2, 15)), Duration.ofMinutes(135));
		assertNull(Dates.toDuration(null));
	}

	@Test
	public void testDurationToTime() {
		assertEquals(Dates.toTime(Duration.ofMinutes(135)), LocalTime.of(2, 15));
		assertNull(Dates.toTime(null));
	}
}
