package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.List;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.clib.test.TestCLibNative;
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
			lib.ioctl.assertAuto(List.of(lib.fd(fd), 0x111, -1, -2, -3));
			assertEquals(CIoctl.ioctl(() -> "test", fd, 0x111, -1, -2, -3), 3);
			lib.ioctl.assertAuto(List.of(lib.fd(fd), 0x111, -1, -2, -3));
		});
	}

	@Test
	public void testBreak() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.tiocsbrk(fd);
			lib.ioctl.assertValues(List.of(lib.fd(fd), CIoctl.TIOCSBRK));
			CIoctl.tioccbrk(fd);
			lib.ioctl.assertValues(List.of(lib.fd(fd), CIoctl.TIOCCBRK));
		});
	}

	@Test
	public void testMacIossiospeed() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CIoctl.Mac.iossiospeed(fd, 250000);
			var list = lib.ioctl.awaitAuto();
			assertEquals(list.get(0), lib.fd(fd));
			assertEquals(list.get(1), CIoctl.Mac.IOSSIOSPEED);
			assertEquals(JnaUtil.unlong((Pointer) list.get(2), 0), 250000L);
		});
	}

	@Test
	public void testMacIoctls() {
		if (!OsUtil.IS_MAC) return;
		assertIoctl("TIOCSBRK", CIoctl.TIOCSBRK, 0x2000747b);
		assertIoctl("TIOCCBRK", CIoctl.TIOCCBRK, 0x2000747a);
		assertIoctl("IOSSIOSPEED", CIoctl.Mac.IOSSIOSPEED, 0x80085402);
		assertIoctl("TIOCSTOP", CIoctl._IO('t', 111), 0x2000746f);
		assertIoctl("TIOCMGET", CIoctl._IOR('t', 106, Integer.BYTES), 0x4004746a);
		assertIoctl("TIOCMSET", CIoctl._IOW('t', 109, Integer.BYTES), 0x8004746d);
		assertIoctl("BIOCSBLEN", CIoctl._IOWR('B', 102, Integer.BYTES), 0xc0044266);
	}

	@Test
	public void testLinuxIoctls() {
		if (!OsUtil.IS_LINUX) return;
		assertIoctl("TIOCSBRK", CIoctl.TIOCSBRK, 0x5427);
		assertIoctl("TIOCCBRK", CIoctl.TIOCCBRK, 0x5428);
		assertIoctl("RTC_AIE_ON", CIoctl._IO('p', 0x01), 0x7001);
		assertIoctl("TIOCGPTN", CIoctl._IOR('T', 0x30, Integer.BYTES), 0x80045430);
		assertIoctl("TIOCSPTLCK", CIoctl._IOW('T', 0x31, Integer.BYTES), 0x40045431);
		assertIoctl("FIFREEZE", CIoctl._IOWR('X', 119, Integer.BYTES), 0xc0045877);
	}

	private static void assertIoctl(String name, int value, int expected) {
		assertEquals(value, expected, "Expected %s = 0x%x: 0x%x", name, expected, value);
	}
}
