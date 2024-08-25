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
	public void shouldDetermineIfKnownLevel() {
		assertEquals(Level.known(null), false);
		assertEquals(Level.known(Level.unknown), false);
		assertEquals(Level.known(Level.low), true);
		assertEquals(Level.known(Level.high), true);
	}

	@Test
	public void shouldInvertLevel() {
		assertEquals(Level.invert(null), Level.unknown);
		assertEquals(Level.invert(Level.unknown), Level.unknown);
		assertEquals(Level.invert(Level.low), Level.high);
		assertEquals(Level.invert(Level.high), Level.low);
	}

	@Test
	public void shouldDetermineStateFromActiveLevel() {
		assertEquals(Level.activeState(null, null), BinaryState.unknown);
		assertEquals(Level.activeState(null, Level.unknown), BinaryState.unknown);
		assertEquals(Level.activeState(null, Level.low), BinaryState.unknown);
		assertEquals(Level.activeState(null, Level.high), BinaryState.unknown);
		assertEquals(Level.activeState(Level.unknown, null), BinaryState.unknown);
		assertEquals(Level.activeState(Level.unknown, Level.unknown), BinaryState.unknown);
		assertEquals(Level.activeState(Level.unknown, Level.low), BinaryState.unknown);
		assertEquals(Level.activeState(Level.unknown, Level.high), BinaryState.unknown);
		assertEquals(Level.activeState(Level.low, null), BinaryState.off);
		assertEquals(Level.activeState(Level.low, Level.unknown), BinaryState.off);
		assertEquals(Level.activeState(Level.low, Level.low), BinaryState.on);
		assertEquals(Level.activeState(Level.low, Level.high), BinaryState.off);
		assertEquals(Level.activeState(Level.high, null), BinaryState.on);
		assertEquals(Level.activeState(Level.high, Level.unknown), BinaryState.on);
		assertEquals(Level.activeState(Level.high, Level.low), BinaryState.off);
		assertEquals(Level.activeState(Level.high, Level.high), BinaryState.on);
	}

	@Test
	public void shouldDetermineStateFromLevel() {
		assertEquals(Level.state(null), BinaryState.unknown);
		assertEquals(Level.state(Level.unknown), BinaryState.unknown);
		assertEquals(Level.state(Level.low), BinaryState.off);
		assertEquals(Level.state(Level.high), BinaryState.on);
	}

}
