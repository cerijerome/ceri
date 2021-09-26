package ceri.serial.clib.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.clib.OpenFlag.O_CREAT;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.test.FileTestHelper;
import ceri.common.util.OsUtil;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.Seek;
import ceri.serial.clib.jna.CLibNative.sighandler_t;
import ceri.serial.clib.test.TestCLibNative;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.test.JnaTestUtil;

public class CLibTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("file1", "test").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testFailToOpenWithParameters() {
		assertThrown(() -> CLib.open(helper.path("").toString(), 3));
		assertThrown(() -> CLib.open(helper.path("").toString(), 3, 0666));
	}

	@Test
	public void testValidFd() throws CException {
		assertEquals(CLib.validFd(-1), false);
		assertEquals(CLib.validFd(0), true);
		assertEquals(CLib.validFd(1), true);
		assertThrown(() -> CLib.validateFd(-1));
		assertEquals(CLib.validateFd(0), 0);
		assertEquals(CLib.validateFd(1), 1);
	}

	@Test
	public void testReadFromFileDescriptor() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertEquals(CLib.read(fd, null, 0), 0);
			Memory m = new Memory(4);
			assertEquals(CLib.read(fd, m, 1), 1);
			JnaTestUtil.assertPointer(m, 0, 't');
			assertEquals(CLib.read(fd, m, 4), 3);
			JnaTestUtil.assertPointer(m, 0, 'e', 's', 't');
			assertEquals(CLib.read(fd, m, 1), -1);
		} finally {
			CLib.close(fd);
		}
	}

	@Test
	public void testReadBytesFromFileDescriptor() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertArray(CLib.read(fd, 0));
			assertArray(CLib.read(fd, 2), 't', 'e');
			assertArray(CLib.readAll(fd), 's', 't');
		} finally {
			CLib.close(fd);
		}
	}

	@Test
	public void testFailToReadWithBadFileDescriptor() {
		Memory m = new Memory(4);
		assertThrown(() -> CLib.read(-1, m, 4));
	}

	@Test
	public void testWriteToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CLib.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			Memory m = JnaUtil.mallocBytes("test".getBytes());
			assertEquals(CLib.write(fd, Pointer.NULL, 0), 0);
			assertEquals(CLib.write(fd, m, 4), 4);
			assertFile(path, "test".getBytes());
		} finally {
			CLib.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testFailToWriteWithBadFileDescriptor() {
		Memory m = JnaUtil.mallocBytes("test".getBytes());
		assertThrown(() -> CLib.write(-1, m, 4));
	}

	@Test
	public void testWriteBytesToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CLib.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			CLib.write(fd, 't', 'e', 's', 't');
			assertFile(path, "test".getBytes());
		} finally {
			CLib.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testSeekFile() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertEquals(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), 0);
			assertEquals(CLib.lseek(fd, 0, Seek.SEEK_END.value), 4);
			assertEquals(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), 4);
			assertThrown(() -> CLib.lseek(fd, -1, Seek.SEEK_SET.value));
		} finally {
			CLib.close(fd);
		}
	}

	@Test
	public void testSetFilePosition() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			CLib.position(fd, 1);
			assertArray(CLib.readAll(fd), 'e', 's', 't');
		} finally {
			CLib.close(fd);
		}
	}

	@Test
	public void testSetFilePositionFailure() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			lib.lseek.autoResponses(3);
			assertThrown(() -> CLib.position(fd, 2));
		});
	}

	@Test
	public void testSignal() throws CException {
		TestCLibNative.exec(lib -> {
			sighandler_t cb = i -> {};
			var old = CLib.signal(15, cb);
			old.invoke(15); // ignored
			lib.signal.assertAuto(List.of(15, cb));
			CLib.raise(15);
			lib.raise.assertAuto(15);
		});
	}

	@Test
	public void testIoctl() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			lib.ioctl.autoResponses(3);
			assertEquals(CLib.ioctl(fd, 0x111, -1, -2, -3), 3);
			lib.ioctl.assertAuto(List.of(lib.fd(fd), 0x111, -1, -2, -3));
			assertEquals(CLib.ioctl(() -> "test", fd, 0x111, -1, -2, -3), 3);
			lib.ioctl.assertAuto(List.of(lib.fd(fd), 0x111, -1, -2, -3));
		});
	}

	@Test
	public void testBreak() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			CLib.tiocsbrk(fd);
			lib.ioctl.assertValues(List.of(lib.fd(fd), CLib.TIOCSBRK));
			CLib.tioccbrk(fd);
			lib.ioctl.assertValues(List.of(lib.fd(fd), CLib.TIOCCBRK));
		});
	}

	@Test
	public void testMacIossiospeed() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			CLib.Mac.iossiospeed(fd, 250000);
			lib.ioctl.assertAuto(List.of(lib.fd(fd), CLib.Mac.IOSSIOSPEED, 250000));
		});
	}

	@Test
	public void testMacTcsetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			var termios = new CLib.Mac.termios();
			CLib.Mac.tcsetattr(fd, 1, termios);
			lib.tcsetattr.assertAuto(List.of(lib.fd(fd), 1, termios.getPointer()));
		});
	}

	@Test
	public void testMacTcgetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			lib.tcgetattr.autoResponse(list -> {
				Pointer p = (Pointer) list.get(1);
				p.setNativeLong(0, JnaUtil.unlong(0x1234));
				return 0;
			});
			var t = CLib.Mac.tcgetattr(fd);
			assertEquals(t.c_iflag, JnaUtil.unlong(0x1234));
		});
	}

	@Test
	public void testLinuxTcsetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			var termios = new CLib.Linux.termios();
			CLib.Linux.tcsetattr(fd, 1, termios);
			lib.tcsetattr.assertAuto(List.of(lib.fd(fd), 1, termios.getPointer()));
		});
	}

	@Test
	public void testLinuxTcgetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CLib.open("test", 0);
			lib.tcgetattr.autoResponse(list -> {
				Pointer p = (Pointer) list.get(1);
				p.setNativeLong(0, JnaUtil.unlong(0x1234));
				return 0;
			});
			var t = CLib.Linux.tcgetattr(fd);
			assertEquals(t.c_iflag, JnaUtil.unlong(0x1234));
		});
	}

	@Test
	public void testMacIoctls() {
		if (!OsUtil.IS_MAC) return;
		assertIoctl("TIOCSBRK", CLib.TIOCSBRK, 0x2000747b);
		assertIoctl("TIOCCBRK", CLib.TIOCCBRK, 0x2000747a);
		assertIoctl("IOSSIOSPEED", CLib.Mac.IOSSIOSPEED, 0x80085402);
		assertIoctl("TIOCSTOP", CLib._IO('t', 111), 0x2000746f);
		assertIoctl("TIOCMGET", CLib._IOR('t', 106, Integer.BYTES), 0x4004746a);
		assertIoctl("TIOCMSET", CLib._IOW('t', 109, Integer.BYTES), 0x8004746d);
		assertIoctl("BIOCSBLEN", CLib._IOWR('B', 102, Integer.BYTES), 0xc0044266);
	}

	@Test
	public void testLinuxIoctls() {
		if (!OsUtil.IS_LINUX) return;
		assertIoctl("TIOCSBRK", CLib.TIOCSBRK, 0x5427);
		assertIoctl("TIOCCBRK", CLib.TIOCCBRK, 0x5428);
		assertIoctl("RTC_AIE_ON", CLib._IO('p', 0x01), 0x7001);
		assertIoctl("TIOCGPTN", CLib._IOR('T', 0x30, Integer.BYTES), 0x80045430);
		assertIoctl("TIOCSPTLCK", CLib._IOW('T', 0x31, Integer.BYTES), 0x40045431);
		assertIoctl("FIFREEZE", CLib._IOWR('X', 119, Integer.BYTES), 0xc0045877);
	}

	private static void assertIoctl(String name, int value, int expected) {
		assertEquals(value, expected, "Expected %s = 0x%x: 0x%x", name, expected, value);
	}
}
