package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.ffm.CIoctl.Linux.serial_struct;
import ceri.ffm.clib.ffm.CTermios.speed_t;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Pointer;
import ceri.ffm.util.FfmOs;

public class CIoctlTest {
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
		Assert.privateConstructor(CIoctl.class);
		Assert.privateConstructor(CIoctl.Mac.class);
		Assert.privateConstructor(CIoctl.Linux.class);
	}

	@Test
	public void testIoctl() throws CException {
		initFd().ioctl.autoResponses(Result.of(3));
		Assert.equal(CIoctl.ioctl(fd, "test", 0x111, -1, -2), 3);
		lib.ioctl.lastValue().verify(fd, 0x111, -1, -2);
	}

	@Test
	public void testIoctlError() throws CException {
		initFd().ioctl.autoResponses(Result.errno(CErrNo.EIO));
		Assert.thrown(CException.class, ".*test:0x111, -1.*",
			() -> CIoctl.ioctl(fd, "test", 0x111, -1));
	}

	@Test
	public void testBreak() throws CException {
		initFd();
		CIoctl.tiocsbrk(fd);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCSBRK);
		CIoctl.tioccbrk(fd);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCCBRK);
	}

	@Test
	public void testFionread() throws CException {
		initFd().ioctl.autoResponse(c -> c.<Pointer.OfInt>arg(0).write(31), Result.of(0));
		Assert.equal(CIoctl.fionread(fd), 31);
		lib.ioctl.lastValue().verify(fd, CIoctl.FIONREAD);
	}

	@Test
	public void testTiocoutq() throws CException {
		initFd().ioctl.autoResponse(c -> c.<Pointer.OfInt>arg(0).write(37), Result.of(0));
		Assert.equal(CIoctl.tiocoutq(fd), 37);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCOUTQ);
	}

	@Test
	public void testTioexcl() throws CException {
		initFd();
		CIoctl.tiocexcl(fd);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCEXCL);
	}

	@Test
	public void testTiocmget() throws CException {
		initFd().ioctl.autoResponse(
			c -> c.<Pointer.OfInt>arg(0).write(CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI), Result.of(0));
		Assert.equal(CIoctl.tiocmget(fd), CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMGET);
	}

	@Test
	public void testTiocmbis() throws CException {
		initFd().ioctl.autoResponse(
			c -> Assert.equal(c.<Pointer.OfInt>arg(0).get(), CIoctl.TIOCM_LE), Result.of(0));
		CIoctl.tiocmbis(fd, CIoctl.TIOCM_LE);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMBIS);
	}

	@Test
	public void testTiocmbic() throws CException {
		initFd().ioctl.autoResponse(
			c -> Assert.equal(c.<Pointer.OfInt>arg(0).get(), CIoctl.TIOCM_LE), Result.of(0));
		CIoctl.tiocmbic(fd, CIoctl.TIOCM_LE);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMBIC);
	}

	@Test
	public void testTiocmset() throws CException {
		initFd().ioctl.autoResponse(
			c -> Assert.equal(c.<Pointer.OfInt>arg(0).get(), CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS),
			Result.of(0));
		CIoctl.tiocmset(fd, CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMSET);
	}

	@Test
	public void testTiocmbitSet() throws CException {
		initFd().ioctl.autoResponse(
			c -> Assert.equal(c.<Pointer.OfInt>arg(0).get(), CIoctl.TIOCM_RI), Result.of(0));
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI, true);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMBIS);
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI, false);
		lib.ioctl.lastValue().verify(fd, CIoctl.TIOCMBIC);
	}

	@Test
	public void testTiocmbitGet() throws CException {
		initFd().ioctl.autoResponse(
			c -> c.<Pointer.OfInt>arg(0).write(CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD), Result.of(0));
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_RTS), true);
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_CD), true);
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_DSR), false);
		lib.ioctl.autoResponse(
			c -> c.<Pointer.OfInt>arg(0).write(CIoctl.TIOCM_DSR | CIoctl.TIOCM_CD), Result.of(0));
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_RTS), false);
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_CD), true);
		Assert.equal(CIoctl.tiocmbit(fd, CIoctl.TIOCM_DSR), true);
	}

	@Test
	public void testMacIossiospeed() throws CException {
		initFd().ioctl.autoResponse(c -> FfmAssert.equal(c.<Pointer<speed_t>>arg(0).get(), 250000L),
			Result.of(0));
		CIoctl.Mac.iossiospeed(fd, 250000);
		lib.ioctl.lastValue().verify(fd, CIoctl.Mac.IOSSIOSPEED);
	}

	@Test
	public void testLinuxTiocgserial() throws CException {
		initFd().ioctl.autoResponse(c -> {
			var ss = new serial_struct();
			ss.type = 0x33;
			ss.baud_base = 0x123;
			c.<Pointer<serial_struct>>arg(0).write(ss);
		}, Result.of(0));
		var ss = CIoctl.Linux.tiocgserial(fd).get();
		lib.ioctl.lastValue().verify(fd, CIoctl.Linux.TIOCGSERIAL);
		Assert.equal(ss.type, 0x33);
		Assert.equal(ss.baud_base, 0x123);
	}

	@Test
	public void testLinuxTiocsserial() throws CException {
		initFd().ioctl.autoResponse(c -> {
			var ss = c.<Pointer<serial_struct>>arg(0).get();
			Assert.equal(ss.type, 0x33);
			Assert.equal(ss.baud_base, 0x123);
		}, Result.of(0));
		var ss = new serial_struct();
		ss.type = 0x33;
		ss.baud_base = 0x123;
		CIoctl.Linux.tiocsserial(fd, serial_struct.$.pointerOf(ss));
		lib.ioctl.lastValue().verify(fd, CIoctl.Linux.TIOCSSERIAL);
	}

	@Test
	public void testFields() throws Exception {
		FfmTesting.testAsOs(FfmOs.mac, Mac.class, CIoctl.class);
		FfmTesting.testAsOs(FfmOs.linux, Linux.class, CIoctl.class);
	}

	public static class Mac {
		static {
			Assert.equal(CIoctl._IO('t', 111), 0x2000746f, "_IO('t', 111)");
			Assert.equal(CIoctl._IOR('t', 106, Integer.BYTES), 0x4004746a, "_IOR('t', 106, int)");
			Assert.equal(CIoctl._IOW('t', 109, Integer.BYTES), 0x8004746d, "_IOW('t', 109, int)");
			Assert.equal(CIoctl._IOWR('B', 102, Integer.BYTES), 0xc0044266, "_IOWR('B', 102, int)");
			Assert.equal(CIoctl.TIOCSBRK, 0x2000747b, "TIOCSBRK");
			Assert.equal(CIoctl.TIOCCBRK, 0x2000747a, "TIOCCBRK");
			Assert.equal(CIoctl.FIONREAD, 0x4004667f, "FIONREAD");
			Assert.equal(CIoctl.TIOCEXCL, 0x2000740d, "TIOCEXCL");
			Assert.equal(CIoctl.TIOCOUTQ, 0x40047473, "TIOCOUTQ");
			Assert.equal(CIoctl.TIOCMGET, 0x4004746a, "TIOCMGET");
			Assert.equal(CIoctl.TIOCMBIS, 0x8004746c, "TIOCMBIS");
			Assert.equal(CIoctl.TIOCMBIC, 0x8004746b, "TIOCMBIC");
			Assert.equal(CIoctl.TIOCMSET, 0x8004746d, "TIOCMSET");
			Assert.equal(CIoctl.Mac.IOSSIOSPEED, 0x80085402, "IOSSIOSPEED");
		}
	}

	public static class Linux {
		static {
			Assert.equal(CIoctl._IO('t', 111), 0x746f, "_IO('t', 111)");
			Assert.equal(CIoctl._IOR('t', 106, Integer.BYTES), 0x8004746a, "_IOR('t', 106, int)");
			Assert.equal(CIoctl._IOW('t', 109, Integer.BYTES), 0x4004746d, "_IOW('t', 109, int)");
			Assert.equal(CIoctl._IOWR('B', 102, Integer.BYTES), 0xc0044266, "_IOWR('B', 102, int)");
			Assert.equal(CIoctl.Linux.ASYNC_SPD_HI, 0x10, "ASYNC_SPD_HI");
			Assert.equal(CIoctl.Linux.ASYNC_SPD_VHI, 0x20, "ASYNC_SPD_VHI");
			Assert.equal(CIoctl.Linux.ASYNC_SPD_SHI, 0x1000, "ASYNC_SPD_SHI");
			Assert.equal(CIoctl.Linux.ASYNC_SPD_CUST, 0x30, "ASYNC_SPD_CUST");
			Assert.equal(CIoctl.Linux.ASYNC_SPD_MASK, 0x1030, "ASYNC_SPD_MASK");
			Assert.equal(CIoctl.Linux.TIOCGSERIAL, 0x541e, "TIOCGSERIAL");
			Assert.equal(CIoctl.Linux.TIOCSSERIAL, 0x541f, "TIOCSSERIAL");
		}
	}

	private TestCLibNative initFd() throws CException {
		lib = testLib.init();
		fd = CFcntl.open("test", 0);
		return lib;
	}
}
