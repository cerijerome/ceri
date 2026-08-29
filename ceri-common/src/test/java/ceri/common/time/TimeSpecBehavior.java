package ceri.common.time;

import static ceri.common.test.Testing.IMAX;
import static ceri.common.test.Testing.LMAX;
import static ceri.common.test.Testing.LMIN;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import ceri.common.test.Assert;

public class TimeSpecBehavior {

	@Test
	public void shouldCreateWithCurrentInstant() {
		var t0 = TimeSpec.from(Instant.now()).totalMillis();
		var t = TimeSpec.now().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void shouldCreateFromCurrentMillis() {
		long t0 = TimeSupplier.millis.time();
		var t = TimeSpec.nowMillis().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void shouldCreateFromCurrentNanos() {
		long t0 = TimeSupplier.nanos.time();
		var t = TimeSpec.nowNanos().totalNanos();
		Assert.range(t, t0, t0 + 1000000000L);
	}

	@Test
	public void shouldCreateFromInstant() {
		var instant = Instant.ofEpochSecond(123, 1234567890); // normalized
		var t = TimeSpec.from(instant);
		assertTime(t, 124, 234567890);
		Assert.equal(t.toInstant(), instant);
	}

	@Test
	public void shouldCreateFromDuration() {
		var duration = Duration.ofSeconds(123, 1234567890); // normalized
		var t = TimeSpec.from(duration);
		assertTime(t, 124, 234567890);
		Assert.equal(t.toDuration(), duration);
	}

	@Test
	public void shouldCreateFromMillis() {
		assertTime(TimeSpec.ofMillis(123, 4567), 123, 4567000000L);
	}

	@Test
	public void shouldProvideTimeUnitValues() {
		Assert.equal(new TimeSpec(123, 1234567890).nanosInt(), 1234567890);
		Assert.equal(new TimeSpec(123, 1234567890).micros(), 1234567L);
		Assert.equal(new TimeSpec(123, 1234567890).microsInt(), 1234567);
		Assert.equal(new TimeSpec(123, 1234567890).millis(), 1234L);
		Assert.equal(new TimeSpec(123, 1234567890).millisInt(), 1234);
		Assert.equal(new TimeSpec(123, 1234567890).secondsInt(), 123);
		Assert.overflow(() -> new TimeSpec(123, IMAX + 1L).nanosInt());
		Assert.overflow(() -> new TimeSpec(IMAX + 1L, 0).secondsInt());
	}

	@Test
	public void shouldProvideTotalTimeUnitValues() {
		Assert.equal(new TimeSpec(123, 1234567890).totalNanos(), 124234567890L);
		Assert.equal(new TimeSpec(123, 1234567890).totalMicros(), 124234567L);
		Assert.equal(new TimeSpec(123, 1234567890).totalMillis(), 124234L);
		Assert.equal(new TimeSpec(9223372036L, 854775807L).totalNanos(), LMAX);
		Assert.overflow(() -> new TimeSpec(9223372036L, 854775808L).totalNanos());
	}

	@Test
	public void shouldNormalize() {
		assertTime(TimeSpec.ofMicros(123, 4567890).normalize(), 127, 567890000L);
		assertTime(TimeSpec.ofMillis(123, 4567).normalize(), 127, 567000000L);
		assertTime(new TimeSpec(LMAX, 999999999).normalize(), LMAX, 999999999);
		assertTime(new TimeSpec(LMIN, -999999999).normalize(), LMIN, -999999999);
		assertTime(new TimeSpec(LMAX, LMIN).normalize(), 9223372027631403770L, 145224192L);
		assertTime(new TimeSpec(LMIN, LMAX).normalize(), -9223372027631403771L, -145224193L);
		Assert.thrown(() -> new TimeSpec(LMAX, 1000000000L).normalize());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(TimeSpec.ofMillis(33, 123), "33s+123000000ns");
	}

	private static void assertTime(TimeSpec t, long secs, long nanos) {
		Assert.equal(t.seconds(), secs);
		Assert.equal(t.nanos(), nanos);
	}
}
