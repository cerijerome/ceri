package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ByteUtilTest {

	@Test
	public void testBit() {
		assertThat(ByteUtil.bit(0, 0), is(false));
		assertThat(ByteUtil.bit(0, 31), is(false));
		assertThat(ByteUtil.bit(Integer.MIN_VALUE, 31), is(true));
		assertThat(ByteUtil.bit(Integer.MAX_VALUE, 31), is(false));
		for (int i = 0; i < 31; i++)
			assertThat(ByteUtil.bit(Integer.MAX_VALUE, i), is(true));
		assertThat(ByteUtil.bit(0x5a, 0), is(false));
		assertThat(ByteUtil.bit(0x5a, 1), is(true));
		assertThat(ByteUtil.bit(0x5a, 2), is(false));
		assertThat(ByteUtil.bit(0x5a, 3), is(true));
		assertThat(ByteUtil.bit(0x5a, 4), is(true));
		assertThat(ByteUtil.bit(0x5a, 5), is(false));
		assertThat(ByteUtil.bit(0x5a, 6), is(true));
		assertThat(ByteUtil.bit(0x5a, 7), is(false));
	}

}
