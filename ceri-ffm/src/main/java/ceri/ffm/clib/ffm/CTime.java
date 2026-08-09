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
		public CLong tv_sec; // time_t
		public CLong tv_usec; // suseconds_t

		public timeval time(TimeSpec t) {
			t = t.normalize();
			tv_sec = new CLong(t.seconds());
			tv_usec = new CLong(t.micros());
			return this;
		}

		public TimeSpec time() {
			return TimeSpec.ofMicros(tv_sec.value(), tv_usec.value());
		}
	}

	public static timeval gettimeofday() {
		return gettimeofday(new timeval());
	}

	public static timeval gettimeofday(timeval time) {
		if (time != null) time.time(TimeSpec.now());
		return time;
	}

	/* <time.h> */

	/**
	 * A time value of seconds and nanosecond offset.
	 */
	@Fields({ "tv_sec", "tv_nsec" })
	public static class timespec extends Struct<timespec> {
		public CLong tv_sec; // time_t
		public CLong tv_nsec; // usually long / long long

		public static timespec of(TimeSpec time) {
			return time == null ? null : new timespec().time(time);
		}

		public timespec time(TimeSpec t) {
			t = t.normalize();
			tv_sec = new CLong(t.seconds());
			tv_nsec = new CLong(t.nanos());
			return this;
		}

		public TimeSpec time() {
			return new TimeSpec(tv_sec.value(), tv_nsec.value());
		}
	}
}
