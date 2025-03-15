package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.BiFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBiFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		BiFunction<Integer, Integer, Integer> f = biFunction().asBiFunction();
		assertEquals(f.apply(2, 3), 5);
		assertRte(() -> f.apply(1, 2));
		assertRte(() -> f.apply(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionBiFunction<RuntimeException, Integer, Integer, Integer> f =
			ExceptionBiFunction.of(Std.biFunction());
		f.apply(1, 2);
		assertRte(() -> f.apply(0, 2));
	}

}
