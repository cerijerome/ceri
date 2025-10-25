package ceri.serial.comm;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class StopBitsBehavior {

	@Test
	public void shouldLookupByValue() {
		Assert.isNull(StopBits.from(0));
		assertEquals(StopBits.from(1), StopBits._1);
		assertEquals(StopBits.from(2), StopBits._2);
		assertEquals(StopBits.from(3), StopBits._1_5);
	}

	@Test
	public void shouldProvideMinimumBitsUsed() {
		assertEquals(StopBits._1.minBits(), 1);
		assertEquals(StopBits._1_5.minBits(), 2);
		assertEquals(StopBits._2.minBits(), 2);
	}

}
