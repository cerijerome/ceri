package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.byteFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionByteFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ByteFunction<Integer> f = byteFunction().asByteFunction();
		assertEquals(f.apply((byte) 2), 2);
		assertThrown(RuntimeException.class, () -> f.apply((byte) 1));
		assertThrown(RuntimeException.class, () -> f.apply((byte) 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionByteFunction<RuntimeException, Integer> f =
			ExceptionByteFunction.of(Std.byteFunction());
		assertEquals(f.apply((byte) 1), 1);
		assertThrown(RuntimeException.class, () -> f.apply((byte) 0));
	}
}
