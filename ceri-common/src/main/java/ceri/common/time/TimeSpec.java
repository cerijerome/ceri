package ceri.common.time;

import java.time.Duration;
import java.time.Instant;

/**
 * A time interval of seconds and nanoseconds. A created instance is not automatically normalized.
 */
public record TimeSpec(long seconds, long nanos) {
	private static final long USEC_NSEC = 1000L;
	private static final long MSEC_NSEC = 1000000L;
	private static final long SEC_MSEC = 1000L;
	private static final long SEC_USEC = 1000000L;
	private static final long SEC_NSEC = 1000000000L;

	public static TimeSpec from(Instant instant) {
		return new TimeSpec(instant.getEpochSecond(), instant.getNano());
	}

	public static TimeSpec from(Duration duration) {
		return new TimeSpec(duration.getSeconds(), duration.getNano());
	}

	public static TimeSpec ofMicros(long seconds, long micros) {
		return new TimeSpec(seconds, Math.multiplyExact(micros, USEC_NSEC));
	}

	public static TimeSpec ofMillis(long seconds, long millis) {
		return new TimeSpec(seconds, Math.multiplyExact(millis, MSEC_NSEC));
	}

	public long micros() {
		return Math.floorDiv(nanos(), USEC_NSEC);
	}

	public long millis() {
		return Math.floorDiv(nanos(), MSEC_NSEC);
	}

	public int secondsInt() {
		return Math.toIntExact(seconds());
	}

	public int nanosInt() {
		return Math.toIntExact(nanos());
	}

	public int microsInt() {
		return Math.toIntExact(micros());
	}

	public int millisInt() {
		return Math.toIntExact(millis());
	}

	public long totalNanos() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_NSEC), nanos());
	}

	public long totalMicros() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_USEC),
			Math.floorDiv(nanos(), USEC_NSEC));
	}

	public long totalMillis() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_MSEC),
			Math.floorDiv(nanos(), MSEC_NSEC));
	}

	public Instant toInstant() {
		return Instant.ofEpochSecond(seconds(), nanos());
	}

	public Duration toDuration() {
		return Duration.ofSeconds(seconds(), nanos());
	}

	/**
	 * Normalizes nanoseconds to be within 1 second, and adjusts seconds with the difference.
	 */
	public TimeSpec normalize() {
		long sec = Math.floorDiv(nanos(), SEC_NSEC);
		if (sec == 0) return this;
		return new TimeSpec(Math.addExact(seconds, sec), Math.floorMod(nanos(), SEC_NSEC));
	}
}
