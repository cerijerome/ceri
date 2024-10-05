package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toDoubleFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.function.ToDoubleFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToDoubleFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToDoubleFunction<Integer> f = toDoubleFunction().asToDoubleFunction();
		assertEquals(f.applyAsDouble(2), 2.0);
		assertThrown(RuntimeException.class, () -> f.applyAsDouble(1));
		assertThrown(RuntimeException.class, () -> f.applyAsDouble(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToDoubleFunction<RuntimeException, Integer> f =
			ExceptionToDoubleFunction.of(Std.toDoubleFunction());
		assertEquals(f.applyAsDouble(1), 1.0);
		assertThrown(RuntimeException.class, () -> f.applyAsDouble(0));
	}

}
