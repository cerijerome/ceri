package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionRunnableBehavior {

	@Test
	public void shouldConvertToRunnable() {
		runnable(2).asRunnable().run();
		assertThrown(RuntimeException.class, () -> runnable(1).asRunnable().run());
		assertThrown(RuntimeException.class, () -> runnable(0).asRunnable().run());
	}

	@Test
	public void shouldConvertFromRunnable() {
		ExceptionRunnable.of(Std.runnable(1)).run();
		assertThrown(RuntimeException.class, () -> ExceptionRunnable.of(Std.runnable(0)).run());
	}

}
