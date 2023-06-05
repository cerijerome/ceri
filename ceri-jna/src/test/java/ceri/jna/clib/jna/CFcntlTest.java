package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import ceri.common.util.Enclosed;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;

public class CFcntlTest {
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
		assertPrivateConstructor(CFcntl.class);
	}

	@Test
	public void testFailToOpenWithParameters() {
		lib.open.error.set(new LastErrorException("test"));
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
		lib.fcntl.autoResponses(3);
		assertEquals(CFcntl.fcntl(fd, 0x111, -1, -2, -3), 3);
		lib.assertFcntl(fd, 0x111, -1, -2, -3);
		assertEquals(CFcntl.fcntl(() -> "test", fd, 0x111, "x"), 3);
		lib.assertFcntlArgs(fd, 0x111, null); // don't check optional args
	}

	@Test
	public void testDupFd() throws CException {
		lib.fcntlAutoResponse(objs -> ((Integer) objs[0]).intValue() + 1);
		assertEquals(CFcntl.dupFd(fd, 777), 778);
		lib.assertFcntl(fd, CFcntl.F_DUPFD, 777);
	}

	@Test
	public void testGetFd() throws CException {
		lib.fcntl.autoResponses(CFcntl.O_CLOEXEC);
		assertEquals(CFcntl.getFd(fd), CFcntl.O_CLOEXEC);
		lib.assertFcntl(fd, CFcntl.F_GETFD);
	}

	@Test
	public void testSetFd() throws CException {
		CFcntl.setFd(fd, CFcntl.O_CLOEXEC);
		lib.assertFcntl(fd, CFcntl.F_SETFD, CFcntl.O_CLOEXEC);
	}

	@Test
	public void testGetFl() throws CException {
		lib.fcntl.autoResponses(CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
		assertEquals(CFcntl.getFl(fd), CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
		lib.assertFcntl(fd, CFcntl.F_GETFL);
	}

	@Test
	public void testSetFl() throws CException {
		CFcntl.setFl(fd, CFcntl.O_RDWR | CFcntl.O_EXCL);
		lib.assertFcntl(fd, CFcntl.F_SETFL, CFcntl.O_RDWR | CFcntl.O_EXCL);
	}

	@Test
	public void testSetFlWithOperator() throws CException {
		lib.fcntl.autoResponses(CFcntl.O_NONBLOCK, 0);
		assertEquals(CFcntl.setFl(fd, flags -> flags | CFcntl.O_RDWR | CFcntl.O_EXCL),
			CFcntl.O_NONBLOCK | CFcntl.O_RDWR | CFcntl.O_EXCL);
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CFcntl.class);
	}
}
