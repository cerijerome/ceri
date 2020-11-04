package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.thrown;
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
		assertTrue(future.isDone());
	}

	@Test
	public void shouldGetResult() throws InterruptedException, ExecutionException {
		var future = TestFuture.of("test");
		assertEquals(future.get(), "test");
		future.get.assertAuto(Timeout.NULL);
	}

	@Test
	public void shouldGetTimeoutResult()
		throws InterruptedException, ExecutionException, TimeoutException {
		var future = TestFuture.of("test");
		assertEquals(future.get(1, TimeUnit.MILLISECONDS), "test");
		future.get.assertAuto(Timeout.of(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void shouldGetWithException() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(IOX);
		Throwable t = thrown(() -> future.get());
		assertThrowable(t, ExecutionException.class);
		assertThrowable(t.getCause(), IOException.class);
	}

	@Test
	public void shouldGetTimeoutWithException() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(IOX);
		Throwable t = thrown(() -> future.get(1, TimeUnit.MILLISECONDS));
		assertThrowable(t, ExecutionException.class);
		assertThrowable(t.getCause(), IOException.class);
	}

}
