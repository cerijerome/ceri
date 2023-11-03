package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.jna.test.JnaTestUtil.LINUX_OS;
import static ceri.jna.test.JnaTestUtil.MAC_OS;
import static ceri.jna.test.JnaTestUtil.assertRef;
import static ceri.jna.test.JnaTestUtil.assertUnlong;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CIoctl.Linux.serial_struct;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaTestUtil;

public class CIoctlTest {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private static int fd;

	@BeforeClass
	public static void beforeClass() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = CFcntl.open("test", 0);
	}

	@AfterClass
	public static void afterClass() {
		lib.close(fd);
		enc.close();
	}

	@Before
	public void before() {
		lib.ioctl.autoResponses(0);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CIoctl.class);
		assertPrivateConstructor(CIoctl.Mac.class);
		assertPrivateConstructor(CIoctl.Linux.class);
	}

	@Test
	public void testIoctl() throws CException {
		lib.ioctl.autoResponses(3);
		assertEquals(CIoctl.ioctl(fd, 0x111, -1, -2, -3), 3);
		assertIoctlAuto(fd, 0x111, -1, -2, -3);
		assertEquals(CIoctl.ioctl(() -> "test", fd, 0x111, -1, -2, -3), 3);
		assertIoctlAuto(fd, 0x111, -1, -2, -3);
	}

	@Test
	public void testBreak() throws CException {
		CIoctl.tiocsbrk(fd);
		assertIoctlAuto(fd, CIoctl.TIOCSBRK);
		CIoctl.tioccbrk(fd);
		assertIoctlAuto(fd, CIoctl.TIOCCBRK);
	}

	@Test
	public void testFionread() throws CException {
		ioctlAutoIntRef(31);
		assertEquals(CIoctl.fionread(fd), 31);
		assertIoctlIntRef(fd, CIoctl.FIONREAD, 31);
	}

	@Test
	public void testTiocoutq() throws CException {
		ioctlAutoIntRef(37);
		assertEquals(CIoctl.tiocoutq(fd), 37);
		assertIoctlIntRef(fd, CIoctl.TIOCOUTQ, 37);
	}

	@Test
	public void testTioexcl() throws CException {
		CIoctl.tiocexcl(fd);
		assertIoctlAuto(fd, CIoctl.TIOCEXCL);
	}

	@Test
	public void testTiocmget() throws CException {
		ioctlAutoIntRef(CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI);
		assertEquals(CIoctl.tiocmget(fd), CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI);
		assertIoctlIntRef(fd, CIoctl.TIOCMGET, CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI);
	}

	@Test
	public void testTiocmbis() throws CException {
		CIoctl.tiocmbis(fd, CIoctl.TIOCM_LE);
		assertIoctlIntRef(fd, CIoctl.TIOCMBIS, CIoctl.TIOCM_LE);
	}

	@Test
	public void testTiocmbic() throws CException {
		CIoctl.tiocmbic(fd, CIoctl.TIOCM_LE);
		assertIoctlIntRef(fd, CIoctl.TIOCMBIC, CIoctl.TIOCM_LE);
	}

	@Test
	public void testTiocmset() throws CException {
		CIoctl.tiocmset(fd, CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS);
		assertIoctlIntRef(fd, CIoctl.TIOCMSET, CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS);
	}

	@Test
	public void testTiocmbitSet() throws CException {
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI, true);
		assertIoctlIntRef(fd, CIoctl.TIOCMBIS, CIoctl.TIOCM_RI);
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI, false);
		assertIoctlIntRef(fd, CIoctl.TIOCMBIC, CIoctl.TIOCM_RI);
	}

	@Test
	public void testTiocmbitGet() throws CException {
		ioctlAutoIntRef(CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD);
		assertEquals(CIoctl.tiocmbit(fd, CIoctl.TIOCM_RTS), true);
		assertIoctlIntRef(fd, CIoctl.TIOCMGET, CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD);
		assertEquals(CIoctl.tiocmbit(fd, CIoctl.TIOCM_CD), true);
		assertIoctlIntRef(fd, CIoctl.TIOCMGET, CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD);
		assertEquals(CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI), false);
		assertIoctlIntRef(fd, CIoctl.TIOCMGET, CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD);
	}

	@Test
	public void testMacIossiospeed() throws CException {
		CIoctl.Mac.iossiospeed(fd, 250000);
		var args = lib.ioctl.awaitAuto();
		assertEquals(args.fd(), fd);
		assertEquals(args.request(), CIoctl.Mac.IOSSIOSPEED);
		assertUnlong(args.arg(0), 250000L);
	}

	@Test
	public void testLinuxTiocgserial() throws CException {
		lib.ioctl.autoResponse(args -> {
			serial_struct ss = args.arg(0);
			ss.type = 0x33;
			ss.baud_base = 0x123;
		}, 0);
		var ss = CIoctl.Linux.tiocgserial(fd);
		assertEquals(ss.type, 0x33);
		assertEquals(ss.baud_base, 0x123);
		assertIoctlAuto(fd, CIoctl.Linux.TIOCGSERIAL, ss);
	}

	@Test
	public void testLinuxTiocsserial() throws CException {
		serial_struct ss = new serial_struct();
		ss.type = 0x33;
		ss.baud_base = 0x123;
		CIoctl.Linux.tiocsserial(fd, ss);
		assertIoctlAuto(fd, CIoctl.Linux.TIOCSSERIAL, ss);
	}

	@Test
	public void testFields() throws Exception {
		JnaTestUtil.testAsOs(MAC_OS, Mac.class, CIoctl.class, CIoctl.Mac.class);
		JnaTestUtil.testAsOs(LINUX_OS, Linux.class, CIoctl.class, CIoctl.Linux.class);
	}

	public static class Mac {
		static {
			assertEquals(CIoctl._IO('t', 111), 0x2000746f, "_IO('t', 111)");
			assertEquals(CIoctl._IOR('t', 106, Integer.BYTES), 0x4004746a, "_IOR('t', 106, int)");
			assertEquals(CIoctl._IOW('t', 109, Integer.BYTES), 0x8004746d, "_IOW('t', 109, int)");
			assertEquals(CIoctl._IOWR('B', 102, Integer.BYTES), 0xc0044266, "_IOWR('B', 102, int)");
			assertEquals(CIoctl.TIOCSBRK, 0x2000747b, "TIOCSBRK");
			assertEquals(CIoctl.TIOCCBRK, 0x2000747a, "TIOCCBRK");
			assertEquals(CIoctl.FIONREAD, 0x4004667f, "FIONREAD");
			assertEquals(CIoctl.TIOCEXCL, 0x2000740d, "TIOCEXCL");
			assertEquals(CIoctl.TIOCOUTQ, 0x40047473, "TIOCOUTQ");
			assertEquals(CIoctl.TIOCMGET, 0x4004746a, "TIOCMGET");
			assertEquals(CIoctl.TIOCMBIS, 0x8004746c, "TIOCMBIS");
			assertEquals(CIoctl.TIOCMBIC, 0x8004746b, "TIOCMBIC");
			assertEquals(CIoctl.TIOCMSET, 0x8004746d, "TIOCMSET");
			assertEquals(CIoctl.Mac.IOSSIOSPEED, 0x80085402, "IOSSIOSPEED");
		}
	}

	public static class Linux {
		static {
			assertEquals(CIoctl._IO('t', 111), 0x746f, "_IO('t', 111)");
			assertEquals(CIoctl._IOR('t', 106, Integer.BYTES), 0x8004746a, "_IOR('t', 106, int)");
			assertEquals(CIoctl._IOW('t', 109, Integer.BYTES), 0x4004746d, "_IOW('t', 109, int)");
			assertEquals(CIoctl._IOWR('B', 102, Integer.BYTES), 0xc0044266, "_IOWR('B', 102, int)");
			assertEquals(CIoctl.Linux.ASYNC_SPD_HI, 0x10, "ASYNC_SPD_HI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_VHI, 0x20, "ASYNC_SPD_VHI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_SHI, 0x1000, "ASYNC_SPD_SHI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_CUST, 0x30, "ASYNC_SPD_CUST");
			assertEquals(CIoctl.Linux.ASYNC_SPD_MASK, 0x1030, "ASYNC_SPD_MASK");
			assertEquals(CIoctl.Linux.TIOCGSERIAL, 0x541e, "TIOCGSERIAL");
			assertEquals(CIoctl.Linux.TIOCSSERIAL, 0x541f, "TIOCSSERIAL");
		}
	}

	private void ioctlAutoIntRef(int value) {
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(value), 0);
	}

	private void assertIoctlIntRef(int fd, int request, int value) {
		var args = lib.ioctl.awaitAuto();
		assertEquals(args.fd(), fd);
		assertEquals(args.request(), request);
		assertRef(args.arg(0), value);
	}
	
	private void assertIoctlAuto(int fd, int request, Object...args) {
		lib.ioctl.assertAuto(CtlArgs.of(lib.fd(fd), request, args));
	}
}
