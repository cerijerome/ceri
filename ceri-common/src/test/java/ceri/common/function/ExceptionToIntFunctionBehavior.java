package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.toIntFunction;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import java.util.function.ToIntFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionToIntFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ToIntFunction<Integer> f = toIntFunction().asToIntFunction();
		assertThat(f.applyAsInt(2), is(2));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(1));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionToIntFunction<RuntimeException, Integer> f =
			ExceptionToIntFunction.of(Std.toIntFunction());
		assertThat(f.applyAsInt(1), is(1));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(0));
	}

}
