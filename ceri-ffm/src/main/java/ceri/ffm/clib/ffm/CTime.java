package ceri.ffm.clib.ffm;

import ceri.common.time.TimeSpec;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType.CLong;
import ceri.ffm.type.Struct;

/**
 * Types and functions from {@code <sys/time.h>} and {@code <time.h>}
 */
@CInclude({ "sys/time.h", "time.h" })
public class CTime {

	private CTime() {}

	/* <sys/time.h> */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 */
	@Fields({ "tv_sec", "tv_usec" })
	public static class timeval extends Struct<timeval> {
		public static final Supporter<timeval> $ = support(timeval.class);
		public CLong tv_sec; // time_t
		public CLong tv_usec; // suseconds_t

		/**
		 * Applies the given time to this instance.
		 */
		public timeval set(TimeSpec t) {
			if (t == null) return this;
			t = t.normalize();
			tv_sec = new CLong(t.seconds());
			tv_usec = new CLong(t.micros());
			return this;
		}

		/**
		 * Gets the time from this instance.
		 */
		public TimeSpec get() {
			return TimeSpec.ofMicros(tv_sec.value(), tv_usec.value());
		}
	}

	/**
	 * Returns a new time instance with the current time of day.
	 */
	public static timeval gettimeofday() {
		return new timeval().set(TimeSpec.now());
	}

	/* <time.h> */

	/**
	 * A time value of seconds and nanosecond offset.
	 */
	@Fields({ "tv_sec", "tv_nsec" })
	public static class timespec extends Struct<timespec> {
		public static final Supporter<timespec> $ = support(timespec.class);
		public CLong tv_sec; // time_t
		public CLong tv_nsec; // usually long / long long

		/**
		 * Applies the given time to this instance.
		 */
		public timespec set(TimeSpec t) {
			if (t == null) return this;
			t = t.normalize();
			tv_sec = new CLong(t.seconds());
			tv_nsec = new CLong(t.nanos());
			return this;
		}

		/**
		 * Gets the time from this instance.
		 */
		public TimeSpec get() {
			return new TimeSpec(tv_sec.value(), tv_nsec.value());
		}
	}
}
