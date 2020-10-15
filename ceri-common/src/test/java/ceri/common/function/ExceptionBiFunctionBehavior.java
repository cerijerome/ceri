package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biFunction;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import java.util.function.BiFunction;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBiFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		BiFunction<Integer, Integer, Integer> f = biFunction().asBiFunction();
		assertThat(f.apply(2, 3), is(5));
		assertThrown(RuntimeException.class, () -> f.apply(1, 2));
		assertThrown(RuntimeException.class, () -> f.apply(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionBiFunction<RuntimeException, Integer, Integer, Integer> f =
			ExceptionBiFunction.of(Std.biFunction());
		f.apply(1, 2);
		assertThrown(RuntimeException.class, () -> f.apply(0, 2));
	}

}
