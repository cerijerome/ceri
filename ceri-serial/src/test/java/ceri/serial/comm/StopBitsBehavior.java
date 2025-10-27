package ceri.serial.comm;

import org.junit.Test;
import ceri.common.test.Assert;

public class StopBitsBehavior {

	@Test
	public void shouldLookupByValue() {
		Assert.isNull(StopBits.from(0));
		Assert.equal(StopBits.from(1), StopBits._1);
		Assert.equal(StopBits.from(2), StopBits._2);
		Assert.equal(StopBits.from(3), StopBits._1_5);
	}

	@Test
	public void shouldProvideMinimumBitsUsed() {
		Assert.equal(StopBits._1.minBits(), 1);
		Assert.equal(StopBits._1_5.minBits(), 2);
		Assert.equal(StopBits._2.minBits(), 2);
	}

}
