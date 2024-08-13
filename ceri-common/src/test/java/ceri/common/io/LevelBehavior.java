package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.data.BinaryState;

public class LevelBehavior {

	@Test
	public void shouldDetermineLevelFromValue() {
		assertEquals(Level.from(-2), Level.unknown);
		assertEquals(Level.from(-1), Level.unknown);
		assertEquals(Level.from(0), Level.low);
		assertEquals(Level.from(1), Level.high);
		assertEquals(Level.from(2), Level.unknown);
	}

	@Test
	public void shouldDetermineValueFromLevel() {
		assertEquals(Level.value(null), -1);
		assertEquals(Level.value(Level.unknown), -1);
		assertEquals(Level.value(Level.low), 0);
		assertEquals(Level.value(Level.high), 1);
	}

	@Test
	public void shouldDetermineStateFromLevel() {
		assertEquals(Level.state(null), BinaryState.unknown);
		assertEquals(Level.state(Level.unknown), BinaryState.unknown);
		assertEquals(Level.state(Level.low), BinaryState.off);
		assertEquals(Level.state(Level.high), BinaryState.on);
	}

}
