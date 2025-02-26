package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class TruthBehavior {

	@Test
	public void testInvert() {
		assertEquals(Truth.invert(null), null);
		assertEquals(Truth.invert(Truth.maybe), Truth.maybe);
		assertEquals(Truth.invert(Truth.yes), Truth.no);
		assertEquals(Truth.invert(Truth.no), Truth.yes);
	}

	@Test
	public void testKnown() {
		assertEquals(Truth.known(null), false);
		assertEquals(Truth.known(Truth.maybe), false);
		assertEquals(Truth.known(Truth.yes), true);
		assertEquals(Truth.known(Truth.no), true);
	}

	@Test
	public void testBool() {
		assertEquals(Truth.bool(null), null);
		assertEquals(Truth.bool(Truth.maybe), null);
		assertEquals(Truth.bool(Truth.yes), true);
		assertEquals(Truth.bool(Truth.no), false);
	}

	@Test
	public void testOn() {
		assertEquals(Truth.maybe.yes(), false);
		assertEquals(Truth.no.yes(), false);
		assertEquals(Truth.yes.yes(), true);
	}

	@Test
	public void testno() {
		assertEquals(Truth.maybe.no(), false);
		assertEquals(Truth.no.no(), true);
		assertEquals(Truth.yes.no(), false);
	}

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(Truth.from(null), Truth.maybe);
		assertEquals(Truth.from(true), Truth.yes);
		assertEquals(Truth.from(false), Truth.no);
	}

	@Test
	public void shouldGetBoolean() {
		assertEquals(Truth.maybe.bool(), null);
		assertEquals(Truth.yes.bool(), true);
		assertEquals(Truth.no.bool(), false);
	}

	@Test
	public void shouldDetermineIfKnown() {
		assertEquals(Truth.maybe.known(), false);
		assertEquals(Truth.yes.known(), true);
		assertEquals(Truth.no.known(), true);
	}

}
