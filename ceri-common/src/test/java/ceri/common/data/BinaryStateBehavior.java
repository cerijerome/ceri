package ceri.common.data;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class BinaryStateBehavior {

	@Test
	public void testInvert() {
		assertEquals(BinaryState.invert(null), null);
		assertEquals(BinaryState.invert(BinaryState.unknown), BinaryState.unknown);
		assertEquals(BinaryState.invert(BinaryState.on), BinaryState.off);
		assertEquals(BinaryState.invert(BinaryState.off), BinaryState.on);
	}

	@Test
	public void testKnown() {
		assertEquals(BinaryState.known(null), false);
		assertEquals(BinaryState.known(BinaryState.unknown), false);
		assertEquals(BinaryState.known(BinaryState.on), true);
		assertEquals(BinaryState.known(BinaryState.off), true);
	}

	@Test
	public void testBool() {
		assertEquals(BinaryState.bool(null), null);
		assertEquals(BinaryState.bool(BinaryState.unknown), null);
		assertEquals(BinaryState.bool(BinaryState.on), true);
		assertEquals(BinaryState.bool(BinaryState.off), false);
	}

	@Test
	public void testOn() {
		assertEquals(BinaryState.unknown.on(), false);
		assertEquals(BinaryState.off.on(), false);
		assertEquals(BinaryState.on.on(), true);
	}

	@Test
	public void testOff() {
		assertEquals(BinaryState.unknown.off(), false);
		assertEquals(BinaryState.off.off(), true);
		assertEquals(BinaryState.on.off(), false);
	}

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
