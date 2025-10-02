package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.jna.clib.jna.CTermios.tcflag_t;
import ceri.jna.clib.jna.CTermios.termios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaOs;

public class CTermiosTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd;

	@After
	public void after() {
		Closeables.close(ref);
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CTermios.class);
		assertPrivateConstructor(CTermios.Mac.class);
		assertPrivateConstructor(CTermios.Linux.class);
	}

	@Test
	public void testSpeedRef() {
		var ref = new CTermios.speed_t.ByRef();
		ref.setValue(new CTermios.speed_t(-1));
		assertEquals(ref.longValue(), 0xffffffffL);
	}

	@Test
	public void testTcsetattr() throws CException {
		var lib = initFd();
		var termios = new CTermios.Mac.termios();
		CTermios.tcsetattr(fd, 123, termios);
		lib.tc.assertAuto(TcArgs.of("tcsetattr", lib.fd(fd), 123, termios.getPointer()));
	}

	@Test
	public void testMacTcgetattr() throws CException {
		var lib = initFd();
		lib.tc.autoResponse(args -> new tcflag_t(0x3456).write(args.arg(0), 0), 0);
		JnaOs.forEach(_ -> {
			var t = CTermios.tcgetattr(fd);
			assertEquals(t.c_iflag, new tcflag_t(0x3456));
		});
	}

	@Test
	public void testTcsendbreak() throws CException {
		var lib = initFd();
		CTermios.tcsendbreak(fd, 123);
		lib.tc.assertAuto(TcArgs.of("tcsendbreak", lib.fd(fd), 123));
	}

	@Test
	public void testTcdrain() throws CException {
		var lib = initFd();
		CTermios.tcdrain(fd);
		lib.tc.assertAuto(TcArgs.of("tcdrain", lib.fd(fd)));
	}

	@Test
	public void testTcflush() throws CException {
		var lib = initFd();
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		lib.tc.assertAuto(TcArgs.of("tcflush", lib.fd(fd), CTermios.TCIOFLUSH));
	}

	@Test
	public void testTcflow() throws CException {
		var lib = initFd();
		CTermios.tcflow(fd, CTermios.TCION);
		lib.tc.assertAuto(TcArgs.of("tcflow", lib.fd(fd), CTermios.TCION));
	}

	@Test
	public void testCfmakeraw() throws CException {
		var lib = ref.init();
		termios termios = new CTermios.Linux.termios();
		CTermios.cfmakeraw(termios);
		lib.cf.assertAuto(CfArgs.of("cfmakeraw", termios.getPointer()));
	}

	@Test
	public void testCfgetispeed() throws CException {
		var lib = ref.init();
		termios termios = new CTermios.Mac.termios();
		lib.cf.autoResponses(12345);
		assertEquals(CTermios.cfgetispeed(termios), 12345);
		lib.cf.assertAuto(CfArgs.of("cfgetispeed", termios.getPointer()));
	}

	@Test
	public void testCfsetispeed() throws CException {
		var lib = ref.init();
		termios termios = new CTermios.Mac.termios();
		CTermios.cfsetispeed(termios, 12345);
		lib.cf.assertAuto(CfArgs.of("cfsetispeed", termios.getPointer(), 12345));
	}

	@Test
	public void testCfgetospeed() throws CException {
		var lib = ref.init();
		termios termios = new CTermios.Linux.termios();
		lib.cf.autoResponses(12345);
		assertEquals(CTermios.cfgetospeed(termios), 12345);
		lib.cf.assertAuto(CfArgs.of("cfgetospeed", termios.getPointer()));
	}

	@Test
	public void testCfsetospeed() throws CException {
		var lib = ref.init();
		termios termios = new CTermios.Linux.termios();
		CTermios.cfsetospeed(termios, 12345);
		lib.cf.assertAuto(CfArgs.of("cfsetospeed", termios.getPointer(), 12345));
	}

	@Test
	public void testOsCoverage() {
		ref.init();
		JnaTestUtil.testForEachOs(CTermios.class);
	}

	private TestCLibNative initFd() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		return lib;
	}
}
