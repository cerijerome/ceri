package ceri.jna.clib.jna;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.test.JnaTesting;

public class CErrNoBehavior {

	@Test
	public void shouldNormalizeCodes() {
		Assert.unordered(CErrNo.codes(-1, 0, 1, 2, -1, -2), 0, 1, 2, -2);
	}

	@Test
	public void testOsCoverage() {
		JnaTesting.testForEachOs(CErrNo.class);
	}
}
