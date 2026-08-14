package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.time.TimeSpec;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.type.IntType.CLong;

public class CTimeTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CTime.class);
	}

	@Test
	public void testTimeVal() {
		var time = new CTime.timeval();
		time.tv_sec = new CLong(-100);
		time.tv_usec = new CLong(-200);
		assertTime(time, -100, -200);
	}

	@Test
	public void testGetTimeOfDay() {
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday().time().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void testSetTimeOfDay() {
		CTime.gettimeofday(null);
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday(new CTime.timeval()).time().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void testTimeSpec() {
		var time = new CTime.timespec();
		time.tv_sec = new CLong(-100);
		time.tv_nsec = new CLong(-200000);
		assertTime(time, -100, -200000);
	}

	@Test
	public void testTimeFromSpec() {
		Assert.equal(CTime.timespec.of(null), null);
		var spec = TimeSpec.now();
		var time = CTime.timespec.of(spec);
		Assert.equal(time.time(), spec);
	}

	private void assertTime(CTime.timeval t, long sec, long usec) {
		FfmAssert.equal(t.tv_sec, sec);
		FfmAssert.equal(t.tv_usec, usec);
	}

	private void assertTime(CTime.timespec t, long sec, long usec) {
		FfmAssert.equal(t.tv_sec, sec);
		FfmAssert.equal(t.tv_nsec, usec);
	}
}
