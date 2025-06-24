package ceri.jna.clib.jna;

import com.sun.jna.Pointer;
import ceri.common.time.TimeSpec;
import ceri.jna.type.CLong;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;

/**
 * Types and functions from {@code <sys/time.h>} and {@code <time.h>}
 */
public class CTime {

	private CTime() {}

	/* <sys/time.h> */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 * Fields are signed values.
	 */
	@Fields({ "tv_sec", "tv_usec" })
	public static class timeval extends Struct {
		public CLong tv_sec = new CLong(0); // time_t
		public CLong tv_usec = new CLong(0); // suseconds_t

		public static timeval of(TimeSpec time) {
			return time == null ? null : new timeval().time(time);
		}

		public timeval() {}

		public timeval(Pointer p) {
			super(p);
		}

		public timeval time(TimeSpec t) {
			t = t.normalize();
			tv_sec.setValue(t.seconds());
			tv_usec.setValue(t.micros());
			return this;
		}

		public TimeSpec time() {
			return TimeSpec.ofMicros(tv_sec.longValue(), tv_usec.longValue());
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
	public static class timespec extends Struct {
		public CLong tv_sec = new CLong(0); // time_t
		public CLong tv_nsec = new CLong(0); // usually long / long long

		public static timespec of(TimeSpec time) {
			return time == null ? null : new timespec().time(time);
		}

		public timespec() {}

		public timespec(Pointer p) {
			super(p);
		}

		public timespec time(TimeSpec t) {
			t = t.normalize();
			tv_sec.setValue(t.seconds());
			tv_nsec.setValue(t.nanos());
			return this;
		}

		public TimeSpec time() {
			return new TimeSpec(tv_sec.longValue(), tv_nsec.longValue());
		}
	}

}
