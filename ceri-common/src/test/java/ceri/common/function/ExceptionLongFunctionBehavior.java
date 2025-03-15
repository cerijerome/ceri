package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.LongFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		LongFunction<Integer> f = longFunction().asLongFunction();
		assertEquals(f.apply(2), 2);
		assertRte(() -> f.apply(1));
		assertRte(() -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionLongFunction<RuntimeException, Integer> f =
			ExceptionLongFunction.of(Std.longFunction());
		assertEquals(f.apply(1), 1);
		assertRte(() -> f.apply(0));
	}

}
