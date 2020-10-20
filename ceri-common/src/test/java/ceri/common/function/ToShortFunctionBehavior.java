package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ToShortFunctionBehavior {

	@Test
	public void testToUint() {
		ToShortFunction<String> fn = s -> (short) s.charAt(0);
		assertEquals(fn.applyAsShort("\uffff"), (short) -1);
		assertEquals(ToShortFunction.toUint(fn).applyAsInt("\uffff"), 0xffff);
	}

}
