package ceri.common.concurrent;

import static ceri.common.function.FunctionUtil.truePredicate;
import static java.util.function.Predicate.isEqual;
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
	private static final BinaryOperator<Object> REPLACER = (latest, current) -> latest;
	private final Lock lock;
	private final Condition condition;
	private final BinaryOperator<T> merger;
	private T value = null;

	public static <T> ValueCondition<T> of() {
		return of(new ReentrantLock());
	}

	public static <T> ValueCondition<T> of(Lock lock) {
		return of(lock, BasicUtil.uncheckedCast(REPLACER));
	}

	public static <T> ValueCondition<T> of(BinaryOperator<T> merger) {
		return of(new ReentrantLock(), merger);
	}

	public static <T> ValueCondition<T> of(Lock lock, BinaryOperator<T> merger) {
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
	 * Set/merge current value and signal waiting threads.
	 */
	public void signal(T value) {
		ConcurrentUtil.execute(lock, () -> {
			this.value = merger.apply(value, this.value);
			condition.signalAll();
		});
	}

	/**
	 * Waits indefinitely for current value to be non-null. Current value is cleared.
	 */
	public T await() throws InterruptedException {
		return await(truePredicate());
	}

	/**
	 * Waits indefinitely for current value to equal given value. Current value is cleared.
	 */
	public T await(T value) throws InterruptedException {
		return await(isEqual(value));
	}

	/**
	 * Waits indefinitely for predicate to be true. Current value is cleared.
	 */
	public T await(Predicate<T> predicate) throws InterruptedException {
		return await(Timer.INFINITE, predicate);
	}

	/**
	 * Waits for current value to be non-null, or timer to expire. Current value is cleared.
	 */
	public T await(long timeoutMs) throws InterruptedException {
		return await(timeoutMs, truePredicate());
	}

	/**
	 * Waits for current value to equal given value, or timer to expire. Current value is cleared.
	 */
	public T await(long timeoutMs, T value) throws InterruptedException {
		return await(timeoutMs, isEqual(value));
	}

	/**
	 * Waits for predicate to be true, or timer to expire. Current value is cleared.
	 */
	public T await(long timeoutMs, Predicate<T> predicate) throws InterruptedException {
		return await(Timer.millis(timeoutMs), predicate);
	}

	private T await(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			T returnValue = awaitValue(timer, predicate);
			value = null;
			return returnValue;
		});
	}

	/**
	 * Clears current value without signaling waiting threads.
	 */
	public T clear() {
		return set(null);
	}

	/**
	 * Waits indefinitely for current value to be non-null.
	 */
	public T awaitPeek() throws InterruptedException {
		return awaitPeek(truePredicate());
	}

	/**
	 * Waits indefinitely for current value to equal given value.
	 */
	public T awaitPeek(T value) throws InterruptedException {
		return awaitPeek(isEqual(value));
	}

	/**
	 * Waits indefinitely for predicate to be true.
	 */
	public T awaitPeek(Predicate<T> predicate) throws InterruptedException {
		return awaitPeek(Timer.INFINITE, predicate);
	}

	/**
	 * Waits for current value to be non-null, or timer to expire.
	 */
	public T awaitPeek(long timeoutMs) throws InterruptedException {
		return awaitPeek(timeoutMs, truePredicate());
	}

	/**
	 * Waits for current value to equal given value, or timer to expire.
	 */
	public T awaitPeek(long timeoutMs, T value) throws InterruptedException {
		return awaitPeek(timeoutMs, isEqual(value));
	}

	/**
	 * Waits for predicate to be true, or timer to expire.
	 */
	public T awaitPeek(long timeoutMs, Predicate<T> predicate) throws InterruptedException {
		return awaitPeek(Timer.millis(timeoutMs), predicate);
	}

	private T awaitPeek(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> awaitValue(timer, predicate));
	}

	/**
	 * Returns the current value.
	 */
	public T value() {
		return ConcurrentUtil.executeGet(lock, () -> value);
	}

	/**
	 * Waits for predicate to be true (on entry, or after signal) or timer to expire.
	 */
	private T awaitValue(Timer timer, Predicate<T> predicate) throws InterruptedException {
		timer.start();
		while (value == null || !predicate.test(value)) {
			Timer.Snapshot snapshot = timer.snapshot();
			if (snapshot.expired()) break;
			if (snapshot.infinite()) condition.await();
			else snapshot.applyRemaining(t -> condition.await(t, TimeUnit.MILLISECONDS));
		}
		return value;
	}

}
