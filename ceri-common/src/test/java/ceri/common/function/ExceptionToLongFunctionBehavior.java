package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toLongFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.ToLongFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToLongFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToLongFunction<Integer> f = toLongFunction().asToLongFunction();
		assertEquals(f.applyAsLong(2), 2L);
		assertRte(() -> f.applyAsLong(1));
		assertRte(() -> f.applyAsLong(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToLongFunction<RuntimeException, Integer> f =
			ExceptionToLongFunction.of(Std.toLongFunction());
		assertEquals(f.applyAsLong(1), 1L);
		assertRte(() -> f.applyAsLong(0));
	}
}
