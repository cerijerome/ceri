package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.Mmap.Option;
import ceri.jna.clib.Mmap.Protection;
import ceri.jna.clib.Mmap.Visibility;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.MmapArgs;
import ceri.jna.clib.test.TestCLibNative.Presult;

public class MmapBehavior {
	private TestCLibNative lib = null;
	private Enclosed<RuntimeException, ?> enc = null;
	private Memory mem = null;
	private CFileDescriptor fd = null;
	private Mmap mmap = null;

	@After
	public void after() {
		CloseableUtil.close(mmap, fd, mem, enc);
		enc = null;
		lib = null;
		mem = null;
		fd = null;
		mmap = null;
	}

	@Test
	public void shouldCreateAnonymousMap() throws IOException {
		initLib();
		mem = new Memory(32);
		mmap = Mmap.anonymous(Visibility.SHARED, 32).address(mem).options(Option.FIXED).map();
		lib.mmap.assertAuto(new MmapArgs(mem, 32L, 0,
			Visibility.SHARED.value + Option.ANONYMOUS.value + Option.FIXED.value, -1, 0));
		assertEquals(mmap.address(0), mem);
	}

	@Test
	public void shouldCreateFileMap() throws IOException {
		initLib();
		fd = CFileDescriptor.open("test");
		mmap = Mmap.file(Visibility.PRIVATE, 32, fd, 8).protections(Protection.READ).map();
		lib.mmap.assertAuto(
			new MmapArgs(null, 32L, Protection.READ.value, Visibility.PRIVATE.value, fd.fd(), 8));
		mmap.address(0);
	}

	@Test
	public void shouldPeovideMemoryMap() throws IOException {
		initLib();
		mem = new Memory(16);
		lib.mmap.autoResponses(new Presult(mem, 0));
		mmap = Mmap.anonymous(Visibility.SHARED, 16).map();
		assertEquals(mmap.address(0), mem);
	}

	private void initLib() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
	}
}
