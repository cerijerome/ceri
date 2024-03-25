package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertCollection;
import org.junit.Test;
import ceri.jna.test.JnaTestUtil;

public class CErrNoBehavior {

	@Test
	public void shouldNormalizeCodes() {
		assertCollection(CErrNo.codes(-1, 0, 1, 2, -1, -2), 0, 1, 2, -2);
	}

	@Test
	public void testOsCoverage() {
		JnaTestUtil.testForEachOs(CErrNo.class);
	}

}
