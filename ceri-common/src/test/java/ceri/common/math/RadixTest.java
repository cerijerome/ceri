package ceri.common.math;

import org.junit.Test;
import ceri.common.test.Assert;

public class RadixTest {

	@Test
	public void testPrefixFind() {
		assertPrefix(Radix.Prefix.find(null), 0, "");
		assertPrefix(Radix.Prefix.find(""), 0, "");
		assertPrefix(Radix.Prefix.find("123"), 0, "");
		assertPrefix(Radix.Prefix.find("0123"), 8, "0");
		assertPrefix(Radix.Prefix.find("0x123"), 16, "0x");
	}

	@Test
	public void testPrefixIsValid() {
		Assert.equal(Radix.Prefix.NULL.isValid(), false);
		Assert.equal(new Radix.Prefix(null, "x").isValid(), false);
		Assert.equal(new Radix.Prefix(Radix.NULL, "x").isValid(), false);
		Assert.equal(new Radix.Prefix(Radix.BIN, "").isValid(), true);
	}

	@Test
	public void testFrom() {
		Assert.equal(Radix.from(null), Radix.NULL);
		Assert.equal(Radix.from(""), Radix.DEC);
		Assert.equal(Radix.from("0b"), Radix.BIN);
		Assert.equal(Radix.from("0"), Radix.OCT);
		Assert.equal(Radix.from("0x"), Radix.HEX);
		Assert.equal(Radix.from("#"), Radix.HEX);
	}

	private static void assertPrefix(Radix.Prefix prefix, int radix, String pre) {
		Assert.equal(prefix.radix().n, radix);
		Assert.string(prefix.prefix(), pre);
	}
}
