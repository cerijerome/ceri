package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.function.IntFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		IntFunction<Integer> f = intFunction().asIntFunction();
		assertEquals(f.apply(2), 2);
		assertThrown(RuntimeException.class, () -> f.apply(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionIntFunction<RuntimeException, Integer> f =
			ExceptionIntFunction.of(Std.intFunction());
		assertEquals(f.apply(1), 1);
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

}
