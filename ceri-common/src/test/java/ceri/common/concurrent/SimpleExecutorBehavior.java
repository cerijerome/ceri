package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class SimpleExecutorBehavior {

	@Test
	public void shouldFailForNullCallable() {
		assertThrown(() -> SimpleExecutor.call(null));
	}

	@Test
	public void shouldReturnConstant() {
		try (var exec = SimpleExecutor.run(() -> {}, "test")) {
			assertEquals(exec.get(1000), "test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldExecuteCallable() {
		try (var exec = SimpleExecutor.call(() -> "test")) {
			assertEquals(exec.get(1000), "test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldCancel() {
		try (var exec = SimpleExecutor.run(() -> Concurrent.delay(10000))) {
			exec.cancel();
		}
	}

}
