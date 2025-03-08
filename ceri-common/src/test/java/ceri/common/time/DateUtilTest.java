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

public class DateUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(DateUtil.class);
	}

	@Test
	public void testTimeUnitFromSymbol() {
		assertEquals(DateUtil.timeUnit("xs"), null);
		assertEquals(DateUtil.timeUnit("us"), TimeUnit.MICROSECONDS);
		assertEquals(DateUtil.timeUnit("\u00b5s"), TimeUnit.MICROSECONDS);
		assertEquals(DateUtil.timeUnit("\u00b5S"), TimeUnit.MICROSECONDS);
		assertEquals(DateUtil.timeUnit("H"), TimeUnit.HOURS);
	}

	@Test
	public void testJvmUptime() {
		assertTrue(DateUtil.jvmUptimeMs() > 0);
		assertTrue(DateUtil.jvmUptimeMs() < TimeUnit.DAYS.toMillis(30 * 365));
	}

	@Test
	public void testSecondsFromInstant() {
		assertEquals(DateUtil.seconds(Instant.ofEpochMilli(1000)), 1.0);
		assertEquals(DateUtil.seconds(Instant.ofEpochMilli(123456789)), 123456.789);
	}

	@Test
	public void testSecondsFromDuration() {
		assertEquals(DateUtil.seconds(Duration.ofSeconds(1, 0)), 1.0);
		assertEquals(DateUtil.seconds(Duration.ofSeconds(123456, 123456789)), 123456.123456789);
	}

	@Test
	public void testMicrosExactFromInstant() {
		assertEquals(DateUtil.microsExact(Instant.ofEpochMilli(1000)), 1000000L);
		assertEquals(DateUtil.microsExact(Instant.ofEpochSecond(12345, 123456789)), 12345123456L);
		assertThrown(() -> DateUtil.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		assertThrown(() -> DateUtil
			.microsExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMicrosExactFromDuration() {
		assertEquals(DateUtil.microsExact(Duration.ofMillis(1000)), 1000000L);
		assertEquals(DateUtil.microsExact(Duration.ofSeconds(12345, 123456789)), 12345123456L);
		assertThrown(() -> DateUtil.microsExact(Duration.ofSeconds(Long.MAX_VALUE)));
		assertThrown(
			() -> DateUtil.microsExact(Duration.ofSeconds(Long.MAX_VALUE / 1000000L, 999999000)));
	}

	@Test
	public void testMillisExactFromInstant() {
		assertEquals(DateUtil.millisExact(Instant.ofEpochSecond(1000)), 1000000L);
		assertEquals(DateUtil.millisExact(Instant.ofEpochSecond(12345, 123456789)), 12345123L);
		assertThrown(() -> DateUtil.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE)));
		assertThrown(
			() -> DateUtil.millisExact(Instant.ofEpochSecond(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testMillisExactFromDuration() {
		assertEquals(DateUtil.millisExact(Duration.ofSeconds(1000)), 1000000L);
		assertEquals(DateUtil.millisExact(Duration.ofSeconds(12345, 123456789)), 12345123L);
		assertThrown(() -> DateUtil.millisExact(Duration.ofSeconds(Long.MAX_VALUE)));
		assertThrown(
			() -> DateUtil.millisExact(Duration.ofSeconds(Long.MAX_VALUE / 1000L, 999000000)));
	}

	@Test
	public void testEpochMilli() {
		long t0 = DateUtil.epochMilli(LocalDateTime.now());
		long t1 = System.currentTimeMillis();
		assertTrue(t1 - t0 < 1000);
	}

	@Test
	public void testDateTime() {
		LocalDateTime t0 = LocalDateTime.now();
		LocalDateTime t1 = DateUtil.dateTime(System.currentTimeMillis());
		LocalDateTime t2 = DateUtil.dateTimeSec(System.currentTimeMillis());
		assertTrue(Duration.between(t0, t1).toMillis() < 1000);
		assertTrue(Duration.between(t2, t1).toMillis() < 1000);
		assertEquals(t2.getNano(), 0);
	}

	@Test
	public void testDateTimePattern() {
		// EEEE, MMMM d, y 'at' h:mm:ss a zzzz (eclipse)
		// EEEE, MMMM d, y, h:mm:ss\u202fa zzzz (jdk)
		assertMatch(DateUtil.dateTimePattern(FormatStyle.FULL, FormatStyle.FULL, Locale.US),
			"EEEE, MMMM d, y(,| 'at') h:mm:ss\\p{Zs}a zzzz");
	}

	@Test
	public void testTruncSec() {
		long millis = 1666666666666L;
		var instant = Instant.ofEpochMilli(millis);
		var zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		assertEquals(instant.getNano(), 666000000);
		assertEquals(DateUtil.truncSec(instant).getNano(), 0);
		assertEquals(zdt.getNano(), 666000000);
		assertEquals(DateUtil.truncSec(zdt).getNano(), 0);
	}

	@Test
	public void testNowSec() {
		assertEquals(DateUtil.nowSec().get(ChronoField.MILLI_OF_SECOND), 0);
		assertEquals(DateUtil.nowSec().get(ChronoField.MICRO_OF_SECOND), 0);
	}

	@Test
	public void testUtcDateTimeSec() {
		long millis = 1666666666666L;
		var t0 = DateUtil.utcDateTime(millis);
		var t1 = DateUtil.utcDateTimeSec(millis);
		assertEquals(t0.getSecond(), 46);
		assertEquals(t1.getSecond(), 46);
		assertEquals(t0.getNano(), 666000000);
		assertEquals(t1.getNano(), 0);
	}

	@Test
	public void testFormatIso() {
		assertEquals(DateUtil.formatIso(0), "1970-01-01T00:00:00Z");
		assertEquals(DateUtil.formatIso(-1000), "1969-12-31T23:59:59Z");
		assertEquals(DateUtil.formatIso(-999), "1969-12-31T23:59:59Z");
		assertEquals(DateUtil.formatIso(999), "1970-01-01T00:00:00Z");
		assertEquals(DateUtil.formatIso(1000), "1970-01-01T00:00:01Z");
	}

	@Test
	public void testFormatLocalIso() {
		int offset = TimeZone.getDefault().getOffset(0);
		if (offset < 0) assertTrue(DateUtil.formatLocalIso(0).startsWith("1969-12-31"));
		else assertTrue(DateUtil.formatLocalIso(0).startsWith("1970-01-01"));
	}

	@Test
	public void testTimeToDuration() {
		assertEquals(DateUtil.toDuration(LocalTime.of(2, 15)), Duration.ofMinutes(135));
		assertNull(DateUtil.toDuration(null));
	}

	@Test
	public void testDurationToTime() {
		assertEquals(DateUtil.toTime(Duration.ofMinutes(135)), LocalTime.of(2, 15));
		assertNull(DateUtil.toTime(null));
	}

}
