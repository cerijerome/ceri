package ceri.common.time;

import java.time.Duration;
import java.time.Instant;

/**
 * A time interval of seconds and nanoseconds. A created instance is not automatically normalized.
 */
public record TimeSpec(long seconds, long nanos) {
	public static TimeSpec ZERO = new TimeSpec(0, 0);
	private static final long USEC_NSEC = 1000L;
	private static final long MSEC_NSEC = 1000000L;
	private static final long SEC_MSEC = 1000L;
	private static final long SEC_USEC = 1000000L;
	private static final long SEC_NSEC = 1000000000L;

	/**
	 * Creates from epoch seconds and nanosecond offset (normalized).
	 */
	public static TimeSpec now() {
		return from(Instant.now());
	}

	/**
	 * Creates from system time in milliseconds (normalized).
	 */
	public static TimeSpec nowMillis() {
		return fromMillis(System.currentTimeMillis());
	}

	/**
	 * Creates from JVM time in nanoseconds (normalized).
	 */
	public static TimeSpec nowNanos() {
		return fromNanos(System.nanoTime());
	}

	/**
	 * Creates from milliseconds (normalized).
	 */
	public static TimeSpec fromMillis(long millis) {
		return new TimeSpec(millis / SEC_MSEC, (millis % SEC_MSEC) * MSEC_NSEC);
	}

	/**
	 * Creates from nanoseconds (normalized).
	 */
	public static TimeSpec fromNanos(long nanos) {
		return new TimeSpec(nanos / SEC_NSEC, nanos % SEC_NSEC);
	}

	/**
	 * Creates from instant epoch seconds and nanosecond offset (normalized).
	 */
	public static TimeSpec from(Instant instant) {
		return new TimeSpec(instant.getEpochSecond(), instant.getNano());
	}

	/**
	 * Creates from duration seconds and nanosecond offset (normalized).
	 */
	public static TimeSpec from(Duration duration) {
		return new TimeSpec(duration.getSeconds(), duration.getNano());
	}

	/**
	 * Creates from seconds and converts microsecond offset to nanoseconds (not normalized).
	 */
	public static TimeSpec ofMicros(long seconds, long micros) {
		return new TimeSpec(seconds, Math.multiplyExact(micros, USEC_NSEC));
	}

	/**
	 * Creates from seconds and converts millisecond offset to nanoseconds (not normalized).
	 */
	public static TimeSpec ofMillis(long seconds, long millis) {
		return new TimeSpec(seconds, Math.multiplyExact(millis, MSEC_NSEC));
	}

	/**
	 * Returns the nanosecond offset as microseconds, rounded down.
	 */
	public long micros() {
		return Math.floorDiv(nanos(), USEC_NSEC);
	}

	/**
	 * Returns the nanosecond offset as milliseconds, rounded down.
	 */
	public long millis() {
		return Math.floorDiv(nanos(), MSEC_NSEC);
	}

	/**
	 * Returns the seconds as an int, with an overflow exception if out of range.
	 */
	public int secondsInt() {
		return Math.toIntExact(seconds());
	}

	/**
	 * Returns the nanosecond offset as an int, with an overflow exception if out of range.
	 */
	public int nanosInt() {
		return Math.toIntExact(nanos());
	}

	/**
	 * Returns the nanosecond offset converted to microseconds as an int, with an overflow exception
	 * if out of range.
	 */
	public int microsInt() {
		return Math.toIntExact(micros());
	}

	/**
	 * Returns the nanosecond offset converted to milliseconds as an int, with an overflow exception
	 * if out of range.
	 */
	public int millisInt() {
		return Math.toIntExact(millis());
	}

	/**
	 * Returns seconds and nanosecond offset converted nanoseconds, with an overflow exception if
	 * out of range.
	 */
	public long totalNanos() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_NSEC), nanos());
	}

	/**
	 * Returns seconds and nanosecond offset converted microseconds, with an overflow exception if
	 * out of range.
	 */
	public long totalMicros() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_USEC),
			Math.floorDiv(nanos(), USEC_NSEC));
	}

	/**
	 * Returns seconds and nanosecond offset converted milliseconds, with an overflow exception if
	 * out of range.
	 */
	public long totalMillis() {
		return Math.addExact(Math.multiplyExact(seconds(), SEC_MSEC),
			Math.floorDiv(nanos(), MSEC_NSEC));
	}

	/**
	 * Converts to an instant of epoch seconds and nanosecond offset.
	 */
	public Instant toInstant() {
		return Instant.ofEpochSecond(seconds(), nanos());
	}

	/**
	 * Converts to an instant of seconds and nanosecond offset.
	 */
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
