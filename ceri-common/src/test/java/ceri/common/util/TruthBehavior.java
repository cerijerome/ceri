package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class TruthBehavior {

	@Test
	public void testInvert() {
		Assert.equal(Truth.invert(null), null);
		Assert.equal(Truth.invert(Truth.maybe), Truth.maybe);
		Assert.equal(Truth.invert(Truth.yes), Truth.no);
		Assert.equal(Truth.invert(Truth.no), Truth.yes);
	}

	@Test
	public void testKnown() {
		Assert.equal(Truth.known(null), false);
		Assert.equal(Truth.known(Truth.maybe), false);
		Assert.equal(Truth.known(Truth.yes), true);
		Assert.equal(Truth.known(Truth.no), true);
	}

	@Test
	public void testBool() {
		Assert.equal(Truth.bool(null), null);
		Assert.equal(Truth.bool(Truth.maybe), null);
		Assert.equal(Truth.bool(Truth.yes), true);
		Assert.equal(Truth.bool(Truth.no), false);
	}

	@Test
	public void testOn() {
		Assert.equal(Truth.maybe.yes(), false);
		Assert.equal(Truth.no.yes(), false);
		Assert.equal(Truth.yes.yes(), true);
	}

	@Test
	public void testno() {
		Assert.equal(Truth.maybe.no(), false);
		Assert.equal(Truth.no.no(), true);
		Assert.equal(Truth.yes.no(), false);
	}

	@Test
	public void shouldGetFromBoolean() {
		Assert.equal(Truth.from(null), Truth.maybe);
		Assert.equal(Truth.from(true), Truth.yes);
		Assert.equal(Truth.from(false), Truth.no);
	}

	@Test
	public void shouldGetBoolean() {
		Assert.equal(Truth.maybe.bool(), null);
		Assert.equal(Truth.yes.bool(), true);
		Assert.equal(Truth.no.bool(), false);
	}

	@Test
	public void shouldDetermineIfKnown() {
		Assert.equal(Truth.maybe.known(), false);
		Assert.equal(Truth.yes.known(), true);
		Assert.equal(Truth.no.known(), true);
	}

}
