package ceri.jna.util;

import static ceri.jna.test.JnaTestUtil.assertNotValid;
import static ceri.jna.test.JnaTestUtil.assertValid;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.test.JnaTestUtil;

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
		assertNotValid(m.m);
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
		assertValid(m.m);
		m.close();
		assertNotValid(m.m);
	}

	private static void assertGcMemory(GcMemory m, int... bytes) {
		JnaTestUtil.assertMemory(m.m, 0, bytes);
	}
}
