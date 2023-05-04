package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.jna.test.JnaTestUtil.LINUX_OS;
import static ceri.jna.test.JnaTestUtil.MAC_OS;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import ceri.jna.clib.jna.CIoctl.Linux.serial_struct;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CIoctlTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CIoctl.class);
	}

	@Test
	public void testIoctl() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.ioctl.autoResponses(3);
			assertEquals(CIoctl.ioctl(fd, 0x111, -1, -2, -3), 3);
			lib.assertIoctl(fd, 0x111, -1, -2, -3);
			assertEquals(CIoctl.ioctl(() -> "test", fd, 0x111, -1, -2, -3), 3);
			lib.assertIoctl(fd, 0x111, -1, -2, -3);
		});
	}

	@Test
	public void testBreak() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.tiocsbrk(fd);
			lib.assertIoctl(fd, CIoctl.TIOCSBRK);
			CIoctl.tioccbrk(fd);
			lib.assertIoctl(fd, CIoctl.TIOCCBRK);
		});
	}

	@Test
	public void testFionread() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.ioctlAutoResponse(setIntRef(31));
			assertEquals(CIoctl.fionread(fd), 31);
			lib.assertIoctlArgs(fd, CIoctl.FIONREAD, null);
		});
	}

	@Test
	public void testTioexcl() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.tiocexcl(fd);
			lib.assertIoctl(fd, CIoctl.TIOCEXCL);
		});
	}

	@Test
	public void testTiocmget() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.ioctlAutoResponse(setIntRef(CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI));
			assertEquals(CIoctl.tiocmget(fd), CIoctl.TIOCM_DTR | CIoctl.TIOCM_RI);
			lib.assertIoctlArgs(fd, CIoctl.TIOCMGET, null);
		});
	}

	@Test
	public void testTiocmset() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.tiocmset(fd, CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS);
			lib.assertIoctlArgs(fd, CIoctl.TIOCMSET,
				assertIntRef(CIoctl.TIOCM_CD | CIoctl.TIOCM_RTS));
		});
	}

	@Test
	public void testMacIossiospeed() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.Mac.iossiospeed(fd, 250000);
			lib.assertIoctlArgs(fd, CIoctl.Mac.IOSSIOSPEED,
				objs -> assertEquals(JnaUtil.unlong((Pointer) objs[0], 0), 250000L));
		});
	}

	@Test
	public void testLinuxTiocgserial() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.ioctlAutoResponse(objs -> {
				serial_struct ss = (serial_struct) objs[0];
				ss.type = 0x33;
				ss.baud_base = 0x123;
				return 0;
			});
			var ss = CIoctl.Linux.tiocgserial(fd);
			assertEquals(ss.type, 0x33);
			assertEquals(ss.baud_base, 0x123);
			lib.assertIoctlArgs(fd, CIoctl.Linux.TIOCGSERIAL, null);
		});
	}

	@Test
	public void testLinuxTiocsserial() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			serial_struct ss = new serial_struct();
			ss.type = 0x33;
			ss.baud_base = 0x123;
			CIoctl.Linux.tiocsserial(fd, ss);
			lib.assertIoctlArgs(fd, CIoctl.Linux.TIOCSSERIAL, objs -> {
				serial_struct s = (serial_struct) objs[0];
				assertEquals(s.type, 0x33);
				assertEquals(s.baud_base, 0x123);
			});
		});
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
			assertEquals(CIoctl.TIOCMGET, 0x4004746a, "TIOCMGET");
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
			assertEquals(CIoctl.TIOCSBRK, 0x5427, "TIOCSBRK");
			assertEquals(CIoctl.TIOCCBRK, 0x5428, "TIOCCBRK");
			assertEquals(CIoctl.FIONREAD, 0x541b, "FIONREAD");
			assertEquals(CIoctl.TIOCEXCL, 0x540c, "TIOCEXCL");
			assertEquals(CIoctl.TIOCMGET, 0x5415, "TIOCMGET");
			assertEquals(CIoctl.TIOCMSET, 0x5418, "TIOCMSET");
			assertEquals(CIoctl.Linux.ASYNC_SPD_HI, 0x10, "ASYNC_SPD_HI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_VHI, 0x20, "ASYNC_SPD_VHI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_SHI, 0x1000, "ASYNC_SPD_SHI");
			assertEquals(CIoctl.Linux.ASYNC_SPD_CUST, 0x30, "ASYNC_SPD_CUST");
			assertEquals(CIoctl.Linux.ASYNC_SPD_MASK, 0x1030, "ASYNC_SPD_MASK");
			assertEquals(CIoctl.Linux.TIOCGSERIAL, 0x541e, "TIOCGSERIAL");
			assertEquals(CIoctl.Linux.TIOCSSERIAL, 0x541f, "TIOCSSERIAL");
		}
	}

	private static Consumer<Object[]> assertIntRef(int value) {
		return objs -> assertEquals(((IntByReference) objs[0]).getValue(), value);
	}

	private static ToIntFunction<Object[]> setIntRef(int value) {
		return objs -> {
			((IntByReference) objs[0]).setValue(value);
			return 0;
		};
	}

}
