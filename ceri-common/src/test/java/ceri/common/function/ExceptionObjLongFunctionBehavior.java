package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjLongFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ObjLongFunction<Integer, Long> f = FunctionTestUtil.objLongFunction().asObjLongFunction();
		assertEquals(f.apply(2, 3), 5L);
		assertRte(() -> f.apply(1, 2));
		assertRte(() -> f.apply(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionObjLongFunction<RuntimeException, Integer, Long> f =
			ExceptionObjLongFunction.of(Std.objLongFunction());
		f.apply(1, 2);
		assertRte(() -> f.apply(0, 2));
	}

}
