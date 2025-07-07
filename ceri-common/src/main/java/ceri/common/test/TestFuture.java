package ceri.common.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.time.Timeout;

public class TestFuture<T> extends FutureTask<T> {
	public final CallSync.Function<Timeout, T> get = CallSync.function(null);

	@SafeVarargs
	public static <T> TestFuture<T> of(T... results) {
		TestFuture<T> future = new TestFuture<>();
		future.get.autoResponses(results);
		return future;
	}

	protected TestFuture() {
		super(() -> null);
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return get.applyWithInterrupt(Timeout.NULL, ExceptionAdapter.none);
		} catch (InterruptedException | ExecutionException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException {
		try {
			return get.applyWithInterrupt(Timeout.of(timeout, unit), ExceptionAdapter.none);
		} catch (InterruptedException | ExecutionException | TimeoutException
			| RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}