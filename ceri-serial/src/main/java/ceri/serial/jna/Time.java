package ceri.serial.jna;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.time.Instant;
import java.util.List;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class Time {

	private Time() {}

	public static class timeval extends Struct {
		private static final List<String> FIELDS = List.of("tv_sec", "tv_usec");

		public static class ByValue extends timeval implements Structure.ByValue {}

		public static class ByReference extends timeval implements Structure.ByReference {}

		public static timeval now() {
			Instant instant = Instant.now();
			return new timeval(instant.getEpochSecond(), NANOSECONDS.toMillis(instant.getNano()));
		}

		public NativeLong tv_sec;
		public NativeLong tv_usec;

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

	public static timeval gettimeofday() {
		return timeval.now();
	}

	public static void gettimeofday(timeval time) {
		if (time == null) return;
		Instant instant = Instant.now();
		time.tv_sec.setValue(instant.getEpochSecond());
		time.tv_usec.setValue(NANOSECONDS.toMillis(instant.getNano()));
	}

}
