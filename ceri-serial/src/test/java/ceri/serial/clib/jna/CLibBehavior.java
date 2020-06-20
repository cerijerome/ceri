package ceri.serial.clib.jna;

import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.serial.clib.OpenFlag.O_CREAT;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.test.FileTestHelper;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.Seek;
import ceri.serial.jna.test.JnaTestUtil;

public class CLibBehavior {
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
	public void shouldFailToOpenWithParameters() {
		assertThrown(() -> CLib.open(helper.path("").toString(), 3));
		assertThrown(() -> CLib.open(helper.path("").toString(), 3, 0666));
	}

	@Test
	public void shouldReadFromFileDescriptor() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertThat(CLib.read(fd, null, 0), is(0));
			Memory m = new Memory(4);
			assertThat(CLib.read(fd, m, 1), is(1));
			JnaTestUtil.assertPointer(m, 0, 't');
			assertThat(CLib.read(fd, m, 4), is(3));
			JnaTestUtil.assertPointer(m, 0, 'e', 's', 't');
			assertThat(CLib.read(fd, m, 1), is(-1));
		} finally {
			CUtil.close(fd);
		}
	}

	@Test
	public void shouldFailToReadWithBadFileDescriptor() {
		Memory m = new Memory(4);
		assertThrown(() -> CLib.read(-1, m, 4));
	}

	@Test
	public void shouldWriteToFileDescriptor() throws IOException {
		Path path = helper.path("file2");
		int fd = CLib.open(path.toString(), OpenFlag.encode(O_CREAT, O_RDWR), 0666);
		try {
			Memory m = CUtil.malloc("test".getBytes());
			assertThat(CLib.write(fd, null, 0), is(0));
			assertThat(CLib.write(fd, m, 4), is(4));
			assertFile(path, "test".getBytes());
		} finally {
			CUtil.close(fd);
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void shouldFailToWriteWithBadFileDescriptor() {
		Memory m = CUtil.malloc("test".getBytes());
		assertThrown(() -> CLib.write(-1, m, 4));
	}

	@Test
	public void shouldSeekFile() throws CException {
		int fd = CLib.open(helper.path("file1").toString(), 0);
		try {
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), is(0));
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_END.value), is(4));
			assertThat(CLib.lseek(fd, 0, Seek.SEEK_CUR.value), is(4));
			assertThrown(() -> CLib.lseek(fd, -1, Seek.SEEK_SET.value));
		} finally {
			CUtil.close(fd);
		}
	}

}
