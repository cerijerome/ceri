package ceri.serial.clib.jna;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.serial.jna.Struct;

public class Time {

	private Time() {}

	/* struct_timeval.h */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 */
	public static class timeval extends Struct {
		private static final List<String> FIELDS = List.of("tv_sec", "tv_usec");

		public static class ByValue extends timeval implements Structure.ByValue {}

		public static class ByReference extends timeval implements Structure.ByReference {}

		public static timeval now() {
			Instant instant = Instant.now();
			return new timeval(instant.getEpochSecond(), NANOSECONDS.toMillis(instant.getNano()));
		}

		public NativeLong tv_sec; //
		public NativeLong tv_usec; //

		public timeval() {
			this(0, 0);
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

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/* time.h */

	public static timeval gettimeofday() {
		return timeval.now();
	}

	public static void gettimeofday(timeval time) {
		if (time == null) return;
		Instant instant = Instant.now();
		time.tv_sec.setValue(instant.getEpochSecond());
		time.tv_usec.setValue(NANOSECONDS.toMillis(instant.getNano()));
	}

	/* utilities */

	/**
	 * Create timeval from duration, which may be null.
	 */
	public static timeval timeval(Duration d) {
		if (d == null) return null;
		return new timeval(d.getSeconds(), d.getNano() / 1000L);
	}

	/**
	 * Create duration from timeval, which may be null.
	 */
	public static Duration duration(timeval tv) {
		if (tv == null) return null;
		return Duration.ofSeconds(tv.tv_sec.longValue(), tv.tv_usec.longValue() * 1000L);
	}

	/**
	 * Create timeval, reading values from pointer.
	 */
	public static timeval read(Pointer p) {
		if (p == null) return null;
		return Struct.read(new timeval(p));
	}

	/**
	 * Write values and return pointer.
	 */
	public static Pointer write(timeval tv) {
		if (tv == null) return null;
		return Struct.write(tv).getPointer();
	}

}
