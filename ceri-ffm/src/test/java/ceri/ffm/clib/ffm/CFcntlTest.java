package ceri.ffm.clib.ffm;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;

public class CFcntlTest {
	private static final String FILE = "file1";
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();
	private FileTestHelper helper = null;
	private int fd = -1;

	@After
	public void after() {
		if (fd != -1) CUnistd.closeSilently(fd);
		Closeables.close(lib, helper);
		helper = null;
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CFcntl.class);
	}

	@Test
	public void testOpenFlagDecode() {
		Assert.unordered(CFcntl.Open.xcoder.decodeAll(0), CFcntl.Open.O_RDONLY);
		Assert.unordered(CFcntl.Open.xcoder.decodeAll(1), CFcntl.Open.O_WRONLY);
		Assert.unordered(CFcntl.Open.xcoder.decodeAll(2), CFcntl.Open.O_RDWR);
		Assert.unordered(CFcntl.Open.xcoder.decodeAll(3), CFcntl.Open.O_WRONLY, CFcntl.Open.O_RDWR);
	}

	@Test
	public void testOpenFlagValue() {
		Assert.equal(CFcntl.Open.of(CFcntl.Open.O_RDONLY).value(), 0);
		Assert.unordered(CFcntl.Open.of(CFcntl.Open.O_RDONLY).flags(), CFcntl.Open.O_RDONLY);
	}

	@Test
	public void testModeMasks() {
		Assert.unordered(CFcntl.Mode.of(0666).modes(), CFcntl.Mode.S_IWOTH, CFcntl.Mode.S_IROTH,
			CFcntl.Mode.S_IWGRP, CFcntl.Mode.S_IRGRP, CFcntl.Mode.S_IWUSR, CFcntl.Mode.S_IRUSR);
		Assert.equal(
			CFcntl.Mode.of(CFcntl.Mode.S_IRWXO, CFcntl.Mode.S_IRWXG, CFcntl.Mode.S_IRWXU).value(),
			0777);
		Assert.string(CFcntl.Mode.of(0456), "0456");
	}

	@Test
	public void testOpenErrors() {
		FfmAssert.cexception(CErrNo.ENOENT, () -> CFcntl.open(null, 0));
		FfmAssert.cexception(CErrNo.ENOENT, () -> CFcntl.open(FILE, 0));
		FfmAssert.cexception(CErrNo.ENOENT, () -> CFcntl.open(FILE, 0, 0));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.open(FILE, 3));
	}

	@Test
	public void testOpenEmulatedErrors() {
		lib.init().open.autoResponse(null, CErrNo.EEXIST);
		FfmAssert.cexception(CErrNo.ENOENT, () -> CFcntl.open(null, 0));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.open(FILE, 3));
		FfmAssert.cexception(CErrNo.EEXIST, () -> CFcntl.open(FILE, 0));
	}

	@Test
	public void testValidateFd() throws CException {
		Assert.equal(CFcntl.validateFd(0), 0);
		Assert.equal(CFcntl.validateFd(1), 1);
		Assert.equal(CFcntl.validateFd(777), 777);
		FfmAssert.cexception(() -> CFcntl.validateFd(-1));
		FfmAssert.cexception(() -> CFcntl.validateFd(-2));
	}

	@Test
	public void testDupFd() throws IOException {
		initFile();
		int fd2 = CFcntl.dupFd(fd, 1000);
		Assert.equal(CFcntl.getFd(fd2), CFcntl.getFd(fd));
		Assert.equal(CFcntl.getFl(fd2), CFcntl.getFl(fd));
		CUnistd.close(fd2);
	}

	@Test
	public void testFcntlFd() throws IOException {
		initFile();
		int flags = CFcntl.getFd(fd);
		CFcntl.setFd(fd, flags);
	}

	@Test
	public void testFcntlApplyFd() throws IOException {
		initFile();
		CFcntl.applyFd(fd, f -> f); // no change
		CFcntl.applyFd(fd, f -> f | CFcntl.FD_CLOEXEC);
	}

	@Test
	public void testFcntlFl() throws IOException {
		initFile();
		int flags = CFcntl.getFl(fd);
		CFcntl.setFl(fd, flags);
	}

	@Test
	public void testFcntlApplyFl() throws IOException {
		initFile();
		CFcntl.applyFl(fd, f -> f);
		CFcntl.applyFl(fd, f -> f | CFcntl.Open.O_NONBLOCK.value);
	}

	@Test
	public void testFcntlErrors() throws IOException {
		initFile();
		FfmAssert.cexception(CErrNo.EBADF, () -> CFcntl.dupFd(-1, 1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.dupFd(fd, -1));
	}

	@Test
	public void testFcntlEmulated() throws CException {
		lib.init().fcntl.autoResponse(c -> {
			Assert.equal(c.fd().path(), FILE);
			Assert.equal(c.request(), 1000L);
			Assert.equal(c.arg(0), 1);
			Assert.equal(c.arg(1), -1);
			return Result.of(3);
		});
		fd = CFcntl.open(FILE, 0);
		Assert.equal(CFcntl.fcntl(fd, "test", 1000, 1, -1), 3);
	}

	@Test
	public void testFcntlEmulatedErrors() throws CException {
		lib.init().fcntl.autoResponses(Result.errno(CErrNo.EINVAL));
		fd = CFcntl.open(FILE, 0);
		FfmAssert.cexception(CErrNo.EBADF, () -> CFcntl.dupFd(-1, 1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.dupFd(fd, 0));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.getFd(fd));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.setFd(fd, 0));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.getFl(fd));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CFcntl.setFl(fd, 0));
	}

	@Test
	public void testOsCoverage() {
		FfmTesting.testForEachOs(CFcntl.class);
	}

	// support

	private void initFile() throws IOException {
		helper = FileTestHelper.builder().file(FILE, "test").build();
		fd = CFcntl.open(helper.path(FILE));
	}
}
