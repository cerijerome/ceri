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

	@Test
	public void shouldGetBoolean() {
		assertEquals(BinaryState.unknown.bool(), null);
		assertEquals(BinaryState.on.bool(), true);
		assertEquals(BinaryState.off.bool(), false);
	}

	@Test
	public void shouldDetermineIfKnown() {
		assertEquals(BinaryState.unknown.known(), false);
		assertEquals(BinaryState.on.known(), true);
		assertEquals(BinaryState.off.known(), true);
	}

}
