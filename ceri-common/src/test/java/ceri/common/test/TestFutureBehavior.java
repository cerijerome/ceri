package ceri.common.test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import ceri.common.time.Timeout;

public class TestFutureBehavior {

	@Test
	public void shouldRunFuture() {
		var future = TestFuture.of();
		future.run();
		Assert.yes(future.isDone());
	}

	@Test
	public void shouldGetResult() throws InterruptedException, ExecutionException {
		var future = TestFuture.of("test");
		Assert.equal(future.get(), "test");
		future.get.assertAuto(Timeout.NULL);
	}

	@Test
	public void shouldGetTimeoutResult()
		throws InterruptedException, ExecutionException, TimeoutException {
		var future = TestFuture.of("test");
		Assert.equal(future.get(1, TimeUnit.MILLISECONDS), "test");
		future.get.assertAuto(Timeout.millis(1));
	}

	@Test
	public void shouldGetWithException() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(ErrorGen.IOX);
		var t = Testing.thrown(() -> future.get());
		Assert.throwable(t, ExecutionException.class);
		Assert.throwable(t.getCause(), IOException.class);
	}

	@Test
	public void shouldGetTimeoutWithException() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(ErrorGen.IOX);
		var t = Testing.thrown(() -> future.get(1, TimeUnit.MILLISECONDS));
		Assert.throwable(t, ExecutionException.class);
		Assert.throwable(t.getCause(), IOException.class);
	}
}
