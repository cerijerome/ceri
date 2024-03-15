package ceri.jna.clib.jna;

import static ceri.common.math.MathUtil.roundDiv;
import java.time.Duration;
import java.time.Instant;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <sys/time.h>} and {@code <time.h>}
 */
public class CTime {
	private static final long USEC_NSEC = 1000L;
	private static final long SEC_USEC = 1000000L;
	private static final long SEC_NSEC = 1000000000L;

	private CTime() {}

	/* <sys/time.h> */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 * Fields are signed values.
	 */
	@Fields({ "tv_sec", "tv_usec" })
	public static class timeval extends Struct {
		public NativeLong tv_sec = JnaUtil.nlong(0); // time_t
		public NativeLong tv_usec = JnaUtil.nlong(0); // suseconds_t

		public static timeval from(Instant instant) {
			return new timeval().set(instant);
		}

		public static timeval from(Duration duration) {
			return new timeval().set(duration);
		}

		public timeval() {}

		public timeval(Pointer p) {
			super(p);
		}

		public timeval set(long sec, long usec) {
			tv_sec.setValue(Math.addExact(sec, usec / SEC_USEC));
			tv_usec.setValue(usec % SEC_USEC);
			return this;
		}

		public timeval set(timeval time) {
			return set(time.tv_sec.longValue(), time.tv_usec.longValue());
		}

		public timeval set(Instant instant) {
			return set(instant.getEpochSecond(), roundDiv(instant.getNano(), USEC_NSEC));
		}

		public timeval set(Duration duration) {
			return set(duration.getSeconds(), roundDiv(duration.getNano(), USEC_NSEC));
		}

		public Instant instant() {
			return Instant.ofEpochSecond(tv_sec.longValue(), tv_usec.longValue() * USEC_NSEC);
		}

		public Duration duration() {
			return Duration.ofSeconds(tv_sec.longValue(), tv_usec.longValue() * USEC_NSEC);
		}
	}

	public static timeval gettimeofday() {
		return timeval.from(Instant.now());
	}

	public static void gettimeofday(timeval time) {
		if (time != null) time.set(Instant.now());
	}

	/* <time.h> */

	/**
	 * A time value of seconds and nanosecond offset.
	 */
	@Fields({ "tv_sec", "tv_nsec" })
	public static class timespec extends Struct {
		public NativeLong tv_sec = JnaUtil.nlong(0); // time_t
		public NativeLong tv_nsec = JnaUtil.nlong(0); // usually long / long long

		public static timespec from(Instant instant) {
			return new timespec().set(instant);
		}

		public static timespec from(Duration duration) {
			return new timespec().set(duration);
		}

		public timespec() {}

		public timespec(Pointer p) {
			super(p);
		}

		public timespec set(long sec, long nsec) {
			tv_sec.setValue(Math.addExact(sec, nsec / SEC_NSEC));
			tv_nsec.setValue(nsec % SEC_NSEC);
			return this;
		}

		public timespec set(timespec time) {
			return set(time.tv_sec.longValue(), time.tv_nsec.longValue());
		}

		public timespec set(Instant instant) {
			return set(instant.getEpochSecond(), instant.getNano());
		}

		public timespec set(Duration duration) {
			return set(duration.getSeconds(), duration.getNano());
		}

		public Instant instant() {
			return Instant.ofEpochSecond(tv_sec.longValue(), tv_nsec.longValue());
		}

		public Duration duration() {
			return Duration.ofSeconds(tv_sec.longValue(), tv_nsec.longValue());
		}
	}

}
