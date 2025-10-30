package ceri.jna.util;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.test.JnaAssert;

public class GcMemoryBehavior {

	@Test
	public void shouldDetermineIfMemoryIsValid() {
		Assert.no(GcMemory.NULL.valid());
		Assert.no(GcMemory.of(null).valid());
		var gm = GcMemory.malloc(3);
		Assert.yes(gm.valid());
		gm.close();
		Assert.no(gm.valid());
		gm.close();
		Assert.no(gm.valid());
	}

	@Test
	public void shouldClearMemory() {
		var m = GcMemory.mallocBytes(-1, 0x80, 0x7f).clear();
		assertGcMemory(m, 0, 0, 0);
		m = GcMemory.mallocBytes(-1, 0x80, 0x7f).close().clear();
		JnaAssert.notValid(m.m);
	}

	@Test
	public void shoudShareMemory() {
		var m = GcMemory.mallocBytes(-1, 0x80, 0, 0x7f);
		assertGcMemory(m.share(0), -1, 0x80, 0, 0x7f);
		assertGcMemory(m.share(1), 0x80, 0, 0x7f);
		assertGcMemory(m.share(1, 2), 0x80, 0);
	}

	@Test
	public void shouldDetermineSize() {
		Assert.equal(GcMemory.NULL.size(), 0L);
		Assert.equal(GcMemory.malloc(2).size(), 2L);
	}

	@Test
	public void shouldDetermineIntSize() {
		Assert.equal(GcMemory.NULL.intSize(), 0);
		Assert.equal(GcMemory.malloc(2).intSize(), 2);
	}

	@Test
	public void shouldFreeMemoryOnClose() {
		var m = GcMemory.malloc(3);
		JnaAssert.valid(m.m);
		m.close();
		JnaAssert.notValid(m.m);
	}

	private static void assertGcMemory(GcMemory m, int... bytes) {
		JnaAssert.memory(m.m, 0, bytes);
	}
}
