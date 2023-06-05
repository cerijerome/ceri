package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;
import ceri.jna.test.JnaTestUtil;

public class CErrorBehavior {

	@Test
	public void shouldDetermineIfDefined() {
		for (CError error : CError.values()) {
			assertEquals(error.defined(), error.code >= 0, error.name());
		}
	}

	@Test
	public void shouldLookupByCode() {
		assertNull(CError.from(-1));
		assertEquals(CError.from(CError.EAGAIN.code), CError.EAGAIN);
	}

	@Test
	public void testOsCoverage() {
		JnaTestUtil.testForEachOs(CError.class, CError.Os.class);
	}

}
