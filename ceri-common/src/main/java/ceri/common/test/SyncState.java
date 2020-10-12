package ceri.common.test;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionRunnable;
import ceri.common.test.ErrorGen.Mode;

/**
 * Utility for synchronizing calls from a thread, and generating exceptions. If resume is enabled,
 * the thread call will only complete once the resume condition is signaled, either by calling
 * awaitCall or assertCall. This ensures no calls are missed by the test class.
 */
public class SyncState<T> {
	private final ErrorGen error = ErrorGen.of();
	private final ValueCondition<T> call = ValueCondition.of();
	private final BooleanCondition resume = BooleanCondition.of();
	private volatile boolean resumeEnabled = true;

	/**
	 * Boolean-typed subclass that accepts calls without object parameters.
	 */
	public static class Bool extends SyncState<Boolean> {
		protected Bool() {}

		/**
		 * The call to be made by the thread. Signals waiting callers, and waits for resume if
		 * enabled. Error generator will throw an exception when configured.
		 */
		public void accept() {
			accept(TRUE);
		}

		public void acceptIo() throws IOException {
			acceptIo(TRUE);
		}

		public <E extends Exception> void accept(Function<String, E> errorFn) throws E {
			accept(TRUE, errorFn);
		}
	}

	public static Bool bool() {
		return new Bool();
	}

	public static <T> SyncState<T> of() {
		return new SyncState<>();
	}

	protected SyncState() {}
	
	/**
	 * Reset state. Does not change resume enabled state.
	 */
	public void reset() {
		error.reset();
		call.clear();
		resume.clear();
	}

	/**
	 * Sets the error generator mode.
	 */
	public SyncState<T> error(Mode mode) {
		return error(() -> mode);
	}

	/**
	 * Sets the error generator mode, evaluated during accept calls.
	 */
	public SyncState<T> error(Supplier<Mode> modeSupplier) {
		error.mode(modeSupplier);
		return this;
	}

	/**
	 * Enable or disable resume. If enabled, the accept calls will wait until awaitCall/assertCall
	 * is made.
	 */
	public SyncState<T> resume(boolean enabled) {
		resumeEnabled = enabled;
		return this;
	}

	/**
	 * Signals that a call has been made, and waits for the resume signal (if enabled). Throws an
	 * exception if the generator is configured.
	 */
	public void accept(T t) {
		sync(t);
		error.generate();
	}

	/**
	 * Signals that a call has been made, and waits for the resume signal (if enabled). Throws an
	 * exception if the generator is configured.
	 * 
	 * @throws IOException
	 */
	public void acceptIo(T t) throws IOException {
		sync(t);
		error.generateIo();
	}

	/**
	 * Signals that a call has been made, and waits for the resume signal (if enabled). Throws an
	 * exception if the generator is configured.
	 */
	public <E extends Exception> void accept(T t, Function<String, E> errorFn) throws E {
		sync(t);
		error.generate(errorFn);
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
	public <E extends Exception> T awaitCall(ExceptionRunnable<E> runnable)
		throws InterruptedException, E {
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
	public <E extends Exception> void assertCall(T value, ExceptionRunnable<E> runnable)
		throws InterruptedException, E {
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
	 * Signals that a call has been made, and waits for the resume signal (if enabled).
	 */
	protected void sync(T t) {
		call.signal(t);
		if (resumeEnabled) ConcurrentUtil.executeInterruptible(resume::await);
	}
}
