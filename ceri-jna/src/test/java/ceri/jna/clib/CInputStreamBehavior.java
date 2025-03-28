package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.data.ByteProvider;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.ReadArgs;
import ceri.jna.util.JnaLibrary;

public class CInputStreamBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd;
	private CInputStream in;

	@After
	public void after() {
		CloseableUtil.close(in, ref);
		fd = -1;
		in = null;
	}

	@Test
	public void shouldProvideAvailableBytes() throws IOException {
		var lib = initIn();
		lib.ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(33), 0);
		assertEquals(in.available(), 33);
	}

	@Test
	public void shouldReadSingleByte() throws IOException {
		var lib = initIn();
		lib.read.autoResponses(ByteProvider.of(33));
		assertEquals(in.read(), 33);
		lib.read.assertAuto(new ReadArgs(fd, 1));
	}

	@Test
	public void shouldNotReadWithZeroLength() throws IOException {
		var lib = initIn();
		lib.read.autoResponses(ByteProvider.of(1, 2, 3));
		assertEquals(in.read(new byte[0]), 0);
		lib.read.assertCalls(0);
	}

	@Test
	public void shouldReadWithBuffer() throws IOException {
		var lib = initIn();
		assertEquals(in.bufferSize(), 3);
		lib.read.autoResponses(ByteProvider.of(1, 2, 3, 4));
		var b = new byte[5];
		assertEquals(in.read(b), 3);
		assertArray(b, 1, 2, 3, 0, 0);
		lib.read.assertAuto(new ReadArgs(fd, 3));
	}

	@Test
	public void shouldReturnEofWithEmptyRead() throws IOException {
		var lib = initIn();
		lib.read.autoResponses(ByteProvider.empty());
		assertEquals(in.read(), -1);
		assertEquals(in.read(new byte[3]), -1);
	}

	@Test
	public void shouldFailIfClosed() {
		var lib = initIn();
		lib.read.autoResponses(ByteProvider.of(0));
		in.close();
		assertThrown(() -> in.read());
		assertThrown(() -> in.read(new byte[3]));
		assertThrown(() -> in.available());
	}
	
	private TestCLibNative initIn() {
		var lib = ref.init();
		fd = lib.open("test", 0);
		in = CInputStream.of(fd);
		in.bufferSize(3);
		return lib;
	}
}
