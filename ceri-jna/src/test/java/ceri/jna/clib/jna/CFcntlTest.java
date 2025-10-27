package ceri.jna.clib.jna;

import static ceri.jna.test.JnaTestUtil.LEX;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;

public class CFcntlTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd;

	@After
	public void after() {
		Closeables.close(ref);
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CFcntl.class);
	}

	@Test
	public void testFailToOpenWithParameters() {
		var lib = ref.init();
		lib.open.error.setFrom(LEX);
		Assert.thrown(() -> CFcntl.open("test1", 3));
		Assert.thrown(() -> CFcntl.open("test2", 3, 0666));
		lib.open.error.clear();
	}

	@Test
	public void testValidFd() throws CException {
		Assert.equal(CFcntl.validFd(-1), false);
		Assert.equal(CFcntl.validFd(0), true);
		Assert.equal(CFcntl.validFd(1), true);
		Assert.thrown(() -> CFcntl.validateFd(-1));
		Assert.equal(CFcntl.validateFd(0), 0);
		Assert.equal(CFcntl.validateFd(1), 1);
	}

	@Test
	public void testFcntl() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(3);
		Assert.equal(CFcntl.fcntl(fd, 0x111, -1, -2, -3), 3);
		assertFcntlAuto(fd, 0x111, -1, -2, -3);
		Assert.equal(CFcntl.fcntl(() -> "test", fd, 0x111, "x"), 3);
		assertFcntlAuto(fd, 0x111, "x");
	}

	@Test
	public void testDupFd() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponse(args -> args.<Integer>arg(0) + 1);
		Assert.equal(CFcntl.dupFd(fd, 777), 778);
		assertFcntlAuto(fd, CFcntl.F_DUPFD, 777);
	}

	@Test
	public void testGetFd() throws CException {
		var lib = initFd();
		lib.fcntl.autoResponses(CFcntl.O_CLOEXEC);
		Assert.equal(CFcntl.getFd(fd), CFcntl.O_CLOEXEC);
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
		Assert.equal(CFcntl.getFl(fd), CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
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
		Assert.equal(CFcntl.setFl(fd, flags -> flags | CFcntl.O_RDWR | CFcntl.O_EXCL),
			CFcntl.O_NONBLOCK | CFcntl.O_RDWR | CFcntl.O_EXCL);
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CFcntl.class);
	}

	private void assertFcntlAuto(int fd, int request, Object... args) {
		var lib = ref.lib();
		Assert.equal(lib.fcntl.awaitAuto(), CtlArgs.of(lib.fd(fd), request, args));
	}

	private TestCLibNative initFd() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		return lib;
	}
}
