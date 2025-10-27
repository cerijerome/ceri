package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class BinaryStateBehavior {

	@Test
	public void testInvert() {
		Assert.equal(BinaryState.invert(null), null);
		Assert.equal(BinaryState.invert(BinaryState.unknown), BinaryState.unknown);
		Assert.equal(BinaryState.invert(BinaryState.on), BinaryState.off);
		Assert.equal(BinaryState.invert(BinaryState.off), BinaryState.on);
	}

	@Test
	public void testKnown() {
		Assert.equal(BinaryState.known(null), false);
		Assert.equal(BinaryState.known(BinaryState.unknown), false);
		Assert.equal(BinaryState.known(BinaryState.on), true);
		Assert.equal(BinaryState.known(BinaryState.off), true);
	}

	@Test
	public void testBool() {
		Assert.equal(BinaryState.bool(null), null);
		Assert.equal(BinaryState.bool(BinaryState.unknown), null);
		Assert.equal(BinaryState.bool(BinaryState.on), true);
		Assert.equal(BinaryState.bool(BinaryState.off), false);
	}

	@Test
	public void testOn() {
		Assert.equal(BinaryState.unknown.on(), false);
		Assert.equal(BinaryState.off.on(), false);
		Assert.equal(BinaryState.on.on(), true);
	}

	@Test
	public void testOff() {
		Assert.equal(BinaryState.unknown.off(), false);
		Assert.equal(BinaryState.off.off(), true);
		Assert.equal(BinaryState.on.off(), false);
	}

	@Test
	public void shouldGetFromBoolean() {
		Assert.equal(BinaryState.from(null), BinaryState.unknown);
		Assert.equal(BinaryState.from(true), BinaryState.on);
		Assert.equal(BinaryState.from(false), BinaryState.off);
	}

	@Test
	public void shouldGetBoolean() {
		Assert.equal(BinaryState.unknown.bool(), null);
		Assert.equal(BinaryState.on.bool(), true);
		Assert.equal(BinaryState.off.bool(), false);
	}

	@Test
	public void shouldDetermineIfKnown() {
		Assert.equal(BinaryState.unknown.known(), false);
		Assert.equal(BinaryState.on.known(), true);
		Assert.equal(BinaryState.off.known(), true);
	}

}
