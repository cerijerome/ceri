package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.ReadArgs;

public class CInputStreamBehavior {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private static int fd;
	private CInputStream in;

	@BeforeClass
	public static void beforeClass() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = lib.open("test", 0);
	}

	@AfterClass
	public static void afterClass() {
		lib.close(fd);
		enc.close();
	}

	@Before
	public void before() {
		lib.read.reset();
		in = CInputStream.of(fd);
		in.bufferSize(3);
	}

	@After
	public void after() {
		in.close();
	}

	@Test
	public void shouldProvideAvailableBytes() throws IOException {
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(33), 0);
		assertEquals(in.available(), 33);
	}

	@Test
	public void shouldReadSingleByte() throws IOException {
		lib.read.autoResponses(ByteProvider.of(33));
		assertEquals(in.read(), 33);
		lib.read.assertAuto(new ReadArgs(fd, 1));
	}

	@Test
	public void shouldNotReadWithZeroLength() throws IOException {
		lib.read.autoResponses(ByteProvider.of(1, 2, 3));
		assertEquals(in.read(new byte[0]), 0);
		lib.read.assertCalls(0);
	}

	@Test
	public void shouldReadWithBuffer() throws IOException {
		assertEquals(in.bufferSize(), 3);
		lib.read.autoResponses(ByteProvider.of(1, 2, 3, 4));
		var b = new byte[5];
		assertEquals(in.read(b), 3);
		assertArray(b, 1, 2, 3, 0, 0);
		lib.read.assertAuto(new ReadArgs(fd, 3));
	}

	@Test
	public void shouldReturnEofWithEmptyRead() throws IOException {
		lib.read.autoResponses(ByteProvider.empty());
		assertEquals(in.read(), -1);
		assertEquals(in.read(new byte[3]), -1);
	}

	@Test
	public void shouldFailIfClosed() {
		lib.read.autoResponses(ByteProvider.of(0));
		in.close();
		assertThrown(() -> in.read());
		assertThrown(() -> in.read(new byte[3]));
		assertThrown(() -> in.available());
	}
}
