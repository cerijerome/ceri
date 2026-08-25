package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.core.Native;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.util.FfmOs;

public class CTermiosTest {
	private final FfmTesting.Lib<TestCLibNative> testLib = TestCLibNative.lib();
	private TestCLibNative lib;
	private int fd;

	@After
	public void after() {
		Closeables.close(testLib);
		lib = null;
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CTermios.class);
		Assert.privateConstructor(CTermios.Mac.class);
		Assert.privateConstructor(CTermios.Linux.class);
	}

	@Test
	public void testTcgetattr() throws CException {
		init(true);
		FfmOs.forEach(_ -> {
			lib.tc.autoResponse(t -> {
				t.termios(0).accept(termios -> {
					termios.c_iflag = new CTermios.tcflag_t(0x123);
					termios.c_lflag = new CTermios.tcflag_t(0x456);
				});
			}, Result.of(0));
			var t = CTermios.tcgetattr(fd).get();
			FfmAssert.equal(t.c_iflag, 0x123);
			FfmAssert.equal(t.c_lflag, 0x456);
		});
	}

	@Test
	public void testTcsetattr() throws CException {
		init(true);
		var termios = CTermios.tcgetattr(fd).accept(t -> {
			t.c_iflag = new CTermios.tcflag_t(0x123);
			t.c_lflag = new CTermios.tcflag_t(0x456);
		});
		CTermios.tcsetattr(fd, 123, termios);
		lib.tc.lastValue().verify("tcsetattr", fd, 123);
		var t = lib.tc.lastValue().termios(1).get();
		FfmAssert.equal(t.c_iflag, 0x123);
		FfmAssert.equal(t.c_lflag, 0x456);
	}

	@Test
	public void testTcsendbreak() throws CException {
		init(true);
		CTermios.tcsendbreak(fd, 123);
		lib.tc.lastValue().verify("tcsendbreak", fd, 123);
	}

	@Test
	public void testTcdrain() throws CException {
		init(true);
		CTermios.tcdrain(fd);
		lib.tc.lastValue().verify("tcdrain", fd);
	}

	@Test
	public void testTcflush() throws CException {
		init(true);
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		lib.tc.lastValue().verify("tcflush", fd, CTermios.TCIOFLUSH);
	}

	@Test
	public void testTcflow() throws CException {
		init(true);
		CTermios.tcflow(fd, CTermios.TCION);
		lib.tc.lastValue().verify("tcflow", fd, CTermios.TCION);
	}

	@Test
	public void testCfmakeraw() throws CException {
		init(true);
		var termios = CTermios.tcgetattr(fd);
		CTermios.cfmakeraw(termios);
		lib.cf.lastValue().verify("cfmakeraw", termios);
	}

	@Test
	public void testCfgetispeed() throws CException {
		init(true).cf.autoResponses(Result.of(12345));
		var termios = CTermios.tcgetattr(fd);
		Assert.equal(CTermios.cfgetispeed(termios), 12345);
		lib.cf.lastValue().verify("cfgetispeed", termios);
	}

	@Test
	public void testCfsetispeed() throws CException {
		init(true);
		var termios = CTermios.tcgetattr(fd);
		CTermios.cfsetispeed(termios, 12345);
		lib.cf.lastValue().verify("cfsetispeed", termios, 12345L);
	}

	@Test
	public void testCfgetospeed() throws CException {
		init(true).cf.autoResponses(Result.of(12345));
		var termios = CTermios.tcgetattr(fd);
		Assert.equal(CTermios.cfgetospeed(termios), 12345);
		lib.cf.lastValue().verify("cfgetospeed", termios);
	}

	@Test
	public void testCfsetospeed() throws CException {
		init(true);
		var termios = CTermios.tcgetattr(fd);
		CTermios.cfsetospeed(termios, 12345);
		lib.cf.lastValue().verify("cfsetospeed", termios, 12345L);
	}

	@Test
	public void testOsCoverage() throws CException {
		init(false);
		FfmTesting.testForEachOs(CTermios.class, Native.Size.class);
	}

	private TestCLibNative init(boolean openFd) throws CException {
		lib = testLib.init();
		if (openFd) fd = CFcntl.open("test", 0);
		return lib;
	}
}
