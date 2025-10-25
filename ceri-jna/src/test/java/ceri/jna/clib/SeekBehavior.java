package ceri.jna.clib;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertEquals(Seek.from(0), Seek.SET);
		assertEquals(Seek.from(1), Seek.CUR);
		assertEquals(Seek.from(2), Seek.END);
		Assert.isNull(Seek.from(-1));
	}

}
