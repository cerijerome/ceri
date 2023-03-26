package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRange;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.util.Struct;

public class CTimeTest {

	@Test
	public void testGetTimeOfDay() {
		timeval t = CTime.gettimeofday();
		assertTimevalNow(t);
	}

	@Test
	public void testSetTimeOfDay() {
		CTime.gettimeofday(null);
		timeval t = new timeval();
		CTime.gettimeofday(t);
		assertTimevalNow(t);
	}

	@Test
	public void shouldCreateFromPointer() {
		Pointer p = Struct.write(new timeval().set(1234, 56789)).getPointer();
		var t = Struct.read(new timeval(p));
		assertTimeval(t, 1234, 56789);
	}

	@Test
	public void shouldSetFromTimeval() {
		var t0 = new timeval().set(100, 23456789);
		var t = new timeval().set(t0);
		assertTimeval(t, 123, 456789);
	}

	@Test
	public void shouldRoundNanos() {
		var t = new timeval().setNsec(1234, 123456789);
		assertTimeval(t, 1234, 123457);
	}

	@Test
	public void shouldNormalize() {
		assertTimeval(new timeval().setMsec(100, 23456), 123, 456000);
		assertTimeval(new timeval().set(100, 23456789), 123, 456789);
	}

	@Test
	public void shouldConvertToDuration() {
		var t = timeval.from(Duration.ofSeconds(1234, 123456789));
		assertTimeval(t, 1234, 123457);
		assertEquals(t.duration(), Duration.ofSeconds(1234, 123457000));
	}

	private void assertTimeval(timeval t, long sec, int usec) {
		assertEquals(t.tv_sec.longValue(), sec);
		assertEquals(t.tv_usec, usec);
	}

	private void assertTimevalNow(timeval t) {
		long s = Duration.between(t.instant(), Instant.now()).getSeconds();
		assertRange(s, 0, 1);
	}
}
