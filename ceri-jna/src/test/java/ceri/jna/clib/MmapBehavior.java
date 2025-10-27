package ceri.jna.clib;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.Mmap.Option;
import ceri.jna.clib.Mmap.Protection;
import ceri.jna.clib.Mmap.Visibility;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.MmapArgs;
import ceri.jna.clib.test.TestCLibNative.Presult;
import ceri.jna.util.JnaLibrary;

public class MmapBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private Memory mem = null;
	private CFileDescriptor fd = null;
	private Mmap mmap = null;

	@After
	public void after() {
		Closeables.close(mmap, fd, mem, ref);
		mem = null;
		fd = null;
		mmap = null;
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		ref.init();
		mem = new Memory(32);
		mmap = Mmap.anonymous(Visibility.SHARED, 32).address(mem).options(Option.FIXED).map();
		Assert.find(mmap, "@\\w+\\+20");
	}
	
	@Test
	public void shouldCalculateLength() throws IOException {
		var lib = ref.init();
		lib.pagesize.autoResponses(0x1000);
		Assert.equal(Mmap.length(0x100), 0x1000L);
		Assert.equal(Mmap.length(0x1234), 0x2000L);
	}

	@Test
	public void shouldCreateAnonymousMap() throws IOException {
		var lib = ref.init();
		mem = new Memory(32);
		mmap = Mmap.anonymous(Visibility.SHARED, 32).address(mem).options(Option.FIXED).map();
		lib.mmap.assertAuto(new MmapArgs(mem, 32L, 0,
			Visibility.SHARED.value + Option.ANONYMOUS.value + Option.FIXED.value, -1, 0));
		Assert.equal(mmap.address(0), mem);
	}

	@Test
	public void shouldCreateFileMap() throws IOException {
		var lib = ref.init();
		fd = CFileDescriptor.open("test");
		mmap = Mmap.file(Visibility.PRIVATE, 32, fd, 8).protections(Protection.READ).map();
		lib.mmap.assertAuto(
			new MmapArgs(null, 32L, Protection.READ.value, Visibility.PRIVATE.value, fd.fd(), 8));
		mmap.address(0);
	}

	@Test
	public void shouldProvideMemoryMap() throws IOException {
		var lib = ref.init();
		mem = new Memory(16);
		lib.mmap.autoResponses(new Presult(mem, 0));
		mmap = Mmap.anonymous(Visibility.SHARED, 16).map();
		Assert.equal(mmap.address(0), mem);
	}
}
