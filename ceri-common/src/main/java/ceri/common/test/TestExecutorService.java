package ceri.common.test;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import ceri.common.time.Timeout;

public class TestExecutorService extends AbstractExecutorService {
	public final CallSync.Accept<Runnable> execute = CallSync.consumer(null, true);
	public final CallSync.Apply<Boolean, List<Runnable>> shutdown = CallSync.function(false, List.of());
	public final CallSync.Apply<Timeout, Boolean> awaitTermination = CallSync.function(null, true);

	public static TestExecutorService of() {
		return new TestExecutorService();
	}

	protected TestExecutorService() {}

	@Override
	public void execute(Runnable command) {
		command.run();
		execute.accept(command);
	}

	@Override
	public boolean isShutdown() {
		return shutdown.value();
	}

	@Override
	public void shutdown() {
		shutdownNow();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return shutdown.apply(true);
	}
	
	@Override
	public boolean isTerminated() {
		return isShutdown();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return awaitTermination.applyWithInterrupt(Timeout.of(timeout, unit));
	}
}