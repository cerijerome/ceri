package ceri.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;
import ceri.common.util.BasicUtil;

/**
 * Simple condition to signal and wait for a value change.
 */
public class ValueCondition<T> {
	private static BinaryOperator<Object> REPLACER = (latest, current) -> latest;
	private final Lock lock;
	private final Condition condition;
	private final BinaryOperator<T> merger;
	private T value = null;

	public static <T> ValueCondition<T> create() {
		return create(new ReentrantLock());
	}

	public static <T> ValueCondition<T> create(Lock lock) {
		return create(lock, BasicUtil.uncheckedCast(REPLACER));
	}

	public static <T> ValueCondition<T> create(BinaryOperator<T> merger) {
		return create(new ReentrantLock(), merger);
	}

	public static <T> ValueCondition<T> create(Lock lock, BinaryOperator<T> merger) {
		return new ValueCondition<>(lock, merger);
	}

	private ValueCondition(Lock lock, BinaryOperator<T> merger) {
		this.lock = lock;
		this.merger = merger;
		condition = lock.newCondition();
	}

	public void signal(T value) {
		ConcurrentUtil.execute(lock, () -> {
			this.value = merger.apply(value, this.value);
			condition.signal();
		});
	}

	public T await() throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			while (value == null)
				condition.await();
			T returnValue = value;
			value = null;
			return returnValue;
		});
	}

	public T await(long timeoutMs) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			if (value == null && timeoutMs > 0) condition.await(timeoutMs, TimeUnit.MILLISECONDS);
			T returnValue = value;
			value = null;
			return returnValue;
		});
	}

	public T clear() {
		return ConcurrentUtil.executeGet(lock, () -> {
			T returnValue = value;
			value = null;
			return returnValue;
		});
	}

	public T awaitPeek() throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			while (value == null)
				condition.await();
			return value;
		});
	}

	public T awaitPeek(long timeoutMs) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			if (value == null && timeoutMs > 0) condition.await(timeoutMs, TimeUnit.MILLISECONDS);
			return value;
		});
	}

	public T value() {
		return ConcurrentUtil.executeGet(lock, () -> value);
	}

}
