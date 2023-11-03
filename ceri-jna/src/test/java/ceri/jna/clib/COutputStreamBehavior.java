package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.WriteArgs;

public class COutputStreamBehavior {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private static int fd;
	private COutputStream out;

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
		lib.write.reset();
		out = COutputStream.of(fd);
		out.bufferSize(3);
	}

	@After
	public void after() {
		out.close();
	}

	@Test
	public void shouldWriteSingleByte() throws IOException {
		lib.write.autoResponses(1);
		out.write(0xabcd);
		lib.write.assertAuto(WriteArgs.of(fd, 0xcd));
		assertEquals(lib.write.calls(), 1);
	}

	@Test
	public void shouldContinueWithIncompleteWrites() throws IOException {
		assertEquals(out.bufferSize(), 3);
		lib.write.autoResponses(1, 2, 1);
		out.write(ArrayUtil.bytes(1, 2, 3, 4, 5));
		lib.write.assertValues( //
			WriteArgs.of(fd, 1, 2, 3), // 1 byte written
			WriteArgs.of(fd, 2, 3), // 2 bytes written
			WriteArgs.of(fd, 4, 5), // 1 byte written
			WriteArgs.of(fd, 5)); // 1 byte written
	}

	@Test
	public void shouldFailForIncompleteWrite() {
		lib.write.autoResponses(0);
		assertThrown(() -> out.write(0xff));
		lib.write.autoResponses(2, 0);
		assertThrown(() -> out.write(new byte[3]));
	}

	@Test
	public void shouldProvideQueueSize() throws IOException {
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(33), 0);
		assertEquals(out.queued(), 33);
	}

	@Test
	public void shouldFlush() throws IOException {
		out.flush(); // no-op
	}

	@Test
	public void shouldFailIfClosed() {
		lib.write.autoResponses(1);
		out.close();
		assertThrown(() -> out.write(0));
		assertThrown(() -> out.write(new byte[3]));
		assertThrown(() -> out.flush());
	}
}
