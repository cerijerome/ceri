package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.clib.FileDescriptor.Open.CREAT;
import static ceri.jna.clib.FileDescriptor.Open.RDWR;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.test.FileTestHelper;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.Seek;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CUnistdTest {
	private FileTestHelper helper = null;
	private TestCLibNative lib = null;
	private Enclosed<RuntimeException, ?> enc = null;
	private int fd = -1;
	private Memory m = null;

	@After
	public void after() {
		if (fd != -1) CUnistd.closeSilently(fd);
		CloseableUtil.close(m, enc, helper);
		helper = null;
		enc = null;
		lib = null;
		fd = -1;
		m = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CUnistd.class);
	}

	@Test
	public void testIsatty() throws IOException {
		initLib();
		fd = CFcntl.open("test", 0);
		assertFalse(CUnistd.isatty(fd));
		lib.isatty.autoResponses(1);
		assertTrue(CUnistd.isatty(fd));
	}

	@Test
	public void testPageSize() throws IOException {
		initLib();
		lib.pagesize.autoResponses(0x100);
		assertEquals(CUnistd.getpagesize(), 0x100);
	}

	@Test
	public void testPipe() throws IOException {
		int[] pipefd = CUnistd.pipe();
		CUnistd.writeAll(pipefd[1], new byte[] { 1, 2, 3 }, 0, 3);
		assertArray(CUnistd.readBytes(pipefd[0], 8), 1, 2, 3); // only reads 3
		CUnistd.close(pipefd[0]);
		CUnistd.close(pipefd[1]);
	}

	@Test
	public void testReadFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		assertEquals(CUnistd.read(fd, Pointer.NULL, 0), 0);
		assertEquals(CUnistd.read(fd, m, 1), 1);
		JnaTestUtil.assertPointer(m, 0, 't');
		assertEquals(CUnistd.read(fd, m, 4), 3);
		JnaTestUtil.assertPointer(m, 0, 'e', 's', 't');
		assertEquals(CUnistd.read(fd, m, 1), 0);
	}

	@Test
	public void testReadAllFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		byte[] b = new byte[3];
		assertEquals(CUnistd.readAll(fd, b), 3);
		assertArray(b, 't', 'e', 's');
		assertEquals(CUnistd.readAll(fd, b), 1);
		assertArray(b, 't', 'e', 's');
		assertEquals(CUnistd.readAll(fd, b), 0);
	}

	@Test
	public void testReadAllWithBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(3);
		byte[] b = new byte[5];
		assertEquals(CUnistd.readAll(fd, m, b), 4);
		assertArray(b, 't', 'e', 's', 't', 0);
	}

	@Test
	public void testReadAllWithPointer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		byte[] b = new byte[5];
		assertEquals(CUnistd.readAll(fd, m, 3, b), 4);
		assertArray(b, 't', 'e', 's', 't', 0);
	}

	@Test
	public void testReadAllBytesFromFileDescriptor() throws IOException {
		initFile();
		fd = open("file1", 0);
		assertArray(CUnistd.readAllBytes(fd, 0));
		assertArray(CUnistd.readAllBytes(fd, 2), 't', 'e');
		assertArray(CUnistd.readAllBytes(fd, 3), 's', 't');
	}

	@Test
	public void testReadNothingOnNonBlockError() throws IOException {
		initLib();
		int fd = CFcntl.open("test", 0);
		lib.read.error.setFrom(ErrNo.EAGAIN::lastError);
		assertEquals(CUnistd.read(fd, new byte[3]), 0);
	}

	@Test
	public void testReadWithBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(4);
		byte[] b = new byte[3];
		assertEquals(CUnistd.read(fd, m, b), 3);
	}

	@Test
	public void testReadWithUndersizedBuffer() throws IOException {
		initFile();
		fd = open("file1", 0);
		m = new Memory(3);
		assertThrown(() -> CUnistd.read(fd, m, new byte[4])); // buffer too small
	}

	@Test
	public void testFailToReadWithBadFileDescriptor() {
		m = new Memory(4);
		assertThrown(() -> CUnistd.read(-1, m, 4));
	}

	@Test
	public void testWriteToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = JnaUtil.mallocBytes("test".getBytes());
		assertEquals(CUnistd.write(fd, Pointer.NULL, 0), 0);
		assertEquals(CUnistd.write(fd, m, 4), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testFailToWriteWithBadFileDescriptor() {
		m = JnaUtil.mallocBytes("test".getBytes());
		assertThrown(() -> CUnistd.write(-1, m, 4));
	}

	@Test
	public void testWriteBytesToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		assertEquals(CUnistd.write(fd, 't', 'e', 's', 't'), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteBytesWithBufferToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(5);
		assertEquals(CUnistd.write(fd, m, "test".getBytes()), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteNothingOnNonBlockError() throws IOException {
		initLib();
		fd = CFcntl.open("test", 0);
		lib.write.error.setFrom(ErrNo.EAGAIN::lastError);
		assertEquals(CUnistd.write(fd, new byte[3]), 0);
	}

	@Test
	public void testWriteAllBytesToFileDescriptor() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		assertEquals(CUnistd.writeAll(fd, 't', 'e', 's', 't'), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllWithBuffer() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(3);
		assertEquals(CUnistd.writeAll(fd, m, "test".getBytes()), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllWithPointer() throws IOException {
		initFile();
		fd = open("file2", Open.encode(CREAT, RDWR), 0666);
		m = new Memory(3);
		assertEquals(CUnistd.writeAll(fd, m, 2, "test".getBytes()), 4);
		assertFile(helper.path("file2"), "test".getBytes());
	}

	@Test
	public void testWriteAllUnderWrite() throws IOException {
		initLib();
		fd = CFcntl.open("test", 0);
		m = new Memory(3);
		lib.write.autoResponses(0);
		assertEquals(CUnistd.writeAll(fd, m, 2), 0);
		assertEquals(CUnistd.writeAll(fd, m, new byte[4]), 0);
	}

	@Test
	public void testSeekFile() throws IOException {
		initFile();
		fd = open("file1", 0);
		assertEquals(CUnistd.lseek(fd, 0, Seek.CUR.value), 0);
		assertEquals(CUnistd.lseek(fd, 0, Seek.END.value), 4);
		assertEquals(CUnistd.lseek(fd, 0, Seek.CUR.value), 4);
		assertThrown(() -> CUnistd.lseek(fd, -1, Seek.SET.value));
	}

	@Test
	public void testSetFilePosition() throws IOException {
		initFile();
		fd = open("file1", 0);
		CUnistd.position(fd, 1);
		assertArray(CUnistd.readAllBytes(fd, CUnistd.size(fd)), 'e', 's', 't');
	}

	@Test
	public void testSetFilePositionFailure() throws IOException {
		initLib();
		int fd = CFcntl.open("test", 0);
		lib.lseek.autoResponses(3);
		assertThrown(() -> CUnistd.position(fd, 2));
	}

	@Test
	public void testCloseInvalidFd() throws IOException {
		CUnistd.close(-1);
		CUnistd.closeSilently(-1);
	}

	private void initLib() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
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
