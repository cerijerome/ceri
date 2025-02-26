package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toByteFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToByteFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToByteFunction<Integer> f = toByteFunction().asToByteFunction();
		assertEquals(f.applyAsByte(2), (byte) 2);
		assertThrown(RuntimeException.class, () -> f.applyAsByte(1));
		assertThrown(RuntimeException.class, () -> f.applyAsByte(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToByteFunction<RuntimeException, Integer> f =
			ExceptionToByteFunction.of(Std.toByteFunction());
		assertEquals(f.applyAsByte(1), (byte) 1);
		assertThrown(RuntimeException.class, () -> f.applyAsByte(0));
	}
}
