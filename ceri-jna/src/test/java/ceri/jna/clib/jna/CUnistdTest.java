package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
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
	public void testReadFromFileDescriptor() throws CException {
		int fd = CFcntl.open(helper.path("file1").toString(), 0);
		try (Memory m = new Memory(4)) {
			assertEquals(CUnistd.read(fd, null, 0), 0);
			assertEquals(CUnistd.read(fd, m, 1), 1);
			JnaTestUtil.assertPointer(m, 0, 't');
			assertEquals(CUnistd.read(fd, m, 4), 3);
			JnaTestUtil.assertPointer(m, 0, 'e', 's', 't');
			assertEquals(CUnistd.read(fd, m, 1), -1);
		} finally {
			CUnistd.close(fd);
		}
	}

	@Test
	public void testReadBytesFromFileDescriptor() throws CException {
		int fd = CFcntl.open(helper.path("file1").toString(), 0);
		try {
			assertArray(CUnistd.read(fd, 0));
			assertArray(CUnistd.read(fd, 2), 't', 'e');
			assertArray(CUnistd.readAll(fd), 's', 't');
		} finally {
			CUnistd.close(fd);
		}
	}

	@Test
	public void testFailToReadWithBadFileDescriptor() {
		try (Memory m = new Memory(4)) {
			assertThrown(() -> CUnistd.read(-1, m, 4));
		}
	}

	@Test
	public void testWriteToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CFcntl.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			try (Memory m = JnaUtil.mallocBytes("test".getBytes())) {
				assertEquals(CUnistd.write(fd, Pointer.NULL, 0), 0);
				assertEquals(CUnistd.write(fd, m, 4), 4);
			}
			assertFile(path, "test".getBytes());
		} finally {
			CUnistd.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testWriteToFileDescriptor0() throws IOException {
		Path path = helper.path("file2");
		int fd = CFcntl.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			try (Memory m = JnaUtil.mallocBytes("test".getBytes())) {
				assertEquals(CUnistd.write(fd, Pointer.NULL, 0), 0);
				assertEquals(CUnistd.write(fd, m, 4), 4);
				assertFile(path, "test".getBytes());
			}
		} finally {
			CUnistd.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testFailToWriteWithBadFileDescriptor() {
		try (Memory m = JnaUtil.mallocBytes("test".getBytes())) {
			assertThrown(() -> CUnistd.write(-1, m, 4));
		}
	}

	@Test
	public void testWriteBytesToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CFcntl.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			CUnistd.write(fd, 't', 'e', 's', 't');
			assertFile(path, "test".getBytes());
		} finally {
			CUnistd.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testSeekFile() throws CException {
		int fd = CFcntl.open(helper.path("file1").toString(), 0);
		try {
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_CUR.value), 0);
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_END.value), 4);
			assertEquals(CUnistd.lseek(fd, 0, Seek.SEEK_CUR.value), 4);
			assertThrown(() -> CUnistd.lseek(fd, -1, Seek.SEEK_SET.value));
		} finally {
			CUnistd.close(fd);
		}
	}

	@Test
	public void testSetFilePosition() throws CException {
		int fd = CFcntl.open(helper.path("file1").toString(), 0);
		try {
			CUnistd.position(fd, 1);
			assertArray(CUnistd.readAll(fd), 'e', 's', 't');
		} finally {
			CUnistd.close(fd);
		}
	}

	@Test
	public void testSetFilePositionFailure() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.lseek.autoResponses(3);
			assertThrown(() -> CUnistd.position(fd, 2));
		});
	}

}
