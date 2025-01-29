package ceri.common.concurrent;

import static ceri.common.function.FunctionUtil.truePredicate;
import static ceri.common.time.TimeSupplier.millis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.function.Predicate.isEqual;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import ceri.common.concurrent.ConcurrentUtil.LockInfo;
import ceri.common.time.Timer;
import ceri.common.util.BasicUtil;
import ceri.common.util.Holder;

/**
 * Simple condition to signal and wait for a value change.
 */
public class ValueCondition<T> {
	private static final BinaryOperator<Object> REPLACER = (latest, _) -> latest;
	public final Lock lock;
	private final Condition condition;
	private final BinaryOperator<T> merger;
	private T value = null;

	/**
	 * Creates an instance with its own lock. A new signal value overwrites an existing value.
	 */
	public static <T> ValueCondition<T> of() {
		return of(new ReentrantLock());
	}

	/**
	 * Creates an instance with the given lock. A new signal value overwrites an existing value.
	 */
	public static <T> ValueCondition<T> of(Lock lock) {
		return of(lock, BasicUtil.uncheckedCast(REPLACER));
	}

	/**
	 * Creates an instance with its own lock and a function that merges a new signal value to its
	 * existing value (either of which may be null).
	 */
	public static <T> ValueCondition<T> of(BinaryOperator<T> merger) {
		return of(new ReentrantLock(), merger);
	}

	/**
	 * Creates an instance with the given lock and a function that merges a new signal value to its
	 * existing value (either of which may be null).
	 */
	public static <T> ValueCondition<T> of(Lock lock, BinaryOperator<T> merger) {
		return new ValueCondition<>(lock, merger);
	}

	private ValueCondition(Lock lock, BinaryOperator<T> merger) {
		this.lock = lock;
		this.merger = merger;
		condition = lock.newCondition();
	}

	/**
	 * Set value without signaling waiting threads. Returns the previous value.
	 */
	public T set(T value) {
		return ConcurrentUtil.lockedGet(lock, () -> {
			T old = this.value;
			this.value = value;
			return old;
		});
	}

	/**
	 * Set/merge current value and signal waiting threads. Returns the previous value.
	 */
	public T signal(T value) {
		return ConcurrentUtil.lockedGet(lock, () -> {
			T old = this.value;
			this.value = merger.apply(value, old);
			condition.signalAll();
			return old;
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
	public T awaitTimeout(long timeoutMs) throws InterruptedException {
		return awaitTimeout(timeoutMs, truePredicate());
	}

	/**
	 * Waits for current value to equal given value, or timer to expire. Current value is cleared.
	 */
	public T awaitTimeout(long timeoutMs, T value) throws InterruptedException {
		return awaitTimeout(timeoutMs, isEqual(value));
	}

	/**
	 * Waits for predicate to be true, or timer to expire. Current value is cleared.
	 */
	public T awaitTimeout(long timeoutMs, Predicate<T> predicate) throws InterruptedException {
		return await(Timer.of(timeoutMs, millis), predicate);
	}

	private T await(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.lockedGet(lock, () -> {
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
		return awaitPeek(Timer.of(timeoutMs, MILLISECONDS), predicate);
	}

	private T awaitPeek(Timer timer, Predicate<T> predicate) throws InterruptedException {
		return ConcurrentUtil.lockedGet(lock, () -> awaitValue(timer, predicate));
	}

	/**
	 * Returns the current value.
	 */
	public T value() {
		return ConcurrentUtil.lockedGet(lock, () -> value);
	}

	/**
	 * Tries to get the current value as a holder. Returns an empty holder if the lock is
	 * unavailable.
	 */
	public Holder<T> tryValue() {
		return ConcurrentUtil.tryLockedGet(lock, () -> value);
	}

	/**
	 * Prints internal state; useful for debugging tests.
	 */
	@Override
	public String toString() {
		LockInfo info = ConcurrentUtil.lockInfo(lock);
		return String.format("%s;hold=%d;queue=%d", tryValue(), info.holdCount, info.queueLength);
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
