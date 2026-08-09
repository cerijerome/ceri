package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.test.FfmTesting;

public class CErrNoTest {

	@Test
	public void shouldNormalizeCodes() {
		Assert.unordered(CErrNo.codes(-1, 0, 1, 2, -1, -2), 0, 1, 2, -2);
	}

	@Test
	public void testOsCoverage() {
		FfmTesting.testForEachOs(CErrNo.class);
	}
}
