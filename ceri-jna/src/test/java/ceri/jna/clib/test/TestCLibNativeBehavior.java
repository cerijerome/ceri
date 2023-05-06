package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import static ceri.jna.clib.jna.CUnistd.close;
import static ceri.jna.clib.jna.CUnistd.read;
import static ceri.jna.clib.jna.CUnistd.write;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.test.TestCLibNative.Fd;
import ceri.jna.util.GcMemory;

public class TestCLibNativeBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private int fd;

	@Before
	public void before() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = lib.open("test", O_RDWR, 0666);
	}

	@After
	public void after() {
		lib.close(fd);
		enc.close();
	}

	@Test
	public void shouldProvideAutoErrorLogic() throws CException {
		TestCLibNative.autoError(lib.fcntl, 333, list -> ((int) list.get(1)) < 0, "Test");
		assertEquals(CFcntl.fcntl(fd, -1), 333);
		assertThrown(() -> CFcntl.fcntl(fd, 1));
		lib.fcntl.error.clear();
	}

	@Test
	public void shouldCaptureOpenParams() {
		var fd = fd();
		assertEquals(fd.fd(), this.fd);
		assertEquals(fd.path(), "test");
		assertEquals(fd.flags(), O_RDWR);
		assertEquals(fd.mode(), 0666);
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		lib.read.autoResponses(ByteProvider.of(1, 2, 3), null, ByteProvider.empty());
		assertArray(read(fd, 5), 1, 2, 3);
		lib.read.assertAuto(List.of(fd(), 5));
		assertArray(read(fd, 3));
		lib.read.assertAuto(List.of(fd(), 3));
		assertArray(read(fd, 2));
		lib.read.assertAuto(List.of(fd(), 2));
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		lib.write.autoResponses(2, 1);
		assertEquals(write(fd, GcMemory.mallocBytes(1, 2, 3).m, 3), 2);
		lib.write.assertAuto(List.of(fd(), ByteProvider.of(1, 2, 3)));
		assertEquals(write(fd, (Pointer) null, 2), 1);
		lib.write.assertAuto(List.of(fd(), ByteProvider.of(0, 0)));
	}

	@Test
	public void shouldFailForInvalidFd() {
		assertThrown(() -> close(-1));
	}

	private Fd fd() {
		return lib.fd(fd);
	}
}
