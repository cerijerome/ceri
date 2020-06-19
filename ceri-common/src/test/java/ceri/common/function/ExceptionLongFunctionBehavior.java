package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longFunction;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.function.LongFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		LongFunction<Integer> f = longFunction().asLongFunction();
		assertThat(f.apply(2), is(2));
		assertThrown(RuntimeException.class, () -> f.apply(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionLongFunction<RuntimeException, Integer> f =
			ExceptionLongFunction.of(Std.longFunction());
		assertThat(f.apply(1), is(1));
		assertThrown(RuntimeException.class, () -> f.apply(0));
	}

}
