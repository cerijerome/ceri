package ceri.serial.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertEquals(Seek.from(0), Seek.SEEK_SET);
		assertEquals(Seek.from(1), Seek.SEEK_CUR);
		assertEquals(Seek.from(2), Seek.SEEK_END);
		assertNull(Seek.from(-1));
	}

}
