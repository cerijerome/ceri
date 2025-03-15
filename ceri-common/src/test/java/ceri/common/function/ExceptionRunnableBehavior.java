package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionRunnableBehavior {

	@Test
	public void shouldConvertToRunnable() {
		ExceptionRunnable.NULL.asRunnable().run();
		runnable(2).asRunnable().run();
		assertRte(() -> runnable(1).asRunnable().run());
		assertRte(() -> runnable(0).asRunnable().run());
	}

	@Test
	public void shouldConvertFromRunnable() {
		ExceptionRunnable.of(Std.runnable(1)).run();
		assertRte(() -> ExceptionRunnable.of(Std.runnable(0)).run());
	}

}
