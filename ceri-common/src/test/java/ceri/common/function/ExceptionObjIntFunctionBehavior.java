package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntFunction;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ObjIntFunction<Integer, Integer> f = objIntFunction().asObjIntFunction();
		assertThat(f.apply(2, 3), is(5));
		assertThrown(RuntimeException.class, () -> f.apply(1, 2));
		assertThrown(RuntimeException.class, () -> f.apply(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionObjIntFunction<RuntimeException, Integer, Integer> f =
			ExceptionObjIntFunction.of(Std.objIntFunction());
		f.apply(1, 2);
		assertThrown(RuntimeException.class, () -> f.apply(0, 2));
	}

}
