package ceri.jna.clib;

import org.junit.Test;
import ceri.common.test.Assert;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		Assert.equal(Seek.from(0), Seek.SET);
		Assert.equal(Seek.from(1), Seek.CUR);
		Assert.equal(Seek.from(2), Seek.END);
		Assert.isNull(Seek.from(-1));
	}

}
