package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertShort;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.clib.OpenFlag.O_CREAT;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.jna.JnaTestUtil.assertMemory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;

public class FileDescriptorBehavior {
	private static final byte[] file1Bytes =
		ByteArray.encoder().writeAscii("test").writeShortMsb(0x3456).bytes();
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("file1", file1Bytes).build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void shouldNotBreachEqualsContract() throws CException {
		try (CFileDescriptor t = open("file1")) {
			@SuppressWarnings("resource")
			CFileDescriptor eq0 = CFileDescriptor.of(t.fd());
			@SuppressWarnings("resource")
			CFileDescriptor ne0 = CFileDescriptor.of(t.fd() + 1);
			exerciseEquals(t, eq0);
			assertAllNotEqual(t, ne0);
		}
	}

	@Test
	public void shouldFailToProvideDescriptorIfClosed() throws CException {
		try (CFileDescriptor fd = open("file1")) {
			fd.fd();
			fd.close();
			assertThrown(() -> fd.fd());
		}
	}

	@Test
	public void shouldFailToWrapInvalidDescriptor() {
		assertThrown(() -> CFileDescriptor.of(-1));
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		Memory m = new Memory(8);
		try (CFileDescriptor fd = open("file1")) {
			assertThat(fd.read(m, 0, 0), is(0));
			assertThat(fd.read(m), is(6));
			assertMemory(m, 0, 6, file1Bytes);
		}
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		Memory m = CUtil.malloc(file1Bytes);
		try (CFileDescriptor fd = open("file2", Mode.of(0666), O_CREAT, O_RDWR)) {
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
				fd.lseek(0, Seek.SEEK_SET);
				assertArray(in.readNBytes(6), 't', 'e', 's', 't', 0x34, 0x56);
				fd.lseek(0, Seek.SEEK_SET);
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

	@Test
	public void shouldReadFile() throws IOException {
		try (CFileDescriptor fd = open("file1")) {
			FileReader r = fd.reader();
			assertThat(r.readAscii(4), is("test"));
			assertShort(r.readShortMsb(), 0x3456);
		}
	}

	@Test
	public void shouldWriteFile() throws IOException {
		try (CFileDescriptor fd = open("file2", Mode.of(0666), O_CREAT, O_RDWR)) {
			FileWriter w = fd.writer();
			w.writeAscii("test").writeShortLsb(0x3456);
			assertFile(helper.path("file2"), ByteUtil.toAscii("testV4"));
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@Test
	public void shouldSetPositionWithinAFile() throws IOException {
		try (CFileDescriptor fd = open("file1")) {
			assertThat(fd.lseek(3, Seek.SEEK_CUR), is(3));
			assertThat(fd.lseek(1, Seek.SEEK_CUR), is(4));
			assertThat(fd.lseek(1, Seek.SEEK_END), is(7));
			assertThat(fd.lseek(0, Seek.SEEK_CUR), is(7));
			assertThat(fd.lseek(4, Seek.SEEK_SET), is(4));
			assertShort(fd.reader().readShortMsb(), 0x3456);
		}
	}

	private CFileDescriptor open(String name, OpenFlag... flags) throws CException {
		return CFileDescriptor.open(helper.path(name).toString(), flags);
	}

	private CFileDescriptor open(String name, Mode mode, OpenFlag... flags) throws CException {
		return CFileDescriptor.open(helper.path(name).toString(), mode, flags);
	}

}
