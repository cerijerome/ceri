package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intFunction;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.function.IntFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		IntFunction<Integer> f = intFunction().asIntFunction();
		assertThat(f.apply(2), is(2));
		assertThrown(RuntimeException.class, () -> f.apply(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionIntFunction<RuntimeException, Integer> f =
			ExceptionIntFunction.of(Std.intFunction());
		assertThat(f.apply(1), is(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

}
