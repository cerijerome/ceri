package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.time.TimeSpec;
import ceri.ffm.test.FfmAssert;

public class CTimeTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CTime.class);
	}

	@Test
	public void testTimeVal() {
		var time = new CTime.timeval().set(new TimeSpec(-100, -200000));
		assertTime(time, -100, -200);
		Assert.equal(time.get(), new TimeSpec(-100, -200000));
		assertTime(time.set(null), -100, -200);
	}

	@Test
	public void testGetTimeOfDay() {
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday().get().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void testSetTimeOfDay() {
		var t0 = TimeSpec.now().totalMillis();
		var t = CTime.gettimeofday().get().totalMillis();
		Assert.range(t, t0, t0 + 1000);
	}

	@Test
	public void testTimeSpec() {
		var time = new CTime.timespec().set(new TimeSpec(-100, -200000));
		assertTime(time, -100, -200000);
		Assert.equal(time.get(), new TimeSpec(-100, -200000));
		assertTime(time.set(null), -100, -200000);
	}

	private void assertTime(CTime.timeval t, long sec, long usec) {
		FfmAssert.equal(t.tv_sec, sec);
		FfmAssert.equal(t.tv_usec, usec);
	}

	private void assertTime(CTime.timespec t, long sec, long nsec) {
		FfmAssert.equal(t.tv_sec, sec);
		FfmAssert.equal(t.tv_nsec, nsec);
	}
}
