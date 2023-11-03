package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTermios.termios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CTermiosTest {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private int fd;

	@BeforeClass
	public static void beforeClass() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
	}

	@AfterClass
	public static void afterClass() {
		enc.close();
	}

	@Before
	public void before() {
		lib.reset();
		fd = lib.open("test", 0);
	}

	@After
	public void after() {
		lib.close(fd);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CTermios.class);
		assertPrivateConstructor(CTermios.Mac.class);
		assertPrivateConstructor(CTermios.Linux.class);
	}

	@Test
	public void testTcsetattr() throws CException {
		var termios = new CTermios.Mac.termios();
		CTermios.tcsetattr(fd, 123, termios);
		lib.tc.assertAuto(TcArgs.of("tcsetattr", lib.fd(fd), 123, termios.getPointer()));
	}

	@Test
	public void testMacTcgetattr() throws CException {
		lib.tc.autoResponse(args -> JnaUtil.unlong(args.arg(0), 0, 0x3456), 0);
		JnaTestUtil.testForEachOs(() -> {
			var t = CTermios.tcgetattr(fd);
			assertEquals(t.c_iflag, JnaUtil.unlong(0x3456));
		});
	}

	@Test
	public void testLinuxTcgetattr() throws CException {
		lib.tc.autoResponse(args -> JnaUtil.unlong(args.arg(0), 0, 0x1234), 0);
		var t = CTermios.Linux.tcgetattr(fd);
		assertEquals(t.c_iflag, JnaUtil.unlong(0x1234));
	}

	@Test
	public void testTcsendbreak() throws CException {
		CTermios.tcsendbreak(fd, 123);
		lib.tc.assertAuto(TcArgs.of("tcsendbreak", lib.fd(fd), 123));
	}

	@Test
	public void testTcdrain() throws CException {
		CTermios.tcdrain(fd);
		lib.tc.assertAuto(TcArgs.of("tcdrain", lib.fd(fd)));
	}

	@Test
	public void testTcflush() throws CException {
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		lib.tc.assertAuto(TcArgs.of("tcflush", lib.fd(fd), CTermios.TCIOFLUSH));
	}

	@Test
	public void testTcflow() throws CException {
		CTermios.tcflow(fd, CTermios.TCION);
		lib.tc.assertAuto(TcArgs.of("tcflow", lib.fd(fd), CTermios.TCION));
	}

	@Test
	public void testCfmakeraw() throws CException {
		termios termios = new CTermios.Linux.termios();
		CTermios.cfmakeraw(termios);
		lib.cf.assertAuto(CfArgs.of("cfmakeraw", termios.getPointer()));
	}

	@Test
	public void testCfgetispeed() throws CException {
		termios termios = new CTermios.Mac.termios();
		lib.cf.autoResponses(12345);
		assertEquals(CTermios.cfgetispeed(termios), 12345);
		lib.cf.assertAuto(CfArgs.of("cfgetispeed", termios.getPointer()));
	}

	@Test
	public void testCfsetispeed() throws CException {
		termios termios = new CTermios.Mac.termios();
		CTermios.cfsetispeed(termios, 12345);
		lib.cf.assertAuto(CfArgs.of("cfsetispeed", termios.getPointer(), 12345));
	}

	@Test
	public void testCfgetospeed() throws CException {
		termios termios = new CTermios.Linux.termios();
		lib.cf.autoResponses(12345);
		assertEquals(CTermios.cfgetospeed(termios), 12345);
		lib.cf.assertAuto(CfArgs.of("cfgetospeed", termios.getPointer()));
	}

	@Test
	public void testCfsetospeed() throws CException {
		termios termios = new CTermios.Linux.termios();
		CTermios.cfsetospeed(termios, 12345);
		lib.cf.assertAuto(CfArgs.of("cfsetospeed", termios.getPointer(), 12345));
	}

	@Test
	public void testOsCoverage() {
		JnaTestUtil.testForEachOs(CTermios.class);
	}

}
