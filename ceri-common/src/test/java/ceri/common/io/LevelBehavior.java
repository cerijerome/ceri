package ceri.common.io;

import org.junit.Test;
import ceri.common.data.BinaryState;
import ceri.common.test.Assert;

public class LevelBehavior {

	@Test
	public void shouldDetermineLevelFromValue() {
		Assert.equal(Level.from(-2), Level.unknown);
		Assert.equal(Level.from(-1), Level.unknown);
		Assert.equal(Level.from(0), Level.low);
		Assert.equal(Level.from(1), Level.high);
		Assert.equal(Level.from(2), Level.unknown);
	}

	@Test
	public void shouldDetermineValueFromLevel() {
		Assert.equal(Level.value(null), -1);
		Assert.equal(Level.value(Level.unknown), -1);
		Assert.equal(Level.value(Level.low), 0);
		Assert.equal(Level.value(Level.high), 1);
	}

	@Test
	public void shouldDetermineIfKnownLevel() {
		Assert.equal(Level.known(null), false);
		Assert.equal(Level.known(Level.unknown), false);
		Assert.equal(Level.known(Level.low), true);
		Assert.equal(Level.known(Level.high), true);
	}

	@Test
	public void shouldInvertLevel() {
		Assert.equal(Level.invert(null), Level.unknown);
		Assert.equal(Level.invert(Level.unknown), Level.unknown);
		Assert.equal(Level.invert(Level.low), Level.high);
		Assert.equal(Level.invert(Level.high), Level.low);
	}

	@Test
	public void shouldDetermineStateFromActiveLevel() {
		Assert.equal(Level.activeState(null, null), BinaryState.unknown);
		Assert.equal(Level.activeState(null, Level.unknown), BinaryState.unknown);
		Assert.equal(Level.activeState(null, Level.low), BinaryState.unknown);
		Assert.equal(Level.activeState(null, Level.high), BinaryState.unknown);
		Assert.equal(Level.activeState(Level.unknown, null), BinaryState.unknown);
		Assert.equal(Level.activeState(Level.unknown, Level.unknown), BinaryState.unknown);
		Assert.equal(Level.activeState(Level.unknown, Level.low), BinaryState.unknown);
		Assert.equal(Level.activeState(Level.unknown, Level.high), BinaryState.unknown);
		Assert.equal(Level.activeState(Level.low, null), BinaryState.off);
		Assert.equal(Level.activeState(Level.low, Level.unknown), BinaryState.off);
		Assert.equal(Level.activeState(Level.low, Level.low), BinaryState.on);
		Assert.equal(Level.activeState(Level.low, Level.high), BinaryState.off);
		Assert.equal(Level.activeState(Level.high, null), BinaryState.on);
		Assert.equal(Level.activeState(Level.high, Level.unknown), BinaryState.on);
		Assert.equal(Level.activeState(Level.high, Level.low), BinaryState.off);
		Assert.equal(Level.activeState(Level.high, Level.high), BinaryState.on);
	}

	@Test
	public void shouldDetermineStateFromLevel() {
		Assert.equal(Level.state(null), BinaryState.unknown);
		Assert.equal(Level.state(Level.unknown), BinaryState.unknown);
		Assert.equal(Level.state(Level.low), BinaryState.off);
		Assert.equal(Level.state(Level.high), BinaryState.on);
	}

}
