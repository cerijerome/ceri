package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntFunction;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ObjIntFunction<Integer, Integer> f = objIntFunction().asObjIntFunction();
		assertEquals(f.apply(2, 3), 5);
		assertRte(() -> f.apply(1, 2));
		assertRte(() -> f.apply(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionObjIntFunction<RuntimeException, Integer, Integer> f =
			ExceptionObjIntFunction.of(Std.objIntFunction());
		f.apply(1, 2);
		assertRte(() -> f.apply(0, 2));
	}

}
