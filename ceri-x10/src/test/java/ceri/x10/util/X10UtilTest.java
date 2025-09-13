package ceri.x10.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class X10UtilTest {

	@Test
	public void testFromNybble() {
		assertEquals(X10Util.fromNybble(0xa956, 0), 0x6);
		assertEquals(X10Util.fromNybble(0xa956, 1), 0x5);
		assertEquals(X10Util.fromNybble(0xa956, 2), 0x9);
		assertEquals(X10Util.fromNybble(0xa956, 3), 0xa);
	}

	@Test
	public void testToNybble() {
		assertEquals(X10Util.toNybble(0xa956, 0), 0x6);
		assertEquals(X10Util.toNybble(0xa95, 1), 0x50);
		assertEquals(X10Util.toNybble(0xa9, 2), 0x900);
		assertEquals(X10Util.toNybble(0xa, 3), 0xa000);
	}
}
