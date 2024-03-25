package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRange;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.time.TimeSpec;
import ceri.jna.clib.jna.CTime.timespec;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.util.Struct;

public class CTimeTest {

	@Test
	public void testGetTimeOfDay() {
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday().time().totalMillis();
		assertRange(t, t0, t0 + 1000);
	}

	@Test
	public void testSetTimeOfDay() {
		CTime.gettimeofday(null);
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday(new timeval()).time().totalMillis();
		assertRange(t, t0, t0 + 1000);
	}

	@Test
	public void shouldCreateTimevalFromPointer() {
		Pointer p = Struct.write(new timeval().time(TimeSpec.ofMicros(1234, 56789))).getPointer();
		var t = Struct.read(new timeval(p));
		assertTimeval(t, 1234, 56789);
		assertEquals(t.time(), TimeSpec.ofMicros(1234, 56789));
	}

	@Test
	public void shouldCreateTimespecFromPointer() {
		Pointer p = Struct.write(new timespec().time(new TimeSpec(1234, 56789))).getPointer();
		var t = Struct.read(new timespec(p));
		assertTimespec(t, 1234, 56789);
		assertEquals(t.time(), new TimeSpec(1234, 56789));
	}

	private void assertTimeval(timeval t, long sec, long usec) {
		assertEquals(t.tv_sec.longValue(), sec);
		assertEquals(t.tv_usec.longValue(), usec);
	}

	private void assertTimespec(timespec t, long sec, long usec) {
		assertEquals(t.tv_sec.longValue(), sec);
		assertEquals(t.tv_nsec.longValue(), usec);
	}
}
