package ceri.common.test;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.time.Timeout;

/**
 * Types to support concurrency tests.
 */
public class TestConcurrent {
	private static final int DELAY_MICROS = 1;

	private TestConcurrent() {}

	/**
	 * A future task test implementation.
	 */
	public static class Future<T> extends FutureTask<T> {
		public final CallSync.Function<Timeout, T> get = CallSync.function(null);

		protected Future() {
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

	/**
	 * An executor service test implementation.
	 */
	public static class Exec extends AbstractExecutorService {
		public final CallSync.Consumer<Runnable> execute = CallSync.consumer(null, true);
		public final CallSync.Function<Boolean, List<Runnable>> shutdown =
			CallSync.function(false, List.of());
		public final CallSync.Function<Timeout, Boolean> awaitTermination =
			CallSync.function(null, true);

		protected Exec() {}

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

	/**
	 * Creates a test future that returns the results in sequence.
	 */
	@SafeVarargs
	public static <T> Future<T> futureOf(T... results) {
		var future = new Future<T>();
		future.get.autoResponses(results);
		return future;
	}

	/**
	 * Creates a test executor service.
	 */
	public static Exec exec() {
		return new Exec();
	}

	/**
	 * Repeat action with a microsecond delay until executor is closed. Useful to avoid intermittent
	 * thread timing issues when waiting on an event, by repeatedly triggering that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.Runnable<?> runnable) {
		return runRepeat(runnable, DELAY_MICROS);
	}

	/**
	 * Repeat action with a microsecond delay until executor is closed. Useful to avoid intermittent
	 * thread timing issues when waiting on an event, by repeatedly triggering that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.Runnable<?> runnable,
		int delayUs) {
		return runRepeat(_ -> runnable.run(), delayUs);
	}

	/**
	 * Repeat action with run count and a microsecond delay until executor is closed. Useful to
	 * avoid intermittent thread timing issues when waiting on an event, by repeatedly triggering
	 * that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.IntConsumer<?> action) {
		return runRepeat(action, DELAY_MICROS);
	}

	/**
	 * Repeat action with run count and a microsecond delay until executor is closed. Useful to
	 * avoid intermittent thread timing issues when waiting on an event, by repeatedly triggering
	 * that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.IntConsumer<?> action,
		int delayUs) {
		return SimpleExecutor.run(() -> {
			for (int i = 0;; i++) {
				action.accept(i);
				Concurrent.delayMicros(delayUs);
			}
		});
	}

	/**
	 * Execute a closable call in a separate thread. Use get() to retrieve the result.
	 */
	public static <T> SimpleExecutor<RuntimeException, T> threadCall(Callable<T> callable) {
		return SimpleExecutor.call(callable);
	}

	/**
	 * Execute a closable call in a separate thread. Use get() to wait for completion.
	 */
	public static SimpleExecutor<RuntimeException, ?> threadRun(Excepts.Runnable<?> runnable) {
		return SimpleExecutor.run(runnable);
	}
}
