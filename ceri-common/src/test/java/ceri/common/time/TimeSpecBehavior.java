package ceri.common.time;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOverflow;
import static ceri.common.test.AssertUtil.assertThrown;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;

public class TimeSpecBehavior {

	@Test
	public void shouldCreateFromInstant() {
		var instant = Instant.ofEpochSecond(123, 1234567890); // normalized
		var t = TimeSpec.from(instant);
		assertTimeSpec(t, 124, 234567890);
		assertEquals(t.toInstant(), instant);
	}

	@Test
	public void shouldCreateFromDuration() {
		var duration = Duration.ofSeconds(123, 1234567890); // normalized
		var t = TimeSpec.from(duration);
		assertTimeSpec(t, 124, 234567890);
		assertEquals(t.toDuration(), duration);
	}

	@Test
	public void shouldCreateFromMicros() {
		assertTimeSpec(TimeSpec.ofMicros(123, 4567890), 123, 4567890000L);
	}

	@Test
	public void shouldCreateFromMillis() {
		assertTimeSpec(TimeSpec.ofMillis(123, 4567), 123, 4567000000L);
	}

	@Test
	public void shouldProvideTimeUnitValues() {
		assertEquals(new TimeSpec(123, 1234567890).nanosInt(), 1234567890);
		assertEquals(new TimeSpec(123, 1234567890).micros(), 1234567L);
		assertEquals(new TimeSpec(123, 1234567890).microsInt(), 1234567);
		assertEquals(new TimeSpec(123, 1234567890).millis(), 1234L);
		assertEquals(new TimeSpec(123, 1234567890).millisInt(), 1234);
		assertEquals(new TimeSpec(123, 1234567890).secondsInt(), 123);
		assertOverflow(() -> new TimeSpec(123, Integer.MAX_VALUE + 1L).nanosInt());
		assertOverflow(() -> new TimeSpec(Integer.MAX_VALUE + 1L, 0).secondsInt());
	}

	@Test
	public void shouldProvideTotalTimeUnitValues() {
		assertEquals(new TimeSpec(123, 1234567890).totalNanos(), 124234567890L);
		assertEquals(new TimeSpec(123, 1234567890).totalMicros(), 124234567L);
		assertEquals(new TimeSpec(123, 1234567890).totalMillis(), 124234L);
		assertEquals(new TimeSpec(9223372036L, 854775807L).totalNanos(), Long.MAX_VALUE);
		assertOverflow(() -> new TimeSpec(9223372036L, 854775808L).totalNanos());
	}

	@Test
	public void shouldNormalize() {
		assertTimeSpec(TimeSpec.ofMicros(123, 4567890).normalize(), 127, 567890000L);
		assertTimeSpec(TimeSpec.ofMillis(123, 4567).normalize(), 127, 567000000L);
		assertTimeSpec(new TimeSpec(Long.MAX_VALUE, 999999999).normalize(), Long.MAX_VALUE,
			999999999);
		assertTimeSpec(new TimeSpec(Long.MAX_VALUE, Long.MIN_VALUE).normalize(),
			9223372027631403770L, 145224192L);
		var t = new TimeSpec(Long.MAX_VALUE, 1000000000L);
		assertThrown(() -> t.normalize());
	}

	private static void assertTimeSpec(TimeSpec t, long secs, long nanos) {
		assertEquals(t.seconds(), secs);
		assertEquals(t.nanos(), nanos);
	}
}
