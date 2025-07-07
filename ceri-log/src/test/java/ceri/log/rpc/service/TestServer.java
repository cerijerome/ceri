package ceri.log.rpc.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.test.CallSync;
import ceri.common.time.Timeout;
import io.grpc.Server;

public class TestServer extends Server {
	public final CallSync.Runnable start = CallSync.runnable(true);
	public final CallSync.Consumer<Runnable> execute = CallSync.consumer(null, true);
	public final CallSync.Consumer<Boolean> shutdown = CallSync.consumer(false, true);
	public final CallSync.Function<Timeout, Boolean> awaitTermination = CallSync.function(null, true);

	public static TestServer of() {
		return new TestServer();
	}

	protected TestServer() {}

	@Override
	public TestServer start() throws IOException {
		start.run(ExceptionAdapter.io);
		return this;
	}

	@Override
	public boolean isShutdown() {
		return shutdown.value();
	}

	@Override
	public TestServer shutdown() {
		return shutdownNow();
	}

	@Override
	public TestServer shutdownNow() {
		shutdown.accept(true);
		return this;
	}

	@Override
	public boolean isTerminated() {
		return isShutdown();
	}

	@Override
	public void awaitTermination() throws InterruptedException {
		awaitTermination.applyWithInterrupt(Timeout.NULL);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return awaitTermination.applyWithInterrupt(Timeout.of(timeout, unit));
	}
}
