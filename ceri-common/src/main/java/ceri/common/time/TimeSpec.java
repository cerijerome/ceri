package ceri.common.time;

import java.time.Duration;
import java.time.Instant;

/**
 * A time interval of seconds and nanoseconds. A created instance is not automatically normalized.
 */
public record TimeSpec(long seconds, long nanos) {
	public static TimeSpec ZERO = new TimeSpec(0, 0);

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
		return new TimeSpec(millis / DateUtil.SEC_MILLIS,
			(millis % DateUtil.SEC_MILLIS) * DateUtil.MILLI_NANOS);
	}

	/**
	 * Creates from nanoseconds (normalized).
	 */
	public static TimeSpec fromNanos(long nanos) {
		return new TimeSpec(nanos / DateUtil.SEC_NANOS, nanos % DateUtil.SEC_NANOS);
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
		return new TimeSpec(seconds, Math.multiplyExact(micros, DateUtil.MICRO_NANOS));
	}

	/**
	 * Creates from seconds and converts millisecond offset to nanoseconds (not normalized).
	 */
	public static TimeSpec ofMillis(long seconds, long millis) {
		return new TimeSpec(seconds, Math.multiplyExact(millis, DateUtil.MILLI_NANOS));
	}

	/**
	 * Returns the nanosecond offset as microseconds, rounded down.
	 */
	public long micros() {
		return Math.floorDiv(nanos(), DateUtil.MICRO_NANOS);
	}

	/**
	 * Returns the nanosecond offset as milliseconds, rounded down.
	 */
	public long millis() {
		return Math.floorDiv(nanos(), DateUtil.MILLI_NANOS);
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
		return Math.addExact(Math.multiplyExact(seconds(), DateUtil.SEC_NANOS), nanos());
	}

	/**
	 * Returns seconds and nanosecond offset converted microseconds, with an overflow exception if
	 * out of range.
	 */
	public long totalMicros() {
		return Math.addExact(Math.multiplyExact(seconds(), DateUtil.SEC_MICROS),
			Math.floorDiv(nanos(), DateUtil.MICRO_NANOS));
	}

	/**
	 * Returns seconds and nanosecond offset converted milliseconds, with an overflow exception if
	 * out of range.
	 */
	public long totalMillis() {
		return Math.addExact(Math.multiplyExact(seconds(), DateUtil.SEC_MILLIS),
			Math.floorDiv(nanos(), DateUtil.MILLI_NANOS));
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
		long sec = Math.floorDiv(nanos(), DateUtil.SEC_NANOS);
		if (sec == 0) return this;
		return new TimeSpec(Math.addExact(seconds, sec),
			Math.floorMod(nanos(), DateUtil.SEC_NANOS));
	}

	@Override
	public String toString() {
		return seconds() + "s+" + nanos() + "ns";
	}
}
