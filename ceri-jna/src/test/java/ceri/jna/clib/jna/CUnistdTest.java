package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.clib.OpenFlag.O_CREAT;
import static ceri.jna.clib.OpenFlag.O_RDWR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.test.FileTestHelper;
import ceri.jna.clib.OpenFlag;
import ceri.jna.clib.Seek;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CUnistdTest {
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
		assertPrivateConstructor(CUnistd.class);
	}

	@Test
	public void testIsatty() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			assertFalse(CUnistd.isatty(fd));
			lib.isatty.autoResponses(1);
			assertTrue(CUnistd.isatty(fd));
		});
	}

	@Test
	public void testPipe() throws CException {
		int[] pipefd = CUnistd.pipe();
		CUnistd.writeAll(pipefd[1], new byte[] { 1, 2, 3 }, 0, 3);
		assertArray(CUnistd.readBytes(pipefd[0], 8), 1, 2, 3); // only reads 3
		CUnistd.close(pipefd[0]);
		CUnistd.close(pipefd[1]);
	}

	@Test
	public void testReadFromFileDescriptor() throws CException {
		execRead("file1", fd -> {
			try (Memory m = new Memory(4)) {
				assertEquals(CUnistd.read(fd, Pointer.NULL, 0), 0);
				assertEquals(CUnistd.read(fd, m, 1), 1);
				JnaTestUtil.assertPointer(m, 0, 't');
				assertEquals(CUnistd.read(fd, m, 4), 3);
				JnaTestUtil.assertPointer(m, 0, 'e', 's', 't');
				assertEquals(CUnistd.read(fd, m, 1), 0);
			}
		});
	}

	@Test
	public void testReadAllFromFileDescriptor() throws CException {
		execRead("file1", fd -> {
			byte[] b = new byte[3];
			assertEquals(CUnistd.readAll(fd, b), 3);
			assertArray(b, 't', 'e', 's');
			assertEquals(CUnistd.readAll(fd, b), 1);
			assertArray(b, 't', 'e', 's');
			assertEquals(CUnistd.readAll(fd, b), 0);
		});
	}

	@Test
	public void testReadAllWithBuffer() throws CException {
		execRead("file1", fd -> {
			try (var m = new Memory(3)) {
				byte[] b = new byte[5];
				assertEquals(CUnistd.readAll(fd, m, b), 4);
				assertArray(b, 't', 'e', 's', 't', 0);
			}
		});
	}

	@Test
	public void testReadAllWithPointer() throws CException {
		execRead("file1", fd -> {
			try (var m = new Memory(4)) {
				byte[] b = new byte[5];
				assertEquals(CUnistd.readAll(fd, m, 3, b), 4);
				assertArray(b, 't', 'e', 's', 't', 0);
			}
		});
	}

	@Test
	public void testReadAllBytesFromFileDescriptor() throws CException {
		execRead("file1", fd -> {
			assertArray(CUnistd.readAllBytes(fd, 0));
			assertArray(CUnistd.readAllBytes(fd, 2), 't', 'e');
			assertArray(CUnistd.readAllBytes(fd, 3), 's', 't');
		});
	}

	@Test
	public void testReadNothingOnNonBlockError() throws IOException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.read.error.setFrom(CError.EAGAIN::error);
			assertEquals(CUnistd.read(fd, new byte[3]), 0);
		});
	}

	@Test
	public void testReadWithBuffer() throws IOException {
		execRead("file1", fd -> {
			try (Memory m = new Memory(4)) {
				byte[] b = new byte[3];
				assertEquals(CUnistd.read(fd, m, b), 3);
			}
		});
	}

	@Test
	public void testReadWithUndersizedBuffer() throws CException {
		execRead("file1", fd -> {
			try (Memory m = new Memory(3)) {
				assertThrown(() -> CUnistd.read(fd, m, new byte[4])); // buffer too small
			}
		});
	}

	@Test
	public void testFailToReadWithBadFileDescriptor() {
		try (Memory m = new Memory(4)) {
			assertThrown(() -> CUnistd.read(-1, m, 4));
		}
	}

	@Test
	public void testWriteToFileDescriptor() throws IOException {
		execWrite("file2", fd -> {
			try (Memory m = JnaUtil.mallocBytes("test".getBytes())) {
				assertEquals(CUnistd.write(fd, Pointer.NULL, 0), 0);
				assertEquals(CUnistd.write(fd, m, 4), 4);
				assertFile(helper.path("file2"), "test".getBytes());
			}
		});
	}

	@Test
	public void testFailToWriteWithBadFileDescriptor() {
		try (Memory m = JnaUtil.mallocBytes("test".getBytes())) {
			assertThrown(() -> CUnistd.write(-1, m, 4));
		}
	}

	@Test
	public void testWriteBytesToFileDescriptor() throws IOException {
		execWrite("file2", fd -> {
			assertEquals(CUnistd.write(fd, 't', 'e', 's', 't'), 4);
			assertFile(helper.path("file2"), "test".getBytes());
		});
	}

	@Test
	public void testWriteBytesWithBufferToFileDescriptor() throws IOException {
		execWrite("file2", fd -> {
			try (Memory m = new Memory(5)) {
				assertEquals(CUnistd.write(fd, m, "test".getBytes()), 4);
				assertFile(helper.path("file2"), "test".getBytes());
			}
		});
	}

	@Test
	public void testWriteNothingOnNonBlockError() throws IOException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.write.error.setFrom(CError.EAGAIN::error);
			assertEquals(CUnistd.write(fd, new byte[3]), 0);
		});
	}

	@Test
	public void testWriteAllBytesToFileDescriptor() throws IOException {
		execWrite("file2", fd -> {
			assertEquals(CUnistd.writeAll(fd, 't', 'e', 's', 't'), 4);
			assertFile(helper.path("file2"), "test".getBytes());
		});
	}

	@Test
	public void testWriteAllWithBuffer() throws IOException {
		execWrite("file2", fd -> {
			try (Memory m = new Memory(3)) {
				assertEquals(CUnistd.writeAll(fd, m, "test".getBytes()), 4);
				assertFile(helper.path("file2"), "test".getBytes());
			}
		});
	}

	@Test
	public void testWriteAllWithPointer() throws IOException {
		execWrite("file2", fd -> {
			try (Memory m = new Memory(3)) {
				assertEquals(CUnistd.writeAll(fd, m, 2, "test".getBytes()), 4);
				assertFile(helper.path("file2"), "test".getBytes());
			}
		});
	}

	@Test
	public void testWriteAllUnderWrite() throws IOException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			try (Memory m = new Memory(3)) {
				lib.write.autoResponses(0);
				assertEquals(CUnistd.writeAll(fd, m, 2), 0);
				assertEquals(CUnistd.writeAll(fd, m, new byte[4]), 0);
			}
		});
		
		
		execWrite("file2", fd -> {
		});
	}

	@Test
	public void testSeekFile() throws CException {
		execRead("file1", fd -> {
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_CUR.value), 0);
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_END.value), 4);
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_CUR.value), 4);
			assertThrown(() -> CUnistd.lseek(fd, -1, Seek.SEEK_SET.value));
		});
	}

	@Test
	public void testSetFilePosition() throws CException {
		execRead("file1", fd -> {
			CUnistd.position(fd, 1);
			assertArray(CUnistd.readAllBytes(fd, CUnistd.size(fd)), 'e', 's', 't');
		});
	}

	@Test
	public void testSetFilePositionFailure() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.lseek.autoResponses(3);
			assertThrown(() -> CUnistd.position(fd, 2));
		});
	}

	@Test
	public void testCloseInvalidFd() throws CException {
		CUnistd.close(-1);
		CUnistd.closeSilently(-1);
	}

	private static <E extends Exception> void execRead(String file, ExceptionIntConsumer<E> tester)
		throws CException, E {
		int fd = CFcntl.open(helper.path(file).toString(), 0);
		try {
			tester.accept(fd);
		} finally {
			CUnistd.closeSilently(fd);
		}
	}

	private static <E extends Exception> void execWrite(String file, ExceptionIntConsumer<E> tester)
		throws IOException, E {
		Path path = helper.path(file);
		int fd = CFcntl.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			tester.accept(fd);
		} finally {
			CUnistd.closeSilently(fd);
			Files.deleteIfExists(path);
		}
	}

}
