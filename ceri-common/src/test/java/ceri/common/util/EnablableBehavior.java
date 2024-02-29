package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class EnablableBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(Enablable.enabled(null));
		assertFalse(Enablable.enabled(() -> false));
		assertTrue(Enablable.enabled(() -> true));
	}

	@Test
	public void shouldProvideConditionalValue() {
		assertEquals(Enablable.conditional(null, 1, 2), 2);
		assertEquals(Enablable.conditional(() -> false, 1, 2), 2);
		assertEquals(Enablable.conditional(() -> true, 1, 2), 1);
	}

}
