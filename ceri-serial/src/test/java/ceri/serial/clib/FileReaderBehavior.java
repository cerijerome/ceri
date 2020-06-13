package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertByte;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.serial.jna.JnaTestUtil.assertMemory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.test.FileTestHelper;
import ceri.serial.clib.jna.CException;

public class FileReaderBehavior {
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
	public void shouldReadBytes() throws IOException {
		try (CFileDescriptor fd = read("file1")) {
			FileReader r = FileReader.of(fd);
			assertByte(r.readByte(), 't');
			assertByte(r.readByte(), 'e');
			r.skip(2);
			assertByte(r.readByte(), '4');
		}
	}

	@Test
	public void shouldThrowExceptionIFUnableToReadBytes() throws IOException {
		try (CFileDescriptor fd = read("file1")) {
			FileReader r = FileReader.of(fd);
			r.skip(6);
			assertThrown(() -> r.readByte());
		}
	}

	@Test
	public void shouldReadIntoByteReceiver() throws IOException {
		Mutable m = Mutable.of(10);
		try (CFileDescriptor fd = read("file1")) {
			FileReader r = FileReader.of(fd);
			assertThat(r.readInto(m, 2, 4), is(6));
			assertThrown(() -> r.readInto(m, 6, 4));
			assertArray(m.copy(0), 0, 0, 't', 'e', 's', 't', 0, 0, 0, 0);
		}
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		Memory m = new Memory(10);
		try (CFileDescriptor fd = read("file1")) {
			FileReader r = FileReader.of(fd);
			assertThat(r.readInto(m, 2, 4), is(6));
			assertThrown(() -> r.readInto(m, 6, 4));
			assertMemory(m, 0, 10, 0, 0, 't', 'e', 's', 't', '4', 'V', 0, 0);
		}
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (CFileDescriptor fd = read("file1")) {
			FileReader r = FileReader.of(fd);
			assertThat(r.transferTo(out, 4), is(4));
			assertThrown(() -> r.transferTo(out, 4));
			assertArray(out.toByteArray(), 't', 'e', 's', 't');
		}
	}

	private CFileDescriptor read(String name) throws CException {
		return CFileDescriptor.open(helper.path(name).toString());
	}
}
