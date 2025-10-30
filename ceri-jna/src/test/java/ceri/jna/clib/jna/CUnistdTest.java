package ceri.jna.clib.jna;

import static ceri.jna.clib.FileDescriptor.Open.CREAT;
import static ceri.jna.clib.FileDescriptor.Open.RDWR;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.Seek;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaAssert;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.Jna;

public class CUnistdTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private FileTestHelper helper = null;
	private int fd = -1;
	private Memory m = null;

	@After
	public void after() {
		if (fd != -1) CUnistd.closeSilently(fd);
		Closeables.close(m, ref, helper);
		helper = null;
		fd = -1;
		m = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CUnistd.class);
	}

	@Test
	public void testIsatty() throws IOException {
		var lib = ref.init();
		fd = CFcntl.open("test", 0);
		Assert.no(CUnistd.isatty(fd));
		lib.isatty.autoResponses(1);
		Assert.yes(CUnistd.isatty(fd));
	}

	@Test
	public void testPageSize() throws IOException {
		var lib = ref.init();
		lib.pagesize.autoResponses(0x100);
		Assert.equal(CUnistd.getpagesize(), 0x100);
	}

	@Test
	public void testPipe() throws IOException {
		int[] pipefd = CUnistd.pipe();
		CUnistd.writeAll(pipefd[1], new byte[] { 1, 2, 3 }, 0, 3);
		Assert.array(CUnistd.readBytes(pipefd[0], 8), 1, 2, 3); // only reads 3
		CUnistd.close(pipefd[0]);
		CUnistd.close(pipefd[1]);
	}

	@Test
	public void testReadFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		Assert.equal(CUnistd.read(fd, Pointer.NULL, 0), 0);
		Assert.equal(CUnistd.read(fd, m, 1), 1);
		JnaAssert.pointer(m, 0, 't');
		Assert.equal(CUnistd.read(fd, m, 4), 3);
		JnaAssert.pointer(m, 0, 'e', 's', 't');
		Assert.equal(CUnistd.read(fd, m, 1), 0);
	}

	@Test
	public void testReadAllFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		byte[] b = new byte[3];
		Assert.equal(CUnistd.readAll(fd, b), 3);
		Assert.array(b, 't', 'e', 's');
		Assert.equal(CUnistd.readAll(fd, b), 1);
		Assert.array(b, 't', 'e', 's');
		Assert.equal(CUnistd.readAll(fd, b), 0);
	}

	@Test
	public void testReadAllWithBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(3);
		byte[] b = new byte[5];
		Assert.equal(CUnistd.readAll(fd, m, b), 4);
		Assert.array(b, 't', 'e', 's', 't', 0);
	}

	@Test
	public void testReadAllWithPointer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		byte[] b = new byte[5];
		Assert.equal(CUnistd.readAll(fd, m, 3, b), 4);
		Assert.array(b, 't', 'e', 's', 't', 0);
	}

	@Test
	public void testReadAllBytesFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		Assert.array(CUnistd.readAllBytes(fd, 0));
		Assert.array(CUnistd.readAllBytes(fd, 2), 't', 'e');
		Assert.array(CUnistd.readAllBytes(fd, 3), 's', 't');
	}

	@Test
	public void testReadNothingOnNonBlockError() throws IOException {
		var lib = ref.init();
		int fd = CFcntl.open("test", 0);
		lib.read.error.setFrom(ErrNo.EAGAIN::lastError);
		Assert.equal(CUnistd.read(fd, new byte[3]), 0);
	}

	@Test
	public void testReadWithBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		byte[] b = new byte[3];
		Assert.equal(CUnistd.read(fd, m, b), 3);
	}

	@Test
	public void testReadWithUndersizedBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(3);
		Assert.thrown(() -> CUnistd.read(fd, m, new byte[4])); // buffer too small
	}

	@Test
	public void testFailToReadWithBadFileDescriptor() {
		m = new Memory(4);
		Assert.thrown(() -> CUnistd.read(-1, m, 4));
	}

	@Test
	public void testWriteToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = Jna.mallocBytes("test".getBytes());
		Assert.equal(CUnistd.write(fd, Pointer.NULL, 0), 0);
		Assert.equal(CUnistd.write(fd, m, 4), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testFailToWriteWithBadFileDescriptor() {
		m = Jna.mallocBytes("test".getBytes());
		Assert.thrown(() -> CUnistd.write(-1, m, 4));
	}

	@Test
	public void testWriteBytesToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		Assert.equal(CUnistd.write(fd, 't', 'e', 's', 't'), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteBytesWithBufferToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(5);
		Assert.equal(CUnistd.write(fd, m, "test".getBytes()), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteNothingOnNonBlockError() throws IOException {
		var lib = ref.init();
		fd = CFcntl.open("test", 0);
		lib.write.error.setFrom(ErrNo.EAGAIN::lastError);
		Assert.equal(CUnistd.write(fd, new byte[3]), 0);
	}

	@Test
	public void testWriteAllBytesToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		Assert.equal(CUnistd.writeAll(fd, 't', 'e', 's', 't'), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllWithBuffer() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(3);
		Assert.equal(CUnistd.writeAll(fd, m, "test".getBytes()), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllWithPointer() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(3);
		Assert.equal(CUnistd.writeAll(fd, m, 2, "test".getBytes()), 4);
		Assert.file(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllUnderWrite() throws IOException {
		var lib = ref.init();
		fd = CFcntl.open("test", 0);
		m = new Memory(3);
		lib.write.autoResponses(0);
		Assert.equal(CUnistd.writeAll(fd, m, 2), 0);
		Assert.equal(CUnistd.writeAll(fd, m, new byte[4]), 0);
	}

	@Test
	public void testSeekFile() throws IOException {
		initFile();
		fd = open("file1", 0);
		Assert.equal(CUnistd.lseek(fd, 0, Seek.CUR.value), 0);
		Assert.equal(CUnistd.lseek(fd, 0, Seek.END.value), 4);
		Assert.equal(CUnistd.lseek(fd, 0, Seek.CUR.value), 4);
		Assert.thrown(() -> CUnistd.lseek(fd, -1, Seek.SET.value));
	}

	@Test
	public void testSetFilePosition() throws IOException {
		initFile();
		fd = open("file1", 0);
		CUnistd.position(fd, 1);
		Assert.array(CUnistd.readAllBytes(fd, CUnistd.size(fd)), 'e', 's', 't');
	}

	@Test
	public void testSetFilePositionFailure() throws IOException {
		var lib = ref.init();
		int fd = CFcntl.open("test", 0);
		lib.lseek.autoResponses(3);
		Assert.thrown(() -> CUnistd.position(fd, 2));
	}

	@Test
	public void testCloseInvalidFd() throws IOException {
		ref.init();
		CUnistd.close(-1);
		CUnistd.closeSilently(-1);
		int fd = CFcntl.open("test", 0, 0);
		CUnistd.close(fd);
		JnaAssert.cexception(() -> CUnistd.close(fd));
		CUnistd.closeSilently(fd);
	}

	private void initFile() throws IOException {
		helper = FileTestHelper.builder().file("file1", "test").build();
	}

	private int open(String file, int open, int mode) throws IOException {
		return CFcntl.open(helper.path(file).toString(), open, mode);
	}

	private int open(String file, int open) throws IOException {
		return CFcntl.open(helper.path(file).toString(), open);
	}
}
