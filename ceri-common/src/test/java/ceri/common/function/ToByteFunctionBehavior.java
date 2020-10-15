package ceri.common.function;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.data.ByteUtil;

public class ToByteFunctionBehavior {

	@Test
	public void testToUint() {
		ToByteFunction<String> fn = s -> ByteUtil.toAscii(s).getByte(0);
		assertThat(fn.applyAsByte("\u00ff"), is((byte) -1));
		assertThat(ToByteFunction.toUint(fn).applyAsInt("\u00ff"), is(0xff));
	}

}
