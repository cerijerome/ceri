package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import com.sun.jna.Memory;

public class GcMemoryBehavior {

	@Test
	public void shouldDetermineIfMemoryIsValid() {
		assertFalse(GcMemory.NULL.valid());
		@SuppressWarnings("resource")
		var gm = GcMemory.of(new Memory(3));
		assertTrue(gm.valid());
		gm.close();
		assertFalse(gm.valid());
		gm.close();
		assertFalse(gm.valid());
	}

	@Test
	public void shouldDetermineSize() {
		assertEquals(GcMemory.NULL.size(), 0L);
		@SuppressWarnings("resource")
		var gm = GcMemory.of(new Memory(3));
		assertEquals(gm.size(), 3L);
	}

}
