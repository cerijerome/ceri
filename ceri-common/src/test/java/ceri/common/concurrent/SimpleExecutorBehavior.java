package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SimpleExecutorBehavior {

	@Test
	public void shouldFailForNullCallable() {
		assertThrown(() -> SimpleExecutor.call(null));
	}

	@Test
	public void shouldReturnConstant() {
		try (var exec = SimpleExecutor.run(() -> {}, "test")) {
			assertThat(exec.get(1000), is("test"));
			assertThat(exec.get(), is("test"));
		}
	}

	@Test
	public void shouldExecuteCallable() {
		try (var exec = SimpleExecutor.call(() -> "test")) {
			assertThat(exec.get(1000), is("test"));
			assertThat(exec.get(), is("test"));
		}
	}

	@Test
	public void shouldCancel() {
		try (var exec = SimpleExecutor.run(() -> ConcurrentUtil.delay(10000))) {
			exec.cancel();
		}
	}

}
