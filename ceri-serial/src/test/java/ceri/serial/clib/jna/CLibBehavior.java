package ceri.serial.clib.jna;

import static ceri.common.test.TestUtil.*;
import static ceri.serial.clib.Mode.Mask.*;
import static ceri.serial.clib.OpenFlag.*;
import static ceri.serial.clib.jna.Ioctls.TIOCSBRK;
import static ceri.serial.clib.jna.SizeOf.INT;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import ceri.common.data.ByteUtil;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.OsUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.Mode;
import ceri.serial.clib.Mode.Mask;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.Seek;
import ceri.serial.javax.SerialPort;
import ceri.serial.javax.SerialPortParams;
import ceri.serial.jna.JnaTestUtil;
import ceri.serial.jna.JnaUtil;
import jtermios.JTermios;
import jtermios.Termios;

public class CLibBehavior {
	private static final int TIOCGETD = OsUtil.macInt(Ioctl._IOR('t', 26, Integer.BYTES), 0x5424);
	private static final int TIOCOUTQ = OsUtil.macInt(Ioctl._IOR('t', 115, Integer.BYTES), 0x5411);
	private static final int TIOCGWINSZ = OsUtil.macInt(Ioctl._IOR('t', 104, Short.BYTES * 4), 0x5413);
	private static final int TIOCSTI = OsUtil.macInt(Ioctl._IOW('t', 114, Byte.BYTES), 0x5412);
	private static final int TIOCGPGRP = OsUtil.macInt(Ioctl._IOR('t', 119, Integer.BYTES), 0x540f);
	private static final int TIOCSTAT = OsUtil.macInt(Ioctl._IO('t', 101), 0x540f);
	
	private static final int FIONREAD = OsUtil.macInt(Ioctl._IOR('f', 127, INT), 0x541b);
	private static FileTestHelper helper = null;
	// private static final int SIOCGIFNAME = 0x8910; // linux-arm

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("file1", "test").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void shouldFailToOpenWithParameters() {
		assertThrown(() -> CLib.open(helper.path("").toString(), 3));
		assertThrown(() -> CLib.open(helper.path("").toString(), 3, 0666));
	}

	@Test
	public void shouldReadFromFileDescriptor() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertThat(CLib.read(fd, null, 0), is(0));
			Memory m = new Memory(4);
			assertThat(CLib.read(fd, m, 1), is(1));
			JnaTestUtil.assertMemory(m, 0, 1, 't');
			assertThat(CLib.read(fd, m, 4), is(3));
			JnaTestUtil.assertMemory(m, 0, 3, 'e', 's', 't');
			assertThat(CLib.read(fd, m, 1), is(-1));
		} finally {
			CUtil.close(fd);
		}
	}

	@Test
	public void shouldFailToReadWithBadFileDescriptor() {
		Memory m = new Memory(4);
		assertThrown(() -> CLib.read(-1, m, 4));
	}

	@Test
	public void shouldWriteToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CLib.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			Memory m = CUtil.malloc("test".getBytes());
			assertThat(CLib.write(fd, null, 0), is(0));
			assertThat(CLib.write(fd, m, 4), is(4));
			assertFile(path, "test".getBytes());
		} finally {
			CUtil.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void shouldFailToWriteWithBadFileDescriptor() {
		Memory m = CUtil.malloc("test".getBytes());
		assertThrown(() -> CLib.write(-1, m, 4));
	}

	@Test
	public void shouldSeekFile() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), is(0));
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_END.value), is(4));
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), is(4));
			assertThrown(() -> CLib.lseek(fd, -1, Seek.SEEK_SET.value));
		} finally {
			CUtil.close(fd);
		}
	}

	//@Test
	public void fuckingIoctl() throws IOException {
		try (SerialPort sp = SerialPort.open("/dev/tty.usbserial-00000000")) {
			sp.setParams(SerialPortParams.DEFAULT);
			BasicUtil.delay(100);
			int fd = sp.nativeFileDescriptor();
			//TermiosUtil.setBreakBit(fd);
			CLib.ioctl(fd, Ioctls.TIOCSBRK);
			//CLib.ioctl(fd, FIONREAD, new Memory(4));
			//CLib.ioctl(fd, FIONREAD, new Memory(4));
			BasicUtil.delay(100);
			CLib.ioctl(fd, FIONREAD, new Memory(4));
			BasicUtil.delay(100);
			//TermiosUtil.clearBreakBit(fd);
			CLib.ioctl(fd, Ioctls.TIOCCBRK);
			BasicUtil.delay(100);
		}
		//try (FileDescriptor fd = FileDescriptor.open("/dev/tty.usbserial-00000000", O_RDWR, O_NOCTTY, O_NONBLOCK)) {
//		try (FileDescriptor fd = FileDescriptor.open("/dev/ptmx", O_RDWR, O_NOCTTY, O_NONBLOCK)) {
//			BasicUtil.delay(100);
//			fd.ioctl(FIONREAD, new Memory(4));
//			BasicUtil.delay(100);
//		}
	}
	
	//@Test
	public void shouldExecuteIoctl() throws CException {
		//int fd = CLib.open("myfifo", OpenFlag.encode(O_RDWR, O_NOCTTY));
		int fd = CLib.open(helper.path("myfifo").toString(),
			OpenFlag.encode(O_CREAT, O_RDWR, O_NONBLOCK, O_CLOEXEC), 010666);
		try {
			//Termios t = TermiosUtil.getTermios(fd);
			//System.out.println(t);
			//CUtil.verifyErrno(() -> JTermios.ioctl(fd, Ioctl._IOR('f', 123, 4), 0), "fuck");
			int cmd = Ioctl._IOR('f', 127, 4);
			Memory m = CUtil.malloc(0, 0, 0, 0, 0, 0, 0, 0);
			//IntByReference i = JnaUtil.intRef(0);
			CLib.ioctl(fd, cmd, m);
			//CLib.ioctl(fd, cmd, i);
			//System.out.println(i.getValue());
			System.out.println(Arrays.toString(JnaUtil.byteArray(m)));
		} finally {
			CUtil.close(fd);
		}
	}
	
}
