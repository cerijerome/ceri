package ceri.common.time;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
	public void testJvmUptime() {
		assertThat(DateUtil.jvmUptimeMs() > 0, is(true));
		assertThat(DateUtil.jvmUptimeMs() < TimeUnit.DAYS.toMillis(30 * 365), is(true));
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
		assertTrue(Duration.between(t0, t1).toMillis() < 1000);
	}

	@Test
	public void testDateTimePattern() {
		assertThat(DateUtil.dateTimePattern(FormatStyle.FULL, FormatStyle.FULL, Locale.US),
			is("EEEE, MMMM d, y 'at' h:mm:ss a zzzz"));
	}

	@Test
	public void testNowSec() {
		assertThat(DateUtil.nowSec().get(ChronoField.MILLI_OF_SECOND), is(0));
		assertThat(DateUtil.nowSec().get(ChronoField.MICRO_OF_SECOND), is(0));
	}

	@Test
	public void testFormatIso() {
		assertThat(DateUtil.formatIso(0), is("1970-01-01T00:00:00Z"));
		assertThat(DateUtil.formatIso(-1000), is("1969-12-31T23:59:59Z"));
		assertThat(DateUtil.formatIso(-999), is("1969-12-31T23:59:59Z"));
		assertThat(DateUtil.formatIso(999), is("1970-01-01T00:00:00Z"));
		assertThat(DateUtil.formatIso(1000), is("1970-01-01T00:00:01Z"));
	}

	@Test
	public void testFormatLocalIso() {
		int offset = TimeZone.getDefault().getOffset(0);
		if (offset < 0) assertTrue(DateUtil.formatLocalIso(0).startsWith("1969-12-31"));
		else assertTrue(DateUtil.formatLocalIso(0).startsWith("1970-01-01"));
	}

	@Test
	public void testTimeToDuration() {
		assertThat(DateUtil.timeToDuration(LocalTime.of(2, 15)), is(Duration.ofMinutes(135)));
		assertNull(DateUtil.timeToDuration(null));
	}

	@Test
	public void testDurationToTime() {
		assertThat(DateUtil.durationToTime(Duration.ofMinutes(135)), is(LocalTime.of(2, 15)));
		assertNull(DateUtil.durationToTime(null));
	}

}
