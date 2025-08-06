package ceri.common.concurrent;

import static ceri.common.math.MathUtil.addLimit;
import static ceri.common.math.MathUtil.multiplyLimit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.StringUtil;
import ceri.common.time.Timer;
import ceri.common.util.Holder;

public class ConcurrentUtil {
	private static final int MICROS_IN_NANOS = 1000;
	public static final Condition NULL_CONDITION = nullCondition();
	public static final Lock NULL_LOCK = nullLock();

	private ConcurrentUtil() {}

	public interface TimedSupplier<E extends Exception, T> {
		/** Apply the time, and return the result. */
		T get(long time, TimeUnit unit) throws E, InterruptedException;
	}

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
		ReentrantLock rlock = Reflect.castOrNull(ReentrantLock.class, lock);
		return rlock == null ? LockInfo.NULL :
			new LockInfo(rlock.getHoldCount(), rlock.getQueueLength());
	}

	/**
	 * Sleeps approximately for given milliseconds, or not if 0. Throws RuntimeInterruptedException
	 * if interrupted. Checks for interrupted thread even if 0 delay.
	 */
	public static void delay(long delayMs) {
		checkRuntimeInterrupted();
		if (delayMs <= 0) return;
		ConcurrentUtil.runInterruptible(() -> Thread.sleep(delayMs));
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
	 * Calls the supplier with remaining time, retrying if interrupted. Restores the current thread
	 * interrupted status on completion..
	 */
	public static <E extends Exception, T> T getWhileInterrupted(TimedSupplier<E, T> supplier,
		long time, TimeUnit unit) throws E {
		boolean interrupted = Thread.interrupted();
		var timer = Timer.of(time, unit).start();
		try {
			while (true) {
				try {
					long t = Math.max(0L, timer.snapshot().remaining());
					return supplier.get(t, unit);
				} catch (RuntimeInterruptedException | InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) interrupt();
		}
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
	 * Calls future get with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Functions.Function<Throwable, ? extends E> exceptionConstructor) throws E {
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
		Functions.Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs) throws E {
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
	 * Submit the action and wait for the result.
	 */
	public static <E extends Exception, T> T submitAndGet(ExecutorService executor,
		Callable<T> callable, Functions.Function<Throwable, ? extends E> exceptionConstructor)
		throws E {
		return get(executor.submit(callable), exceptionConstructor);
	}

	/**
	 * Submit the action and wait for the result.
	 */
	public static <E extends Exception, T> T submitAndGet(ExecutorService executor,
		Callable<T> callable, Functions.Function<Throwable, ? extends E> exceptionConstructor,
		int timeoutMs) throws E {
		return get(executor.submit(callable), exceptionConstructor, timeoutMs);
	}

	/**
	 * Submit the action and wait for completion.
	 */
	public static <E extends Exception> void submitAndWait(ExecutorService executor,
		Excepts.Runnable<?> runnable,
		Functions.Function<Throwable, ? extends E> exceptionConstructor) throws E {
		get(submit(executor, runnable), exceptionConstructor);
	}

	/**
	 * Submit the action and wait for completion.
	 */
	public static <E extends Exception> void submitAndWait(ExecutorService executor,
		Excepts.Runnable<?> runnable,
		Functions.Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs) throws E {
		get(submit(executor, runnable), exceptionConstructor, timeoutMs);
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Functions.Function<Throwable, E> exceptionConstructor, Excepts.Runnable<E>... runnables)
		throws InterruptedException, E {
		invoke(executor, exceptionConstructor, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Functions.Function<Throwable, ? extends E> exceptionConstructor,
		Collection<Excepts.Runnable<E>> runnables) throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, null, runnables);
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Functions.Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		Excepts.Runnable<E>... runnables) throws InterruptedException, E {
		invoke(executor, exceptionConstructor, timeoutMs, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Functions.Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		Collection<Excepts.Runnable<E>> runnables) throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, timeoutMs, runnables);
	}

	/**
	 * Submits a task to the executor service and returns a Future. If the executor is shut down, a
	 * cancelled future is returned.
	 */
	public static Future<?> submit(ExecutorService executor, Excepts.Runnable<?> runnable) {
		return submit(executor, runnable, Boolean.TRUE);
	}

	/**
	 * Submits a task to the executor service and returns a Future. If the executor is shut down, a
	 * cancelled future is returned.
	 */
	public static <T> Future<T> submit(ExecutorService executor, Excepts.Runnable<?> runnable,
		T result) {
		return submit(executor, callable(runnable, result));
	}

	/**
	 * Submits a task to the executor service and returns a Future. If the executor is shut down, a
	 * cancelled future is returned.
	 */
	public static <T> Future<T> submit(ExecutorService executor, Callable<T> callable) {
		try {
			return executor.submit(callable);
		} catch (RejectedExecutionException e) {
			if (executor.isShutdown()) return Futures.cancelled();
			throw e;
		}
	}

	/**
	 * Converts a runnable with exception into a callable type.
	 */
	public static Callable<Boolean> callable(Excepts.Runnable<?> runnable) {
		return callable(runnable, Boolean.TRUE);
	}

	/**
	 * Converts a runnable with exception into a callable type.
	 */
	public static <T> Callable<T> callable(Excepts.Runnable<?> runnable, T result) {
		return () -> {
			runnable.run();
			return result;
		};
	}

	/**
	 * Provides a locked try-with-resources that unlocks on close.
	 */
	public static Functions.Closeable locker(Lock lock) {
		if (lock == null) return Functions.Closeable.NULL;
		lock.lock();
		return () -> lock.unlock();
	}

	/**
	 * Provides a locked try-with-resources that unlocks on close. Executes given post-lock and
	 * pre-unlock logic, making sure the lock is unlocked if an exception occurs.
	 */
	public static <E extends Exception> Excepts.Closeable<E> locker(Lock lock,
		Excepts.Runnable<E> postLock, Excepts.Runnable<E> preUnlock) throws E {
		lock.lock();
		try {
			if (postLock != null) postLock.run();
			return () -> {
				try {
					if (preUnlock != null) preUnlock.run();
				} finally {
					lock.unlock();
				}
			};
		} catch (Exception e) {
			lock.unlock();
			throw e;
		}
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception, T> T lockedGet(Lock lock, Excepts.Supplier<E, T> supplier)
		throws E {
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception> int lockedGetAsInt(Lock lock,
		Excepts.IntSupplier<E> supplier) throws E {
		lock.lock();
		try {
			return supplier.getAsInt();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception> long lockedGetAsLong(Lock lock,
		Excepts.LongSupplier<E> supplier) throws E {
		lock.lock();
		try {
			return supplier.getAsLong();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Executes the operation within the lock.
	 */
	public static <E extends Exception> void lockedRun(Lock lock, Excepts.Runnable<E> runnable)
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
	public static <E extends Exception, T> Holder<T> tryLockedGet(Lock lock,
		Excepts.Supplier<E, T> supplier) throws E {
		if (!lock.tryLock()) return Holder.of();
		try {
			return Holder.of(supplier.get());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public static <E extends Exception> OptionalInt tryLockedGetAsInt(Lock lock,
		Excepts.IntSupplier<E> supplier) throws E {
		if (!lock.tryLock()) return OptionalInt.empty();
		try {
			return OptionalInt.of(supplier.getAsInt());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public static <E extends Exception> OptionalLong tryLockedGetAsLong(Lock lock,
		Excepts.LongSupplier<E> supplier) throws E {
		if (!lock.tryLock()) return OptionalLong.empty();
		try {
			return OptionalLong.of(supplier.getAsLong());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to execute the operation within the lock. Returns false if the lock is not available.
	 */
	public static <E extends Exception> boolean tryLockedRun(Lock lock,
		Excepts.Runnable<E> runnable) throws E {
		if (!lock.tryLock()) return false;
		try {
			runnable.run();
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the thread name, or "null" if null.
	 */
	public static String name(Thread thread) {
		if (thread == null) return StringUtil.NULL;
		return thread.getName();
	}

	/**
	 * Interrupt the current thread.
	 */
	public static void interrupt() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Interrupt the current thread if the exception is from an interrupt. Returns true if the
	 * thread was interrupted.
	 */
	public static boolean interrupt(Exception e) {
		if (!interrupted(e)) return false;
		interrupt();
		return true;
	}

	/**
	 * Returns true if the current thread is interrupted, without clearing status.
	 */
	public static boolean interrupted() {
		return Thread.currentThread().isInterrupted();
	}

	/**
	 * Returns true if the exception is from an interrupt.
	 */
	public static boolean interrupted(Exception e) {
		return (e instanceof RuntimeInterruptedException) || (e instanceof InterruptedException);
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
	public static void runInterruptible(Excepts.Runnable<? extends InterruptedException> runnable) {
		try {
			runnable.run();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Executes and converts InterruptedException to runtime.
	 */
	public static <T> T getInterruptible(Excepts.Supplier<InterruptedException, T> supplier) {
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
	 * If the timeout expires, tasks are cancelled and a CancellationException is thrown. Does
	 * nothing If the executor is shut down.
	 */
	private static <E extends Exception> void invokeAll(ExecutorService executor,
		Functions.Function<Throwable, ? extends E> exceptionConstructor, Integer timeoutMs,
		Collection<Excepts.Runnable<E>> runnables) throws InterruptedException, E {
		var callables = CollectionUtil.toList(ConcurrentUtil::callable, runnables);
		try {
			var futures = timeoutMs == null ? executor.invokeAll(callables) :
				executor.invokeAll(callables, timeoutMs, MILLISECONDS);
			for (var future : futures)
				get(future, exceptionConstructor);
		} catch (RejectedExecutionException e) {
			if (!executor.isShutdown()) throw e;
		}
	}

	private static Lock nullLock() {
		return new Lock() {
			@Override
			public Condition newCondition() {
				return NULL_CONDITION;
			}

			@Override
			public void lockInterruptibly() throws InterruptedException {}

			@Override
			public void lock() {}

			@Override
			public boolean tryLock() {
				return false;
			}

			@Override
			public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
				return false;
			}

			@Override
			public void unlock() {}
		};
	}

	private static Condition nullCondition() {
		return new Condition() {
			@Override
			public void signalAll() {}

			@Override
			public void signal() {}

			@Override
			public void await() throws InterruptedException {}

			@Override
			public boolean await(long time, TimeUnit unit) throws InterruptedException {
				return false;
			}

			@Override
			public long awaitNanos(long nanosTimeout) throws InterruptedException {
				return 0;
			}

			@Override
			public boolean awaitUntil(Date deadline) throws InterruptedException {
				return false;
			}

			@Override
			public void awaitUninterruptibly() {}

		};
	}
}
