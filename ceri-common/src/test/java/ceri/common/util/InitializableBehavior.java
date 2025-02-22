package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class InitializableBehavior {

	@Test
	public void shouldInitializeTypes() {
		int[] i = { 0 };
		Initializable.init(() -> i[0]++, () -> i[0] += 5);
		assertEquals(i[0], 6);
	}

}
