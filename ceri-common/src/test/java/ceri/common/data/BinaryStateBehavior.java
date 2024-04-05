package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class BinaryStateBehavior {

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(BinaryState.from(null), BinaryState.unknown);
		assertEquals(BinaryState.from(true), BinaryState.on);
		assertEquals(BinaryState.from(false), BinaryState.off);
	}
	
}
