package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Closeables;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.WriteArgs;
import ceri.jna.util.JnaLibrary;

public class COutputStreamBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd;
	private COutputStream out;

	@After
	public void after() {
		Closeables.close(out, ref);
		fd = -1;
		out = null;
	}

	@Test
	public void shouldWriteSingleByte() throws IOException {
		var lib = initOut();
		lib.write.autoResponses(1);
		out.write(0xabcd);
		lib.write.assertAuto(WriteArgs.of(fd, 0xcd));
		assertEquals(lib.write.calls(), 1);
	}

	@Test
	public void shouldContinueWithIncompleteWrites() throws IOException {
		var lib = initOut();
		assertEquals(out.bufferSize(), 3);
		lib.write.autoResponses(1, 2, 1);
		out.write(ArrayUtil.bytes.of(1, 2, 3, 4, 5));
		lib.write.assertValues( //
			WriteArgs.of(fd, 1, 2, 3), // 1 byte written
			WriteArgs.of(fd, 2, 3), // 2 bytes written
			WriteArgs.of(fd, 4, 5), // 1 byte written
			WriteArgs.of(fd, 5)); // 1 byte written
	}

	@Test
	public void shouldFailForIncompleteWrite() {
		var lib = initOut();
		lib.write.autoResponses(0);
		assertThrown(() -> out.write(0xff));
		lib.write.autoResponses(2, 0);
		assertThrown(() -> out.write(new byte[3]));
	}

	@Test
	public void shouldProvideQueueSize() throws IOException {
		var lib = initOut();
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(33), 0);
		assertEquals(out.queued(), 33);
	}

	@Test
	public void shouldFlush() throws IOException {
		initOut();
		out.flush(); // no-op
	}

	@Test
	public void shouldFailIfClosed() {
		var lib = initOut();
		lib.write.autoResponses(1);
		out.close();
		assertThrown(() -> out.write(0));
		assertThrown(() -> out.write(new byte[3]));
		assertThrown(() -> out.flush());
	}

	private TestCLibNative initOut() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		out = COutputStream.of(fd);
		out.bufferSize(3);
		return lib;
	}
}
