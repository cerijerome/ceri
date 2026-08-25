package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.clib.test.TestCLibNative.Mmap;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Memory;

public class CMmanTest {
	private final FfmTesting.Lib<TestCLibNative> testLib = TestCLibNative.lib();
	private final MemorySegment MEM = Memory.auto().allocate(8);
	private TestCLibNative lib;
	private Arena arena;
	private int fd = -1;

	@After
	public void after() {
		if (fd != -1) CUnistd.closeSilently(fd);
		Closeables.close(testLib, arena);
		lib = null;
		arena = null;
		fd = -1;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CMman.class);
	}

	@Test
	public void testMmap() throws CException {
		init();
		var mapped = CMman.mmap(MEM, 8, 1, 2, 3, 4);
		Assert.equal(mapped.byteSize(), 8L);
		Assert.equal(lib.mmap.lastValue(), new Mmap(MEM, 8, 1, 2, 3, 4, true));
		lib.mmap.autoResponses(Result.of(MEM));
		Assert.equal(CMman.mmap(MEM, 16, 0, 0, 0, 0), MEM);
		Assert.equal(lib.mmap.lastValue(), new Mmap(MEM, 16, 0, 0, 0, 0, true));
	}

	@Test
	public void testMmapErrors() {
		init();
		FfmAssert.cexception(() -> CMman.mmap(null, 8, 0, 0, 0, 0));
		FfmAssert.cexception(() -> CMman.mmap(MemorySegment.NULL, 8, 0, 0, 0, 0));
		FfmAssert.cexception(() -> CMman.mmap(MEM, 0, 0, 0, 0, 0));
		lib.mmap.autoResponses(Result.errno(CErrNo.EACCES));
		FfmAssert.cexception(() -> CMman.mmap(MEM, 8, 0, 0, 0, 0));
	}

	@Test
	public void testMunmap() throws CException {
		init();
		CMman.munmap(MEM, 32);
		Assert.equal(lib.mmap.lastValue(), new Mmap(MEM, 32, 0, 0, 0, 0, false));
	}

	@Test
	public void testMunmapErrors() {
		init();
		FfmAssert.cexception(() -> CMman.munmap(null, 8));
		FfmAssert.cexception(() -> CMman.munmap(MemorySegment.NULL, 8));
		FfmAssert.cexception(() -> CMman.munmap(MEM, 0));
		lib.mmap.autoResponses(Result.errno(CErrNo.EACCES));
		FfmAssert.cexception(() -> CMman.munmap(MEM, 8));
	}

	@Test
	public void testFields() throws Exception {
		FfmTesting.testForEachOs(CMman.class);
	}

	private TestCLibNative init() {
		lib = testLib.init();
		return lib;
	}
}
