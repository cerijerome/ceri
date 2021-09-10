package ceri.serial.clib;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.clib.jna.TestCLibNative.Fd;

public class CFileDescriptorBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private CFileDescriptor fd;

	@Before
	public void before() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = CFileDescriptor.open("test");
	}

	@After
	public void after() throws IOException {
		fd.close();
		enc.close();
	}

	@Test
	public void testIsBroken() {
		assertFalse(CFileDescriptor.isBroken(null));
		assertFalse(CFileDescriptor.isBroken(CException.of(1, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(2, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(121, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(1, "remote i/o")));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		@SuppressWarnings("resource")
		CFileDescriptor eq0 = CFileDescriptor.of(fd.fd());
		@SuppressWarnings("resource")
		CFileDescriptor ne0 = CFileDescriptor.of(fd.fd() + 1);
		exerciseEquals(fd, eq0);
		assertAllNotEqual(fd, ne0);
	}

	@Test
	public void shouldFailToWrapInvalidDescriptor() {
		assertThrown(() -> CFileDescriptor.of(-1));
	}

	@Test
	public void shouldFailToProvideDescriptorIfClosed() throws IOException {
		fd.fd();
		fd.close();
		assertThrown(() -> fd.fd());
	}

	@Test
	public void shouldFailOnIncompleteWrite() {
		Memory m = CUtil.mallocBytes(1, 2, 3, 4, 5);
		assertThrown(() -> fd.write(m)); // incomplete write (returns 0)
		lib.write.assertAuto(List.of(fd(), ByteProvider.of(1, 2, 3, 4, 5)));
	}

	@Test
	public void shouldWriteUntilComplete() throws IOException {
		Memory m = CUtil.mallocBytes(1, 2, 3, 4, 5);
		lib.write.autoResponses(2, 3);
		fd.write(m);
		lib.write.assertValues(List.of(fd(), ByteProvider.of(1, 2, 3, 4, 5)),
			List.of(fd(), ByteProvider.of(3, 4, 5)));
	}

	@Test
	public void shouldCallIoctl() throws IOException {
		fd.ioctl(100, "a", 1);
		lib.ioctl.assertAuto(List.of(fd(), 100, "a", 1));
	}

	private Fd fd() {
		return lib.fd(fd.fd());
	}
}
