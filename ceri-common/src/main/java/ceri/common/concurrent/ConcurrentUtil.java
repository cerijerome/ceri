package ceri.common.concurrent;

import static ceri.common.math.MathUtil.addLimit;
import static ceri.common.math.MathUtil.multiplyLimit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.Holder;

public class ConcurrentUtil {
	private static final int MICROS_IN_NANOS = 1000;

	private ConcurrentUtil() {}

	/**
	 * Estimated lock state information.
	 */
	public static class LockInfo {
		public static final LockInfo NULL = new LockInfo(-1, -1);
		public final int holdCount;
		public final int queueLength;

		private LockInfo(int holdCount, int queueLength) {
			this.holdCount = holdCount;
			this.queueLength = queueLength;
		}

		@Override
		public String toString() {
			return String.format("hold=%d;queue=%d", holdCount, queueLength);
		}
	}

	/**
	 * Returns estimated lock information for debugging purposes. Returns LockInfo.NULL if not a
	 * ReentrantLock.
	 */
	public static LockInfo lockInfo(Lock lock) {
		ReentrantLock rlock = ReflectUtil.castOrNull(ReentrantLock.class, lock);
		return rlock == null ? LockInfo.NULL :
			new LockInfo(rlock.getHoldCount(), rlock.getQueueLength());
	}

	/**
	 * Sleeps approximately for given milliseconds, or not if 0. Throws RuntimeInterruptedException
	 * if interrupted. Checks for interrupted thread even if 0 delay.
	 */
	public static void delay(long delayMs) {
		checkRuntimeInterrupted();
		if (delayMs == 0) return;
		ConcurrentUtil.executeInterruptible(() -> Thread.sleep(delayMs));
	}

	/**
	 * Sleeps approximately for given microseconds, or not if 0. Throws RuntimeInterruptedException
	 * if interrupted. Checks for interrupted thread even if 0 delay.
	 */
	public static void delayMicros(long delayMicros) {
		delayNanos(multiplyLimit(delayMicros, MICROS_IN_NANOS), MICROS_IN_NANOS);
	}

	/**
	 * Sleeps approximately for given nanoseconds, or not if 0. Throws RuntimeInterruptedException
	 * if interrupted. Checks for interrupted thread even if 0 delay.
	 */
	public static void delayNanos(long delayNanos) {
		delayNanos(delayNanos, 1);
	}

	/**
	 * Calls await() on a Condition with or without a timeout. Use a null timeout to call without.
	 * Must be called inside a locked block.
	 */
	public static boolean await(Condition condition, Integer timeout, TimeUnit unit) {
		try {
			if (timeout != null) return condition.await(timeout, unit);
			condition.await();
			return true;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Shuts down an executor and waits for completion. Returns true if successfully shut down.
	 */
	public static boolean close(ExecutorService exec, int timeoutMs) {
		try {
			if (exec == null) return false;
			exec.shutdownNow();
			return exec.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public static <E extends Exception, T> T executeAndGet(ExecutorService executor,
		Callable<T> callable, Function<Throwable, ? extends E> exceptionConstructor) throws E {
		return get(executor.submit(callable), exceptionConstructor);
	}

	public static <E extends Exception, T> T executeAndGet(ExecutorService executor,
		Callable<T> callable, Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs)
		throws E {
		return get(executor.submit(callable), exceptionConstructor, timeoutMs);
	}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, ? extends E> exceptionConstructor)
		throws E {
		get(submit(executor, runnable), exceptionConstructor);
	}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, ? extends E> exceptionConstructor,
		int timeoutMs) throws E {
		get(submit(executor, runnable), exceptionConstructor, timeoutMs);
	}

	/**
	 * Calls future get with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Function<Throwable, ? extends E> exceptionConstructor) throws E {
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			throw exceptionConstructor.apply(e.getCause());
		}
	}

	/**
	 * Calls future get with millisecond time limit, with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs) throws E {
		try {
			return future.get(timeoutMs, MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (TimeoutException e) {
			throw exceptionConstructor.apply(e);
		} catch (ExecutionException e) {
			throw exceptionConstructor.apply(e.getCause());
		}
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, E> exceptionConstructor, ExceptionRunnable<E>... runnables)
		throws InterruptedException, E {
		invoke(executor, exceptionConstructor, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor,
		Collection<ExceptionRunnable<E>> runnables) throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, null, runnables);
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		ExceptionRunnable<E>... runnables) throws InterruptedException, E {
		invoke(executor, exceptionConstructor, timeoutMs, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		Collection<ExceptionRunnable<E>> runnables) throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, timeoutMs, runnables);
	}

	/**
	 * Submits a runnable to the executor service, allowing exceptions to be thrown. Returns a
	 * boolean Future.
	 */
	public static Future<?> submit(ExecutorService executor, ExceptionRunnable<?> runnable) {
		return executor.submit(callable(runnable));
	}

	/**
	 * Converts a runnable with exception into a callable type.
	 */
	public static Callable<Boolean> callable(ExceptionRunnable<?> runnable) {
		return () -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception, T> T executeGet(Lock lock, ExceptionSupplier<E, T> supplier)
		throws E {
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Executes the operation within the lock.
	 */
	public static <E extends Exception> void execute(Lock lock, ExceptionRunnable<E> runnable)
		throws E {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public static <E extends Exception, T> Holder<T> tryExecuteGet(Lock lock,
		ExceptionSupplier<E, T> supplier) throws E {
		if (!lock.tryLock()) return Holder.of();
		try {
			return Holder.of(supplier.get());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to execute the operation within the lock. Returns false if the lock is not available.
	 */
	public static <E extends Exception> boolean tryExecute(Lock lock, ExceptionRunnable<E> runnable)
		throws E {
		if (!lock.tryLock()) return false;
		try {
			runnable.run();
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks if the current thread has been interrupted and throws an InterruptedException.
	 */
	public static void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException("Thread has been interrupted");
	}

	/**
	 * Checks if the current thread has been interrupted and throws a RuntimeInterruptedException
	 * instead.
	 */
	public static void checkRuntimeInterrupted() throws RuntimeInterruptedException {
		if (Thread.interrupted())
			throw new RuntimeInterruptedException("Thread has been interrupted");
	}

	/**
	 * Executes and converts InterruptedException to runtime.
	 */
	public static void executeInterruptible(ExceptionRunnable<InterruptedException> runnable) {
		try {
			runnable.run();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Executes and converts InterruptedException to runtime.
	 */
	public static <T> T
		executeGetInterruptible(ExceptionSupplier<InterruptedException, T> supplier) {
		try {
			return supplier.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	private static void delayNanos(long delayNanos, long minNanos) {
		long deadline = addLimit(System.nanoTime(), delayNanos);
		while (true) {
			checkRuntimeInterrupted();
			long delayNs = deadline - System.nanoTime();
			if (delayNs < minNanos) return;
			LockSupport.parkNanos(delayNs); // can return spuriously
		}
	}

	/**
	 * Executes all runnable tasks and waits for completion. A null timeout will wait indefinitely.
	 * If the timeout expires, tasks are cancelled and a CancellationException is thrown.
	 */
	private static <E extends Exception> void invokeAll(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, Integer timeoutMs,
		Collection<ExceptionRunnable<E>> runnables) throws InterruptedException, E {
		List<Callable<Boolean>> callables =
			CollectionUtil.toList(ConcurrentUtil::callable, runnables);
		List<Future<Boolean>> futures = timeoutMs == null ? executor.invokeAll(callables) :
			executor.invokeAll(callables, timeoutMs, MILLISECONDS);
		for (Future<Boolean> future : futures)
			get(future, exceptionConstructor);
	}

}
