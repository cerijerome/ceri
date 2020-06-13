package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.test.FileTestHelper;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.JnaMemory;

public class FileWriterBehavior {
	private static final Mode mode = Mode.of(0666);
	private static final Set<OpenFlag> flags = Set.of(OpenFlag.O_CREAT, OpenFlag.O_RDWR);
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
	public void shouldWriteBytes() throws IOException {
		try (CFileDescriptor fd = create("file2")) {
			FileWriter w = FileWriter.of(fd);
			w.writeByte('a').writeByte('b').skip(2).fill(2, 'x');
			assertFile(helper.path("file2"), 'a', 'b', 0, 0, 'x', 'x');
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@Test
	public void shouldWriteFromByteProvider() throws IOException {
		JnaMemory m = JnaMemory.of(CUtil.malloc(file1Bytes));
		try (CFileDescriptor fd = create("file2")) {
			FileWriter w = FileWriter.of(fd);
			w.writeFrom(m).writeFrom(m, 2, 2);
			assertFile(helper.path("file2"), "test4Vst".getBytes());
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		Memory m = CUtil.malloc(file1Bytes);
		try (CFileDescriptor fd = create("file2")) {
			FileWriter w = FileWriter.of(fd);
			w.writeFrom(m, 1, 4);
			assertFile(helper.path("file2"), "est4".getBytes());
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldTransferFromInputStream() throws IOException {
		try (CFileDescriptor fd0 = read("file1")) {
			try (CFileDescriptor fd = create("file2")) {
				FileWriter w = FileWriter.of(fd);
				assertThat(w.transferFrom(fd0.in(), 8), is(6));
				assertFile(helper.path("file2"), "test4V".getBytes());
			}
		} finally {
			Files.delete(helper.path("file2"));
		}
	}

	private CFileDescriptor create(String name) throws CException {
		return CFileDescriptor.open(helper.path(name).toString(), mode, flags);
	}

	private CFileDescriptor read(String name) throws CException {
		return CFileDescriptor.open(helper.path(name).toString());
	}
}
