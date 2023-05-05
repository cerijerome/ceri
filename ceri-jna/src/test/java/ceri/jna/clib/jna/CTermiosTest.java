package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTermios.termios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CTermiosTest {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private static int fd;

	@BeforeClass
	public static void beforeClass() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = lib.open("test", 0);
	}

	@AfterClass
	public static void afterClass() {
		lib.close(fd);
		enc.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CTermios.class);
	}

	@Test
	public void testTcsetattr() throws CException {
		var termios = new CTermios.Mac.termios();
		CTermios.tcsetattr(fd, 123, termios);
		lib.tcsetattr.assertAuto(List.of(lib.fd(fd), 123, termios.getPointer()));
	}

	@Test
	public void testMacTcgetattr() throws CException {
		lib.tcgetattr.autoResponse(list -> {
			Pointer p = (Pointer) list.get(1);
			p.setNativeLong(0, JnaUtil.unlong(0x3456));
			return 0;
		});
		var t = CTermios.Mac.tcgetattr(fd);
		assertEquals(t.c_iflag, JnaUtil.unlong(0x3456));
	}

	@Test
	public void testLinuxTcgetattr() throws CException {
		lib.tcgetattr.autoResponse(list -> {
			Pointer p = (Pointer) list.get(1);
			p.setNativeLong(0, JnaUtil.unlong(0x1234));
			return 0;
		});
		var t = CTermios.Linux.tcgetattr(fd);
		assertEquals(t.c_iflag, JnaUtil.unlong(0x1234));
	}

	@Test
	public void testTcsendbreak() throws CException {
		CTermios.tcsendbreak(fd, 123);
		lib.tcsendbreak.assertAuto(List.of(lib.fd(fd), 123));
	}

	@Test
	public void testTcdrain() throws CException {
		CTermios.tcdrain(fd);
		lib.tcdrain.assertAuto(lib.fd(fd));
	}

	@Test
	public void testTcflush() throws CException {
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		lib.tcflush.assertAuto(List.of(lib.fd(fd), CTermios.TCIOFLUSH));
	}

	@Test
	public void testTcflow() throws CException {
		CTermios.tcflow(fd, CTermios.TCION);
		lib.tcflow.assertAuto(List.of(lib.fd(fd), CTermios.TCION));
	}

	@Test
	public void testCfmakeraw() throws CException {
		termios termios = new CTermios.Linux.termios();
		CTermios.cfmakeraw(termios);
		lib.cfmakeraw.assertAuto(termios.getPointer());
	}

	@Test
	public void testCfgetispeed() throws CException {
		termios termios = new CTermios.Mac.termios();
		lib.cfgetispeed.autoResponses(12345);
		assertEquals(CTermios.cfgetispeed(termios), 12345);
		lib.cfgetispeed.assertAuto(termios.getPointer());
	}

	@Test
	public void testCfsetispeed() throws CException {
		termios termios = new CTermios.Mac.termios();
		CTermios.cfsetispeed(termios, 12345);
		lib.cfsetispeed.assertAuto(List.of(termios.getPointer(), 12345));
	}

	@Test
	public void testCfgetospeed() throws CException {
		termios termios = new CTermios.Linux.termios();
		lib.cfgetospeed.autoResponses(12345);
		assertEquals(CTermios.cfgetospeed(termios), 12345);
		lib.cfgetospeed.assertAuto(termios.getPointer());
	}

	@Test
	public void testCfsetospeed() throws CException {
		termios termios = new CTermios.Linux.termios();
		CTermios.cfsetospeed(termios, 12345);
		lib.cfsetospeed.assertAuto(List.of(termios.getPointer(), 12345));
	}

	@Test
	public void testOsCoverage() {
		JnaTestUtil.testForEachOs(CTermios.class);
	}

}
