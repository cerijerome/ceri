package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.jna.clib.OpenFlag.O_CREAT;
import static ceri.jna.clib.OpenFlag.O_RDWR;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteUtil;
import ceri.common.test.FileTestHelper;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.util.JnaUtil;

public class FileDescriptorBehavior {
	private static final byte[] file1Bytes =
		ByteArray.Encoder.of().writeAscii("test").writeShortMsb(0x3456).bytes();
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("file1", file1Bytes).build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	public static class TestFd {}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		try (Memory m = new Memory(8); CFileDescriptor fd = open("file1")) {
			assertEquals(fd.read(m, 0, 0), 0);
			assertEquals(fd.read(m), 6);
			assertPointer(m, 0, file1Bytes);
		}
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		try (Memory m = JnaUtil.mallocBytes(file1Bytes);
			CFileDescriptor fd = open("file2", Mode.of(0666), O_CREAT, O_RDWR)) {
			fd.write(m);
			assertFile(helper.path("file2"), 't', 'e', 's', 't', 0x34, 0x56);
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@Test
	public void shouldProvideFileInputStream() throws IOException {
		try (CFileDescriptor fd = open("file1")) {
			try (InputStream in = fd.in()) {
				assertArray(in.readNBytes(4), 't', 'e', 's', 't');
				fd.seek(0, Seek.SEEK_SET);
				assertArray(in.readNBytes(6), 't', 'e', 's', 't', 0x34, 0x56);
				fd.seek(0, Seek.SEEK_SET);
				assertArray(in.readAllBytes(), 't', 'e', 's', 't', 0x34, 0x56);
				assertArray(in.readAllBytes());
			}
		}
	}

	@Test
	public void shouldAllowCustomStreamBufferSize() throws IOException {
		try (CFileDescriptor fd = open("file1")) {
			try (InputStream in = fd.in(6)) {
				assertArray(in.readAllBytes(), 't', 'e', 's', 't', 0x34, 0x56);
				assertArray(in.readAllBytes());
			}
		}
	}

	@Test
	public void shouldProvideFileOutputStream() throws IOException {
		try (CFileDescriptor fd = open("file2", Mode.of(0666), O_CREAT, O_RDWR)) {
			try (OutputStream out = fd.out()) {
				ByteUtil.toAscii("test").writeTo(0, out);
			}
			assertFile(helper.path("file2"), ByteUtil.toAscii("test"));
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetPositionWithinAFile() throws IOException {
		try (CFileDescriptor fd = open("file1")) {
			assertEquals(fd.seek(3, Seek.SEEK_CUR), 3);
			assertEquals(fd.seek(1, Seek.SEEK_CUR), 4);
			assertEquals(fd.seek(1, Seek.SEEK_END), 7);
			assertEquals(fd.seek(0, Seek.SEEK_CUR), 7);
			assertEquals(fd.seek(4, Seek.SEEK_SET), 4);
			assertArray(fd.in().readAllBytes(), 0x34, 0x56);
		}
	}

	@Test
	public void shouldProvideNoOpFd() throws IOException {
		assertEquals(CFcntl.validFd(FileDescriptor.NULL.fd()), false);
		assertEquals(FileDescriptor.NULL.read(null, 1, 5), 5);
		FileDescriptor.NULL.write(null, 1, 5);
		assertEquals(FileDescriptor.NULL.seek(5, null), 0);
		assertEquals(FileDescriptor.NULL.ioctl(0), 0);
		FileDescriptor.NULL.close();
	}

	private CFileDescriptor open(String name, OpenFlag... flags) throws IOException {
		return CFileDescriptor.open(helper.path(name).toString(), flags);
	}

	private CFileDescriptor open(String name, Mode mode, OpenFlag... flags) throws IOException {
		return CFileDescriptor.open(helper.path(name).toString(), mode, flags);
	}

}
