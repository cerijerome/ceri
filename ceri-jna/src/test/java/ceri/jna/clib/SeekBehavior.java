package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertEquals(Seek.from(0), Seek.SET);
		assertEquals(Seek.from(1), Seek.CUR);
		assertEquals(Seek.from(2), Seek.END);
		assertNull(Seek.from(-1));
	}

}
