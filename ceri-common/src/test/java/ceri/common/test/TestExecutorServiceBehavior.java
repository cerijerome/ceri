package ceri.common.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.time.Timeout;

public class TestExecutorServiceBehavior {

	@Test
	public void shouldAwaitTermination() throws InterruptedException {
		try (var exec = TestExecutorService.of()) {
			Assert.yes(exec.awaitTermination(1, TimeUnit.MILLISECONDS));
			exec.awaitTermination.assertAuto(Timeout.millis(1));
		}
	}

	@Test
	public void shouldExecute() throws InterruptedException, ExecutionException {
		try (var exec = TestExecutorService.of()) {
			Assert.equal(exec.submit(() -> "test").get(), "test");
			exec.execute.awaitAuto();
		}
	}

	@Test
	public void shouldShutdown() {
		try (var exec = TestExecutorService.of()) {
			exec.shutdown();
			exec.shutdown.assertAuto(true);
			Assert.yes(exec.isShutdown());
			Assert.yes(exec.isTerminated());
		}
	}

}
