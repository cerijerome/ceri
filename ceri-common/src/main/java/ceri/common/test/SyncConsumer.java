package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;

/**
 * Utility for synchronizing calls from a thread, and generating exceptions based on call count. A
 * call from a thread will only complete once the resume condition is signaled, either by calling
 * awaitCall or assertCall. This ensures no calls are missed by the test class.
 */
public class SyncConsumer<E extends Exception, T> implements ExceptionConsumer<E, T> {
	private final List<ThrowEntry<RuntimeException>> rtExceptions = new ArrayList<>();
	private final List<ThrowEntry<E>> exceptions = new ArrayList<>();
	private final ValueCondition<T> call = ValueCondition.of();
	private final BooleanCondition resume = BooleanCondition.of();
	private volatile int count = 0;

	private static class ThrowEntry<E extends Exception> {
		public final Predicate<Integer> countMatch;
		public final Function<String, E> exceptionFn;

		public ThrowEntry(Function<String, E> exceptionFn, Predicate<Integer> countMatcher) {
			this.countMatch = countMatcher;
			this.exceptionFn = exceptionFn;
		}

		public void exec(int count) throws E {
			if (!countMatch.test(count)) return;
			throw exceptionFn.apply("count = " + count);
		}
	}

	public static class Bool<E extends Exception> extends SyncConsumer<E, Boolean> {
		protected Bool() {}

		public void accept() throws E {
			super.accept(Boolean.TRUE);
		}
	}

	public static <E extends Exception> Bool<E> bool() {
		return new Bool<>();
	}

	public static <E extends Exception, T> SyncConsumer<E, T> of() {
		return new SyncConsumer<>();
	}

	protected SyncConsumer() {}

	/**
	 * The call to be made by the thread. Signals waiting callers, waits for resume, then
	 * throws an exception if a predicate has been set.
	 */
	@Override
	public void accept(T t) throws E {
		try {
			call.signal(t);
			ConcurrentUtil.executeInterruptible(resume::await);
			for (ThrowEntry<E> ex : exceptions) ex.exec(count);
			for (ThrowEntry<RuntimeException> ex : rtExceptions) ex.exec(count);
		} finally {
			count++;
		}
	}

	/**
	 * Waits for a call, then signals the call to resume.
	 */
	public T awaitCall() throws InterruptedException {
		T t = call.await();
		resume.signal();
		return t;
	}

	/**
	 * Waits for a call, executes the runnable, then signals the call to resume.
	 */
	public T awaitCall(ExceptionRunnable<E> runnable) throws InterruptedException, E {
		T t = call.await();
		runnable.run();
		resume.signal();
		return t;
	}

	/**
	 * Asserts that the call passed in the value. Signals the call to resume.
	 */
	public void assertCall(T value) throws InterruptedException {
		assertThat(call.await(), is(value));
		resume.signal();
	}

	/**
	 * Asserts that the call passed in the value. Executes runnable, the signals the call to resume.
	 */
	public void assertCall(T value, ExceptionRunnable<E> runnable) throws InterruptedException, E {
		assertThat(call.await(), is(value));
		runnable.run();
		resume.signal();
	}

	/**
	 * Asserts that no call was made.
	 */
	public void assertNoCall() {
		assertNull(call.value());
	}

	/**
	 * Generate a RuntimeInterruptedException whenever a call is made.
	 */
	public SyncConsumer<E, T> rtiError() {
		return rtiError((Integer i) -> true);
	}

	/**
	 * Generate a RuntimeInterruptedException whenever the predicate matches the call count.
	 */
	public SyncConsumer<E, T> rtiError(Predicate<Integer> countMatch) {
		rtExceptions.add(new ThrowEntry<>(RuntimeInterruptedException::new, countMatch));
		return this;
	}

	/**
	 * Generate a RuntimeException whenever a call is made.
	 */
	public SyncConsumer<E, T> rtError() {
		return rtError((Integer i) -> true);
	}

	/**
	 * Generate a RuntimeException whenever the predicate matches the call count.
	 */
	public SyncConsumer<E, T> rtError(Predicate<Integer> countMatch) {
		rtExceptions.add(new ThrowEntry<>(RuntimeException::new, countMatch));
		return this;
	}

	/**
	 * Generate the Exception whenever a call is made. The count as text is passed to the exception
	 * constructor.
	 */
	public SyncConsumer<E, T> error(Function<String, E> exceptionFn) {
		return error(exceptionFn, i -> true);
	}

	/**
	 * Generate the Exception whenever the predicate matches the call count. The count as text is
	 * passed to the exception constructor.
	 */
	public SyncConsumer<E, T> error(Function<String, E> exceptionFn,
		Predicate<Integer> countMatch) {
		exceptions.add(new ThrowEntry<>(exceptionFn, countMatch));
		return this;
	}

}
