package ceri.serial.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.util.OsUtil.macInt;
import org.junit.Test;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertEquals(Seek.from(0), Seek.SEEK_SET);
		assertEquals(Seek.from(1), Seek.SEEK_CUR);
		assertEquals(Seek.from(2), Seek.SEEK_END);
		assertEquals(Seek.from(macInt(4, 3)), Seek.SEEK_DATA);
		assertEquals(Seek.from(macInt(3, 4)), Seek.SEEK_HOLE);
		assertNull(Seek.from(-1));
	}

}
