package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.test.Assert;

public class SimpleExecutorBehavior {

	@Test
	public void shouldFailForNullCallable() {
		Assert.thrown(() -> SimpleExecutor.call(null));
	}

	@Test
	public void shouldReturnConstant() {
		try (var exec = SimpleExecutor.run(() -> {}, "test")) {
			Assert.equal(exec.get(1000), "test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void shouldExecuteCallable() {
		try (var exec = SimpleExecutor.call(() -> "test")) {
			Assert.equal(exec.get(1000), "test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void shouldCancel() {
		try (var exec = SimpleExecutor.run(() -> Concurrent.delay(10000))) {
			exec.cancel();
		}
	}
}
