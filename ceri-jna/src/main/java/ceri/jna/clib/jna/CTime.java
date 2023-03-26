package ceri.jna.clib.jna;

import java.time.Duration;
import java.time.Instant;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.math.MathUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

public class CTime {
	private static final long MSEC_USEC = 1000L;
	private static final long USEC_NSEC = 1000L;
	private static final long SEC_MSEC = 1000L;
	private static final long SEC_USEC = 1000000L;

	private CTime() {}

	/* struct_timeval.h */

	/**
	 * A time value that is accurate to the nearest microsecond but also has a range of years.
	 * Fields are signed values.
	 */
	@Fields({ "tv_sec", "tv_usec" })
	public static class timeval extends Struct {
		public NativeLong tv_sec = JnaUtil.nlong(0); // time_t
		public int tv_usec = 0; // suseconds_t

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
			tv_usec = (int) (usec % SEC_USEC);
			return this;
		}

		public timeval set(timeval time) {
			return set(time.tv_sec.longValue(), time.tv_usec);
		}

		public timeval setNsec(long sec, long nsec) {
			return set(sec, MathUtil.roundDiv(nsec, USEC_NSEC));
		}

		public timeval setMsec(long sec, long msec) {
			return set(Math.addExact(sec, msec / SEC_MSEC), (msec % SEC_MSEC) * MSEC_USEC);
		}

		public timeval set(Instant instant) {
			return setNsec(instant.getEpochSecond(), instant.getNano());
		}

		public timeval set(Duration duration) {
			return setNsec(duration.getSeconds(), duration.getNano());
		}

		public Instant instant() {
			return Instant.ofEpochSecond(tv_sec.longValue(), tv_usec * USEC_NSEC);
		}

		public Duration duration() {
			return Duration.ofSeconds(tv_sec.longValue(), tv_usec * USEC_NSEC);
		}
	}

	/* time.h */

	public static timeval gettimeofday() {
		return timeval.from(Instant.now());
	}

	public static void gettimeofday(timeval time) {
		if (time != null) time.set(Instant.now());
	}

}
