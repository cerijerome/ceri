package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.time.Timeout;

public class TestExecutorServiceBehavior {

	@Test
	public void shouldAwaitTermination() throws InterruptedException {
		var exec = TestExecutorService.of();
		assertTrue(exec.awaitTermination(1, TimeUnit.MILLISECONDS));
		exec.awaitTermination.assertAuto(Timeout.millis(1));
	}

	@Test
	public void shouldExecute() throws InterruptedException, ExecutionException {
		var exec = TestExecutorService.of();
		assertEquals(exec.submit(() -> "test").get(), "test");
		exec.execute.awaitAuto();
	}

	@Test
	public void shouldShutdown() {
		var exec = TestExecutorService.of();
		exec.shutdown();
		exec.shutdown.assertAuto(true);
		assertTrue(exec.isShutdown());
		assertTrue(exec.isTerminated());
	}

}
