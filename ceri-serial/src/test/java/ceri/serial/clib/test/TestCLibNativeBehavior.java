package ceri.serial.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaUtil.nlong;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CLib;
import ceri.serial.clib.test.TestCLibNative.Fd;
import ceri.serial.jna.JnaUtil;

public class TestCLibNativeBehavior {
	private Enclosed<RuntimeException, TestCLibNative> enclosed;
	private TestCLibNative clib;
	private int fd;

	@Before
	public void before() throws CException {
		enclosed = TestCLibNative.register();
		clib = enclosed.subject;
		fd = CLib.open("test", CLib.O_RDWR, 0666);
	}

	@After
	public void after() throws CException {
		CLib.close(fd);
		enclosed.close();
	}

	@Test
	public void shouldCaptureOpenParams() {
		var fd = fd();
		assertEquals(fd.fd(), this.fd);
		assertEquals(fd.path(), "test");
		assertEquals(fd.flags(), CLib.O_RDWR);
		assertEquals(fd.mode(), 0666);
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		clib.read.autoResponses(ByteProvider.of(1, 2, 3), null, ByteProvider.empty());
		assertArray(CLib.read(fd, 5), 1, 2, 3);
		clib.read.assertAuto(List.of(fd(), 5));
		assertArray(CLib.read(fd, 3));
		clib.read.assertAuto(List.of(fd(), 3));
		assertArray(CLib.read(fd, 2));
		clib.read.assertAuto(List.of(fd(), 2));
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		clib.write.autoResponses(2, 1);
		assertEquals(CLib.write(fd, JnaUtil.mallocBytes(1, 2, 3), 3), 2);
		clib.write.assertAuto(List.of(fd(), ByteProvider.of(1, 2, 3)));
		assertEquals(CLib.write(fd, (Pointer) null, 2), 1);
		clib.write.assertAuto(List.of(fd(), ByteProvider.of(0, 0)));
	}

	@Test
	public void shouldCallTermios() {
		// currently stubbed
		assertEquals(clib.tcsendbreak(33, 1000), 0);
		assertEquals(clib.tcdrain(33), 0);
		assertEquals(clib.tcflush(33, 1000), 0);
		assertEquals(clib.tcflow(33, 1000), 0);
		clib.cfmakeraw(null);
		assertEquals(clib.cfgetispeed(null), nlong(0));
		assertEquals(clib.cfgetospeed(null), nlong(0));
		assertEquals(clib.cfsetispeed(null, nlong(0)), 0);
		assertEquals(clib.cfsetospeed(null, nlong(0)), 0);
		assertEquals(clib.cfsetspeed(null, nlong(0)), 0);
	}

	@Test
	public void shouldFailForInvalidFd() {
		assertThrown(() -> CLib.close(-1));
	}

	private Fd fd() {
		return clib.fd(fd);
	}
}
