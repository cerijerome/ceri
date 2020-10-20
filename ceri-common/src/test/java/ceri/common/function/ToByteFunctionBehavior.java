package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.data.ByteUtil;

public class ToByteFunctionBehavior {

	@Test
	public void testToUint() {
		ToByteFunction<String> fn = s -> ByteUtil.toAscii(s).getByte(0);
		assertEquals(fn.applyAsByte("\u00ff"), (byte) -1);
		assertEquals(ToByteFunction.toUint(fn).applyAsInt("\u00ff"), 0xff);
	}

}
