package ceri.ffm.clib.ffm;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.core.Library;
import ceri.ffm.test.FfmAssert;

public class CUnistdTest {
	private static final String FILE = "file1";
	private final Library.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private FileTestHelper helper = null;
	private int fd = -1;

	@After
	public void after() {
		if (fd != -1) CUnistd.closeSilently(fd);
		Closeables.close(ref, helper);
		helper = null;
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CUnistd.class);
	}

	@Test
	public void testClose() throws CException {
		Assert.equal(CUnistd.close(CUnistd.STDERR_FILENO), false);
		initTestFile().close.autoResponses(null, CErrNo.EINTR);
		Assert.equal(CUnistd.close(fd), true);
		FfmAssert.cexception(CErrNo.EBADF, () -> CUnistd.close(fd));
		fd = CFcntl.open("test", 0);
		Assert.equal(CUnistd.close(fd), false);
	}

	@Test
	public void testCloseSilently() throws CException {
		initTestFile();
		Assert.equal(CUnistd.closeSilently(0, 1), true);
		Assert.equal(CUnistd.closeSilently(fd), true);
		Assert.equal(CUnistd.closeSilently(fd), false);
	}

	@Test
	public void testIsatty() throws IOException {
		initTestFile().isatty.autoResponses(null, CErrNo.ENOTTY, CErrNo.EBADF);
		Assert.yes(CUnistd.isatty(fd));
		Assert.no(CUnistd.isatty(fd));
		FfmAssert.cexception(CErrNo.EBADF, () -> CUnistd.isatty(fd));
	}

	@Test
	public void testPageSize() throws IOException {
		ref.init().pagesize.autoResponses(0x100);
		Assert.equal(CUnistd.getpagesize(), 0x100);
	}

	@Test
	public void testPipe() throws IOException {
		int[] pipefd = CUnistd.pipe();
		CUnistd.writeAll(pipefd[1], 1, 2, 3);
		Assert.array(CUnistd.readBytes(pipefd[0], 8), 1, 2, 3); // only reads 3
		CUnistd.close(pipefd[0]);
		CUnistd.close(pipefd[1]);
	}

	@Test
	public void testPipeErrors() {
		ref.init().pipe.autoResponses(null, null, null, CErrNo.ENFILE);
		Assert.equal(ref.lib().pipe(null), 0);
		Assert.equal(ref.lib().pipe(new int[1]), 0);
		Assert.equal(ref.lib().pipe(new int[2]), 0);
		FfmAssert.result(ref.lib().pipe(new int[2]), -1, CErrNo.ENFILE);
	}

	@Test
	public void testReadToMemory() throws IOException {
		Assert.equal(CUnistd.read(fd, (MemorySegment) null, 1), 0);
	}

	@Test
	public void testReadToBytes() throws IOException {
		initFile();
		Assert.equal(CUnistd.read(fd, (byte[]) null), 0);
		Assert.array(readToBytes(fd, 0));
		Assert.array(readToBytes(fd, 2), 't', 'e');
		Assert.array(readToBytes(fd, 4), 's', 't', 0, 0);
		Assert.array(readToBytes(fd, 2), 0, 0);
	}

	@Test
	public void testReadBytes() throws IOException {
		initFile();
		Assert.array(CUnistd.readBytes(fd, 0));
		Assert.array(CUnistd.readBytes(fd, 2), 't', 'e');
		Assert.array(CUnistd.readBytes(fd, 4), 's', 't');
		Assert.array(CUnistd.readBytes(fd, 2));
	}

	@Test
	public void testReadError() throws IOException {
		initTestFile().read.autoResponses(result(1, 2, 3), error(CErrNo.EAGAIN),
			error(CErrNo.EACCES));
		Assert.array(CUnistd.readBytes(fd, 5), 1, 2, 3);
		Assert.array(CUnistd.readBytes(fd, 3));
		FfmAssert.cexception(CErrNo.EACCES, () -> CUnistd.readBytes(fd, 3));
	}

	@Test
	public void testReadAllToMemory() throws IOException {
		Assert.equal(CUnistd.readAll(fd, (MemorySegment) null), 0);
		Assert.equal(CUnistd.readAll(fd, (MemorySegment) null, 1), 0);
	}

	@Test
	public void testReadAllToBytes() throws IOException {
		initTestFile().read.autoResponses(result(), result(1, 2, 3), result(4, 5), result());
		Assert.equal(CUnistd.readAll(fd, (byte[]) null), 0);
		Assert.array(readAllToBytes(fd, 0));
		Assert.array(readAllToBytes(fd, 3), 0, 0, 0);
		Assert.array(readAllToBytes(fd, 6), 1, 2, 3, 4, 5, 0);
	}

	@Test
	public void testReadAllBytes() throws IOException {
		initTestFile().read.autoResponses(result(), result(1, 2, 3), result(4, 5), result());
		Assert.array(CUnistd.readAllBytes(fd, 0));
		Assert.array(CUnistd.readAllBytes(fd, 3));
		Assert.array(CUnistd.readAllBytes(fd, 6), 1, 2, 3, 4, 5);
	}

	@Test
	public void testReadAllError() throws IOException {
		initTestFile().read.autoResponses(result(1, 2, 3), error(CErrNo.EAGAIN),
			error(CErrNo.EACCES));
		Assert.array(CUnistd.readAllBytes(fd, 5), 1, 2, 3);
		FfmAssert.cexception(CErrNo.EACCES, () -> CUnistd.readAllBytes(fd, 3));
	}

	@Test
	public void testWriteMemory() throws IOException {
		Assert.equal(CUnistd.write(fd, (MemorySegment) null), 0);
		Assert.equal(CUnistd.write(fd, (MemorySegment) null, 1), 0);
	}

	@Test
	public void testWriteBytes() throws IOException {
		createFile();
		Assert.equal(CUnistd.write(fd), 0);
		Assert.equal(CUnistd.write(fd, 1, 2, 3), 3);
		assertFile(1, 2, 3);
		Assert.equal(CUnistd.write(fd, new byte[] { 4, 5 }), 2);
		assertFile(1, 2, 3, 4, 5);
	}

	@Test
	public void testWriteAllMemory() throws IOException {
		Assert.equal(CUnistd.writeAll(fd, (MemorySegment) null), 0);
		Assert.equal(CUnistd.writeAll(fd, (MemorySegment) null, 1), 0);
	}

	@Test
	public void testWriteAllBytes() throws IOException {
		byte[][] bytes = { { 0, 0 }, { 0 } };
		initTestFile().write.autoResponses(result(bytes[0]), result(bytes[1]), result());
		Assert.equal(CUnistd.writeAll(fd), 0);
		Assert.equal(CUnistd.writeAll(fd, new byte[] { 1, 2, 3, 4, 5 }), 3);
		Assert.deepEqual(bytes, new byte[][] { { 1, 2 }, { 3 } });
	}

	@Test
	public void testWriteAllError() throws IOException {
		var bytes = new byte[2];
		initTestFile().write.autoResponses(result(bytes), error(CErrNo.EAGAIN),
			error(CErrNo.EACCES));
		Assert.equal(CUnistd.writeAll(fd, 1, 2, 3), 2);
		Assert.array(bytes, 1, 2);
		FfmAssert.cexception(CErrNo.EACCES, () -> CUnistd.writeAll(fd, 4, 5));
	}

	@Test
	public void testLseek() throws IOException {
		initFile();
		Assert.equal(CUnistd.size(fd), 4L);
		Assert.array(CUnistd.readBytes(fd, 2), 't', 'e');
		Assert.equal(CUnistd.size(fd), 4L);
		Assert.equal(CUnistd.position(fd, 3), 3L);
		Assert.array(CUnistd.readBytes(fd, 3), 't');
	}

	@Test
	public void testLseekErrors() throws IOException {
		initFile();
		Assert.thrown(() -> CUnistd.lseek(fd, 3, null));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CUnistd.lseek(fd, 0, -1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CUnistd.lseek(fd, -1, 0));
	}

	@Test
	public void testLseekResponses() throws IOException {
		initTestFile().lseek.autoResponses(result(3L), error(CErrNo.ESPIPE));
		Assert.equal(CUnistd.lseek(fd, 1, CUnistd.Seek.SEEK_END), 3L);
		FfmAssert.cexception(CErrNo.ESPIPE, () -> CUnistd.lseek(fd, 0, CUnistd.Seek.SEEK_SET));
	}

	// support

	private static byte[] readToBytes(int fd, int size) throws CException {
		var bytes = new byte[size];
		CUnistd.read(fd, bytes);
		return bytes;
	}

	private static byte[] readAllToBytes(int fd, int size) throws CException {
		var bytes = new byte[size];
		CUnistd.readAll(fd, bytes);
		return bytes;
	}

	private static <T> TestCLibNative.Result<T> error(CErrNo errNo) {
		return TestCLibNative.Result.error(errNo);
	}

	private static TestCLibNative.Result<byte[]> result(int... bytes) {
		return TestCLibNative.Result.ofBytes(bytes);
	}

	private static <T> TestCLibNative.Result<T> result(T bytes) {
		return TestCLibNative.Result.of(bytes);
	}

	private void initFile() throws IOException {
		helper = FileTestHelper.builder().file(FILE, "test").build();
		fd = open(FILE, 0);
	}

	private void createFile() throws IOException {
		helper = FileTestHelper.builder().file(FILE, "").build();
		fd = open(FILE, CFcntl.Open.O_RDWR.value, 0666);
	}

	private TestCLibNative initTestFile() throws CException {
		ref.init();
		fd = CFcntl.open(FILE, 0);
		return ref.lib();
	}

	private int open(String file, int open, int mode) throws IOException {
		return CFcntl.open(helper.path(file).toString(), open, mode);
	}

	private int open(String file, int open) throws IOException {
		return CFcntl.open(helper.path(file).toString(), open);
	}

	private void assertFile(int... bytes) throws IOException {
		Assert.array(helper.read(FILE), bytes);
	}
}
