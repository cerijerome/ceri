package ceri.common.concurrent;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import ceri.common.test.CallSync;
import ceri.common.time.Timeout;

public class TestExecutorService extends AbstractExecutorService {
	public final CallSync.Apply<Timeout, Boolean> awaitTermination = CallSync.function(null, true);

	public static TestExecutorService of() {
		return new TestExecutorService();
	}

	protected TestExecutorService() {}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return awaitTermination.applyWithInterrupt(Timeout.of(timeout, unit));
	}

	@Override
	public void execute(Runnable command) {}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public void shutdown() {}

	@Override
	public List<Runnable> shutdownNow() {
		return List.of();
	}
}