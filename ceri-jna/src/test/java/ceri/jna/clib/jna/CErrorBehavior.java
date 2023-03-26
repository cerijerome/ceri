package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class CErrorBehavior {

	@Test
	public void shouldDetermineIfUndefined() {
		for (CError error : CError.values()) {
			assertEquals(error.undefined(), error.code <= 0, error.name());
		}
	}

}
