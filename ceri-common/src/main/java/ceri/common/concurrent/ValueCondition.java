package ceri.common.concurrent;

import static java.util.function.Predicate.isEqual;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import ceri.common.util.BasicUtil;
import ceri.common.util.Timer;

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

	/**
	 * Set value without signaling waiting threads.
	 */
	public T set(T value) {
		return ConcurrentUtil.executeGet(lock, () -> {
			T returnValue = this.value;
			this.value = value;
			return returnValue;
		});
	}

	/**
	 * Set value and signal waiting threads.
	 */
	public void signal(T value) {
		ConcurrentUtil.execute(lock, () -> {
			this.value = merger.apply(value, this.value);
			condition.signalAll();
		});
	}

	public T await() throws InterruptedException {
		return await(Objects::nonNull);
	}

	public T await(T value) throws InterruptedException {
		return await(isEqual(value));
	}

	public T await(Predicate<T> predicate) throws InterruptedException {
		return await(Timer.INFINITE, predicate);
	}

	public T await(long timeoutMs) throws InterruptedException {
		return await(timeoutMs, Objects::nonNull);
	}

	public T await(long timeoutMs, T value) throws InterruptedException {
		return await(timeoutMs, isEqual(value));
	}

	public T await(long timeoutMs, Predicate<T> predicate) throws InterruptedException {
		return await(Timer.of(timeoutMs), predicate);
	}

	private T await(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			T returnValue = awaitValue(timer, predicate);
			value = null;
			return returnValue;
		});
	}

	public T clear() {
		return set(null);
	}

	public T awaitPeek() throws InterruptedException {
		return awaitPeek(Objects::nonNull);
	}

	public T awaitPeek(T value) throws InterruptedException {
		return awaitPeek(isEqual(value));
	}

	public T awaitPeek(Predicate<T> predicate) throws InterruptedException {
		return awaitPeek(Timer.INFINITE, predicate);
	}

	public T awaitPeek(long timeoutMs) throws InterruptedException {
		return awaitPeek(timeoutMs, Objects::nonNull);
	}

	public T awaitPeek(long timeoutMs, T value) throws InterruptedException {
		return awaitPeek(timeoutMs, isEqual(value));
	}

	public T awaitPeek(long timeoutMs, Predicate<T> predicate) throws InterruptedException {
		return awaitPeek(Timer.of(timeoutMs), predicate);
	}

	private T awaitPeek(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			return awaitValue(timer, predicate);
		});
	}

	public T value() {
		return ConcurrentUtil.executeGet(lock, () -> value);
	}

	private T awaitValue(Timer timer, Predicate<T> predicate) throws InterruptedException {
		timer.start();
		while (!predicate.test(value)) {
			Timer.Snapshot snapshot = timer.snapshot();
			if (timer.snapshot().expired()) break;
			if (timer.isInfinite()) condition.await();
			else condition.await(snapshot.remaining, TimeUnit.MILLISECONDS);
		}
		return value;
	}

}
