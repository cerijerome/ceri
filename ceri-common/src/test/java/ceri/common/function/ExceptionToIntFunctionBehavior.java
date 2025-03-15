package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toIntFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.ToIntFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToIntFunction<Integer> f = toIntFunction().asToIntFunction();
		assertEquals(f.applyAsInt(2), 2);
		assertRte(() -> f.applyAsInt(1));
		assertRte(() -> f.applyAsInt(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToIntFunction<RuntimeException, Integer> f =
			ExceptionToIntFunction.of(Std.toIntFunction());
		assertEquals(f.applyAsInt(1), 1);
		assertRte(() -> f.applyAsInt(0));
	}

}
