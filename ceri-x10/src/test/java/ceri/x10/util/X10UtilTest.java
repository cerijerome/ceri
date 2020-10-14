package ceri.x10.util;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class X10UtilTest {

	@Test
	public void testFromNybble() {
		assertThat(X10Util.fromNybble(0xa956, 0), is(0x6));
		assertThat(X10Util.fromNybble(0xa956, 1), is(0x5));
		assertThat(X10Util.fromNybble(0xa956, 2), is(0x9));
		assertThat(X10Util.fromNybble(0xa956, 3), is(0xa));
	}

	@Test
	public void testToNybble() {
		assertThat(X10Util.toNybble(0xa956, 0), is(0x6));
		assertThat(X10Util.toNybble(0xa95, 1), is(0x50));
		assertThat(X10Util.toNybble(0xa9, 2), is(0x900));
		assertThat(X10Util.toNybble(0xa, 3), is(0xa000));
	}

}
