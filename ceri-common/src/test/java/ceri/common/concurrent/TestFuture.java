package ceri.common.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.test.CallSync;
import ceri.common.time.Timeout;

public class TestFuture<T> extends FutureTask<T> {
	public final CallSync.Get<T> get = CallSync.supplier();
	public final CallSync.Apply<Timeout, T> getTimeout = CallSync.function(null);

	public static <T> TestFuture<T> of(T result) {
		return new TestFuture<>(result);
	}

	protected TestFuture(T result) {
		super(() -> result);
		get.autoResponses(result);
		getTimeout.autoResponses(result);
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return get.getWithInterrupt(ExceptionAdapter.NULL);
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
			return getTimeout.applyWithInterrupt(Timeout.of(timeout, unit), ExceptionAdapter.NULL);
		} catch (InterruptedException | ExecutionException | TimeoutException
			| RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}