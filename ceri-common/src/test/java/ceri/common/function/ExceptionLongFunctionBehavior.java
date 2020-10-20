package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.function.LongFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		LongFunction<Integer> f = longFunction().asLongFunction();
		assertEquals(f.apply(2), 2);
		assertThrown(RuntimeException.class, () -> f.apply(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionLongFunction<RuntimeException, Integer> f =
			ExceptionLongFunction.of(Std.longFunction());
		assertEquals(f.apply(1), 1);
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

}
