package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.test.JnaTestUtil.LEX;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;

public class CFcntlTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd;

	@After
	public void after() {
		CloseableUtil.close(ref);
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CFcntl.class);
	}

	@Test
	public void testFailToOpenWithParameters() {
		var lib = ref.init();
		lib.open.error.setFrom(LEX);
		assertThrown(() -> CFcntl.open("test1", 3));
		assertThrown(() -> CFcntl.open("test2", 3, 0666));
		lib.open.error.clear();
	}

	@Test
	public void testValidFd() throws CException {
		assertEquals(CFcntl.validFd(-1), false);
		assertEquals(CFcntl.validFd(0), true);
		assertEquals(CFcntl.validFd(1), true);
		assertThrown(() -> CFcntl.validateFd(-1));
		assertEquals(CFcntl.validateFd(0), 0);
		assertEquals(CFcntl.validateFd(1), 1);
	}

	@Test
	public void testFcntl() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(3);
		assertEquals(CFcntl.fcntl(fd, 0x111, -1, -2, -3), 3);
		assertFcntlAuto(fd, 0x111, -1, -2, -3);
		assertEquals(CFcntl.fcntl(() -> "test", fd, 0x111, "x"), 3);
		assertFcntlAuto(fd, 0x111, "x");
	}

	@Test
	public void testDupFd() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponse(args -> args.<Integer>arg(0) + 1);
		assertEquals(CFcntl.dupFd(fd, 777), 778);
		assertFcntlAuto(fd, CFcntl.F_DUPFD, 777);
	}

	@Test
	public void testGetFd() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(CFcntl.O_CLOEXEC);
		assertEquals(CFcntl.getFd(fd), CFcntl.O_CLOEXEC);
		assertFcntlAuto(fd, CFcntl.F_GETFD);
	}

	@Test
	public void testSetFd() throws CException {
		initFd();
		CFcntl.setFd(fd, CFcntl.O_CLOEXEC);
		assertFcntlAuto(fd, CFcntl.F_SETFD, CFcntl.O_CLOEXEC);
	}

	@Test
	public void testGetFl() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
		assertEquals(CFcntl.getFl(fd), CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
		assertFcntlAuto(fd, CFcntl.F_GETFL);
	}

	@Test
	public void testSetFl() throws CException {
		initFd();
		CFcntl.setFl(fd, CFcntl.O_RDWR | CFcntl.O_EXCL);
		assertFcntlAuto(fd, CFcntl.F_SETFL, CFcntl.O_RDWR | CFcntl.O_EXCL);
	}

	@Test
	public void testSetFlWithOperator() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(CFcntl.O_NONBLOCK, 0);
		assertEquals(CFcntl.setFl(fd, flags -> flags | CFcntl.O_RDWR | CFcntl.O_EXCL),
			CFcntl.O_NONBLOCK | CFcntl.O_RDWR | CFcntl.O_EXCL);
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CFcntl.class);
	}

	private void assertFcntlAuto(int fd, int request, Object... args) {
		var lib = ref.lib();
		assertEquals(lib.fcntl.awaitAuto(), CtlArgs.of(lib.fd(fd), request, args));
	}

	private TestCLibNative initFd() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		return lib;
	}
}
