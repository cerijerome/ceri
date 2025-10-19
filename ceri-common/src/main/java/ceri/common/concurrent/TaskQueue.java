package ceri.common.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;
import ceri.common.function.Functional;
import ceri.common.reflect.Reflect;

/**
 * A simple queue for processing tasks. Calls to execute a task wait until the task is complete,
 * either successfully, or by an exception being thrown. A queue processing thread calls the queue
 * to process the next available task in its current thread.
 */
public class TaskQueue<E extends Exception> {
	private final int maxSize;
	private final Lock lock;
	private final Condition sync;
	private final Queue<Task<E, ?>> queue = new ArrayDeque<>();

	/**
	 * Internal task, holding synchronization and result types.
	 */
	private static class Task<E extends Exception, T> {
		private final TaskQueue<E> queue;
		private final Condition sync;
		private final Supplier<E, T> action;
		private T result;
		private Exception ex;

		private Task(TaskQueue<E> queue, Supplier<E, T> action) {
			this.queue = queue;
			sync = queue.lock.newCondition();
			this.action = action;
		}

		private T exec() throws E {
			try {
				T result = action.get();
				return set(result, null);
			} catch (RuntimeException e) {
				set(null, e);
				throw e;
			} catch (Exception e) {
				set(null, e);
				throw Reflect.<E>unchecked(e);
			}
		}

		private T set(T result, Exception ex) {
			return Concurrent.lockedGet(queue.lock, () -> {
				this.result = result;
				this.ex = ex;
				sync.signalAll();
				return result;
			});
		}

		private T get(Integer timeout, TimeUnit unit) throws E {
			return Concurrent.lockedGet(queue.lock, () -> {
				if (!Concurrent.await(sync, timeout, unit)) return null;
				return result();
			});
		}

		private T result() throws E {
			if (ex == null) return result;
			if (ex instanceof RuntimeException e) throw e;
			throw Reflect.<E>unchecked(ex);
		}
	}

	/**
	 * Creates the task queue with maximum size.
	 */
	public static <E extends Exception> TaskQueue<E> of(int maxSize) {
		return of(maxSize, new ReentrantLock());
	}

	/**
	 * Creates the task queue with maximum size, using given lock for synchronization of tasks.
	 */
	public static <E extends Exception> TaskQueue<E> of(int maxSize, Lock lock) {
		return new TaskQueue<>(maxSize, lock);
	}

	private TaskQueue(int maxSize, Lock lock) {
		this.maxSize = maxSize;
		this.lock = lock;
		sync = lock.newCondition();
	}

	/**
	 * Executes the action, and waits for it to complete.
	 */
	public void execute(Runnable<E> action) throws E {
		executeGet(Functional.Adapt.runSupplier(action, null));
	}

	/**
	 * Executes the action, and waits for it to complete.
	 */
	public void execute(Runnable<E> action, int timeout, TimeUnit unit) throws E {
		executeGet(Functional.Adapt.runSupplier(action, null), timeout, unit);
	}

	/**
	 * Executes the action, waits for it to complete, and returns the result.
	 */
	public <T> T executeGet(Supplier<E, T> action) throws E {
		return Concurrent.lockedGet(lock, () -> add(action).get(null, null));
	}

	/**
	 * Executes the action, waits for it to complete, and returns the result.
	 */
	public <T> T executeGet(Supplier<E, T> action, int timeout, TimeUnit unit) throws E {
		return Concurrent.lockedGet(lock, () -> add(action).get(timeout, unit));
	}

	/**
	 * Wait for the next task, then process it.
	 */
	public void processNext() throws E {
		processNextTask(null, null);
	}

	/**
	 * Wait for the next task, then process it. Returns false if waiting timed out.
	 */
	public boolean processNext(int timeout, TimeUnit unit) throws E {
		return processNextTask(timeout, unit);
	}

	private boolean processNextTask(Integer timeout, TimeUnit unit) throws E {
		Task<E, ?> task = Concurrent.lockedGet(lock, () -> {
			if (queue.isEmpty()) Concurrent.await(sync, timeout, unit);
			return queue.poll();
		});
		if (task == null) return false;
		task.exec();
		return true;
	}

	private <T> Task<E, T> add(Supplier<E, T> action) {
		if (queue.size() >= maxSize)
			throw new IllegalStateException("Queue is full: " + queue.size());
		Task<E, T> task = new Task<>(this, action);
		queue.add(task);
		sync.signalAll();
		return task;
	}

}
