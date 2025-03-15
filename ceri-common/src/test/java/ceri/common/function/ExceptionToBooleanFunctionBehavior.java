package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toBooleanFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToBooleanFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToBooleanFunction<Integer> f = toBooleanFunction().asToBooleanFunction();
		assertEquals(f.applyAsBoolean(3), true);
		assertEquals(f.applyAsBoolean(-1), false);
		assertRte(() -> f.applyAsBoolean(1));
		assertRte(() -> f.applyAsBoolean(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToBooleanFunction<RuntimeException, Integer> f =
			ExceptionToBooleanFunction.of(Std.toBooleanFunction());
		assertEquals(f.applyAsBoolean(1), true);
		assertEquals(f.applyAsBoolean(-1), false);
		assertRte(() -> f.applyAsBoolean(0));
	}

}
