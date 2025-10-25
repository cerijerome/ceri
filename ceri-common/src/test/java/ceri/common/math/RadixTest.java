package ceri.common.math;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertString;
import org.junit.Test;

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
		assertEquals(Radix.Prefix.NULL.isValid(), false);
		assertEquals(new Radix.Prefix(null, "x").isValid(), false);
		assertEquals(new Radix.Prefix(Radix.NULL, "x").isValid(), false);
		assertEquals(new Radix.Prefix(Radix.BIN, "").isValid(), true);
	}

	@Test
	public void testFrom() {
		assertEquals(Radix.from(null), Radix.NULL);
		assertEquals(Radix.from(""), Radix.DEC);
		assertEquals(Radix.from("0b"), Radix.BIN);
		assertEquals(Radix.from("0"), Radix.OCT);
		assertEquals(Radix.from("0x"), Radix.HEX);
		assertEquals(Radix.from("#"), Radix.HEX);
	}

	private static void assertPrefix(Radix.Prefix prefix, int radix, String pre) {
		assertEquals(prefix.radix().n, radix);
		assertString(prefix.prefix(), pre);
	}
}
