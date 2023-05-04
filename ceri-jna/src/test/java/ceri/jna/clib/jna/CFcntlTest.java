package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;

public class CFcntlTest {
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
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CFcntl.class);
	}

	@Test
	public void testFailToOpenWithParameters() {
		assertThrown(() -> CFcntl.open(helper.path("").toString(), 3));
		assertThrown(() -> CFcntl.open(helper.path("").toString(), 3, 0666));
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
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.fcntl.autoResponses(3);
			assertEquals(CFcntl.fcntl(fd, 0x111, -1, -2, -3), 3);
			lib.assertFcntl(fd, 0x111, -1, -2, -3);
			assertEquals(CFcntl.fcntl(() -> "test", fd, 0x111, -1, -2, -3), 3);
			lib.assertFcntl(fd, 0x111, -1, -2, -3);
		});
	}

	@Test
	public void testDupFd() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.fcntlAutoResponse(objs -> ((Integer) objs[0]).intValue() + 1);
			assertEquals(CFcntl.dupFd(fd, 777), 778);
			lib.assertFcntl(fd, CFcntl.F_DUPFD, 777);
		});
	}

	@Test
	public void testGetFd() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.fcntl.autoResponses(CFcntl.O_CLOEXEC);
			assertEquals(CFcntl.getFd(fd), CFcntl.O_CLOEXEC);
			lib.assertFcntl(fd, CFcntl.F_GETFD);
		});
	}

	@Test
	public void testSetFd() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CFcntl.setFd(fd, CFcntl.O_CLOEXEC);
			lib.assertFcntl(fd, CFcntl.F_SETFD, CFcntl.O_CLOEXEC);
		});
	}

	@Test
	public void testGetFl() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.fcntl.autoResponses(CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
			assertEquals(CFcntl.getFl(fd), CFcntl.O_RDWR | CFcntl.O_NONBLOCK);
			lib.assertFcntl(fd, CFcntl.F_GETFL);
		});
	}

	@Test
	public void testSetFl() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			CFcntl.setFl(fd, CFcntl.O_RDWR | CFcntl.O_EXCL);
			lib.assertFcntl(fd, CFcntl.F_SETFL, CFcntl.O_RDWR | CFcntl.O_EXCL);
		});
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CFcntl.class);
	}
}
