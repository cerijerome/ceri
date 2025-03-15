package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.Function;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		Function<Integer, Integer> f = function().asFunction();
		assertEquals(f.apply(2), 2);
		assertRte(() -> f.apply(1));
		assertRte(() -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionFunction<RuntimeException, Integer, Integer> f =
			ExceptionFunction.of(Std.function());
		f.apply(1);
		assertRte(() -> f.apply(0));
	}

}
