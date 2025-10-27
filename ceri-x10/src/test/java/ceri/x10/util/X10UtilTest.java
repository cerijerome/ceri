package ceri.x10.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class X10UtilTest {

	@Test
	public void testFromNybble() {
		Assert.equal(X10Util.fromNybble(0xa956, 0), 0x6);
		Assert.equal(X10Util.fromNybble(0xa956, 1), 0x5);
		Assert.equal(X10Util.fromNybble(0xa956, 2), 0x9);
		Assert.equal(X10Util.fromNybble(0xa956, 3), 0xa);
	}

	@Test
	public void testToNybble() {
		Assert.equal(X10Util.toNybble(0xa956, 0), 0x6);
		Assert.equal(X10Util.toNybble(0xa95, 1), 0x50);
		Assert.equal(X10Util.toNybble(0xa9, 2), 0x900);
		Assert.equal(X10Util.toNybble(0xa, 3), 0xa000);
	}
}
