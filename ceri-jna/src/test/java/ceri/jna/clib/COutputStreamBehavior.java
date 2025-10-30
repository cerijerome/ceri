package ceri.jna.clib;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.array.Array;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
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
		Assert.equal(lib.write.calls(), 1);
	}

	@Test
	public void shouldContinueWithIncompleteWrites() throws IOException {
		var lib = initOut();
		Assert.equal(out.bufferSize(), 3);
		lib.write.autoResponses(1, 2, 1);
		out.write(Array.bytes.of(1, 2, 3, 4, 5));
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
		Assert.thrown(() -> out.write(0xff));
		lib.write.autoResponses(2, 0);
		Assert.thrown(() -> out.write(new byte[3]));
	}

	@Test
	public void shouldProvideQueueSize() throws IOException {
		var lib = initOut();
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(33), 0);
		Assert.equal(out.queued(), 33);
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
		Assert.thrown(() -> out.write(0));
		Assert.thrown(() -> out.write(new byte[3]));
		Assert.thrown(() -> out.flush());
	}

	private TestCLibNative initOut() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		out = COutputStream.of(fd);
		out.bufferSize(3);
		return lib;
	}
}
