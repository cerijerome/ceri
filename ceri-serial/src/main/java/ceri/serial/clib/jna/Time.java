package ceri.serial.clib.jna;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.time.Duration;
import java.time.Instant;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Struct.Fields;

public class Time {
	private static final long MICRO_NANOS = 1000L;
	
	private Time() {}

	/* struct_timeval.h */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 */
	@Fields({ "tv_sec", "tv_usec" })
	public static class timeval extends Struct {
		public NativeLong tv_sec; //
		public NativeLong tv_usec; //

		public static timeval now() {
			Instant instant = Instant.now();
			return new timeval(instant.getEpochSecond(), NANOSECONDS.toMillis(instant.getNano()));
		}

		public timeval(long sec, long usec) {
			tv_sec = new NativeLong(sec);
			tv_usec = new NativeLong(usec);
		}

		public timeval(Pointer p) {
			super(p);
		}

		public double diffInSec(timeval other) {
			double sec = other == null ? 0.0 : other.tv_sec.doubleValue();
			double usec = other == null ? 0.0 : other.tv_usec.doubleValue();
			return (tv_sec.doubleValue() - sec) + 1e-6 * (tv_usec.doubleValue() - usec);
		}
	}

	/* time.h */

	public static timeval gettimeofday() {
		Instant instant = Instant.now();
		return new timeval(instant.getEpochSecond(), instant.getNano() / MICRO_NANOS);
	}

	public static void gettimeofday(timeval time) {
		if (time == null) return;
		Instant instant = Instant.now();
		time.tv_sec.setValue(instant.getEpochSecond());
		time.tv_usec.setValue(instant.getNano() / MICRO_NANOS);
	}

	/* utilities */

	/**
	 * Create timeval from duration, which may be null.
	 */
	public static timeval timeval(Duration d) {
		return d == null ? null : new timeval(d.getSeconds(), d.getNano() / MICRO_NANOS);
	}

	/**
	 * Create duration from timeval, which may be null.
	 */
	public static Duration duration(timeval tv) {
		return tv == null ? null :
			Duration.ofSeconds(tv.tv_sec.longValue(), tv.tv_usec.longValue() * MICRO_NANOS);
	}

	/**
	 * Create timeval, reading values from pointer.
	 */
	public static timeval read(Pointer p) {
		return p == null ? null : Struct.read(new timeval(p));
	}

	/**
	 * Write values and return pointer.
	 */
	public static Pointer write(timeval tv) {
		return tv == null ? null : Struct.write(tv).getPointer();
	}
}
