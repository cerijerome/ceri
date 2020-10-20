package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.test.ErrorGen.Mode;
import ceri.common.util.Counter;

/**
 * Utility for synchronizing calls from a thread, and generating exceptions. If autoResponse is set,
 * the thread continues when the sync call is made. If not, the thread waits for the response
 * condition to be signaled; either by awaitCall or assertCall. This ensures no calls are missed by
 * the test class.
 */
public class CallSync<T, R> {
	private static final Object OBJ = new Object();
	private static final Function<String, RuntimeException> rtxFn = RuntimeException::new;
	private final T valueDef;
	private final ErrorGen error = ErrorGen.of();
	private final ValueCondition<T> callSync = ValueCondition.of();
	private final ValueCondition<R> responseSync = ValueCondition.of();
	private volatile Function<T, R> autoResponseFn = null;
	private volatile BiFunction<T, R, Mode> errorModeFn;
	private volatile T lastValue = null;

	/**
	 * Creates a synchronizing function. Use null to disable auto response.
	 */
	public static <T, R> Apply<T, R> function(T valueDef, R autoResponse) {
		Apply<T, R> apply = new Apply<>(valueDef);
		apply.autoResponse(autoResponse);
		return apply;
	}

	/**
	 * Creates a synchronizing consumer.
	 */
	public static <T> Accept<T> consumer(T valueDef, boolean autoResponse) {
		Accept<T> accept = new Accept<>(valueDef);
		accept.autoResponse(autoResponse);
		return accept;
	}

	/**
	 * Creates a synchronizing supplier. Use null to disable auto response.
	 */
	public static <R> Get<R> supplier(R autoResponse) {
		Get<R> get = new Get<>();
		get.autoResponse(autoResponse);
		return get;
	}

	/**
	 * Creates a synchronizing runnable.
	 */
	public static Run runnable(boolean autoResponse) {
		Run run = new Run();
		run.autoResponse(autoResponse);
		return run;
	}

	/**
	 * Sub-class for a function call.
	 */
	public static class Apply<T, R> extends CallSync<T, R> {
		protected Apply(T valueDef) {
			super(valueDef);
		}

		/**
		 * Returns the last value passed to the call.
		 */
		public T value() {
			return super.lastValue;
		}

		/**
		 * Sets the call value without signaling.
		 */
		public void value(T value) {
			super.lastValue = value;
		}

		/**
		 * Sets the error generator mode, evaluated when a call is made.
		 */
		public void error(BiFunction<T, R, Mode> errorModeFn) {
			super.errorFromFn(errorModeFn);
		}

		/**
		 * Sets the auto response value. Use null to disable.
		 */
		public void autoResponse(R response) {
			autoResponse(response != null ? t -> response : null);
		}

		/**
		 * Sets the auto response function. Use null to disable.
		 */
		public void autoResponse(Function<T, R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn != null ? t -> autoResponseFn.apply(t) : null);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		public R apply(T t) {
			return apply(t, rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R apply(T t, Function<String, E> errorFn) throws E {
			return super.apply(t, errorFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException or InterruptedException if the generator is configured.
		 */
		public R applyWithInterrupt(T t) throws InterruptedException {
			return applyWithInterrupt(t, rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R applyWithInterrupt(T t, Function<String, E> errorFn)
			throws InterruptedException, E {
			return super.applyWithInterrupt(t, errorFn);
		}

		/**
		 * Awaits the call, and responds without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public T awaitAuto() throws InterruptedException {
			return super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call, signals completion, and responds with the given value. Use when
		 * autoResponse is disabled.
		 */
		public T await(R response) throws InterruptedException {
			return await(t -> response);
		}

		/**
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> T await(ExceptionFunction<E, T, R> responseFn)
			throws InterruptedException, E {
			return super.awaitCallWithResponse(responseFn);
		}

		/**
		 * Awaits and asserts the call, without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void assertAuto(T value) throws InterruptedException {
			super.assertCallWithAutoResponse(value);
		}

		/**
		 * Awaits and asserts the call, signals completion, and responds with the given value. Use
		 * when autoResponse is disabled.
		 */
		public void assertCall(T value, R response) throws InterruptedException {
			assertCall(value, t -> response);
		}

		/**
		 * Awaits and asserts the call, evaluates the response action, and signals completion. Use
		 * when autoResponse is disabled.
		 */
		public <E extends Exception> void assertCall(T value, ExceptionFunction<E, T, R> responseFn)
			throws InterruptedException, E {
			super.assertCallWithResponse(value, responseFn);
		}
	}

	/**
	 * Sub-class for a consumer call.
	 */
	public static class Accept<T> extends CallSync<T, Object> {
		protected Accept(T valueDef) {
			super(valueDef);
		}

		/**
		 * Returns the last value passed to the call.
		 */
		public T value() {
			return super.lastValue;
		}

		/**
		 * Sets the call value without signaling.
		 */
		public void value(T value) {
			super.lastValue = value;
		}

		/**
		 * Sets the error generator mode, evaluated when a call is made.
		 */
		public void error(Function<T, Mode> errorModeFn) {
			super.errorFromFn((t, r) -> errorModeFn.apply(t));
		}

		/**
		 * Enables/disables auto response.
		 */
		public void autoResponse(boolean enabled) {
			super.autoResponseFn(enabled ? t -> OBJ : null);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		public void accept(T t) {
			accept(t, rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void accept(T t, Function<String, E> errorFn) throws E {
			super.apply(t, errorFn);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException or
		 * InterruptedException if the generator is configured.
		 */
		public void acceptWithInterrupt(T t) throws InterruptedException {
			acceptWithInterrupt(t, rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void acceptWithInterrupt(T t, Function<String, E> errorFn)
			throws InterruptedException, E {
			super.applyWithInterrupt(t, errorFn);
		}

		/**
		 * Awaits the call without signaling completion. Use when autoResponse is enabled.
		 */
		public T awaitAuto() throws InterruptedException {
			return super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call and signals completion. Use when autoResponse is disabled.
		 */
		public T await() throws InterruptedException {
			return await(t -> {});
		}

		/**
		 * Awaits the call, executes the action, and signals completion. Use when autoResponse is
		 * disabled.
		 */
		public <E extends Exception> T await(ExceptionConsumer<E, T> actionFn)
			throws InterruptedException, E {
			return super.awaitCallWithResponse(t -> exec(t, actionFn));
		}

		/**
		 * Awaits and asserts the call, without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void assertAuto(T value) throws InterruptedException {
			super.assertCallWithAutoResponse(value);
		}

		/**
		 * Awaits and asserts the call, and signals completion. Use when autoResponse is disabled.
		 */
		public void assertCall(T value) throws InterruptedException {
			assertCall(value, t -> {});
		}

		/**
		 * Awaits and asserts the call, executes the action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void assertCall(T value, ExceptionConsumer<E, T> actionFn)
			throws InterruptedException, E {
			super.assertCallWithResponse(value, t -> exec(t, actionFn));
		}

		private <E extends Exception> Object exec(T t, ExceptionConsumer<E, T> responseFn)
			throws E {
			responseFn.accept(t);
			return OBJ;
		}
	}

	/**
	 * Sub-class for a supplier call.
	 */
	public static class Get<R> extends CallSync<Object, R> {
		protected Get() {
			super(OBJ);
		}

		/**
		 * Sets the error generator mode, evaluated when a call is made.
		 */
		public void error(Function<R, Mode> errorModeFn) {
			super.errorFromFn((t, r) -> errorModeFn.apply(r));
		}

		/**
		 * Sets the auto response value. Use null to disable.
		 */
		public void autoResponse(R response) {
			autoResponse(response != null ? () -> response : null);
		}

		/**
		 * Sets the auto response supplier. Use null to disable.
		 */
		public void autoResponse(Supplier<R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn != null ? t -> autoResponseFn.get() : null);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		public R get() {
			return get(rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R get(Function<String, E> errorFn) throws E {
			return super.apply(OBJ, errorFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException or InterruptedException if the generator is configured.
		 */
		public R getWithInterrupt() throws InterruptedException {
			return getWithInterrupt(rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R getWithInterrupt(Function<String, E> errorFn)
			throws E, InterruptedException {
			return super.applyWithInterrupt(OBJ, errorFn);
		}

		/**
		 * Awaits the call, and responds without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void awaitAuto() throws InterruptedException {
			super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call, signals completion, and responds with the given value. Use when
		 * autoResponse is disabled.
		 */
		public void await(R response) throws InterruptedException {
			await(() -> response);
		}

		/**
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void await(ExceptionSupplier<E, R> responseFn)
			throws InterruptedException, E {
			super.awaitCallWithResponse(t -> responseFn.get());
		}
	}

	/**
	 * Sub-class for a runnable call.
	 */
	public static class Run extends CallSync<Object, Object> {
		protected Run() {
			super(OBJ);
		}

		/**
		 * Enables/disables auto response.
		 */
		public void autoResponse(boolean enabled) {
			super.autoResponseFn(enabled ? t -> OBJ : null);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		public void run() {
			run(RuntimeException::new);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void run(Function<String, E> errorFn) throws E {
			super.apply(OBJ, errorFn);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException or
		 * InterruptedException if the generator is configured.
		 */
		public void runWithInterrupt() throws InterruptedException {
			runWithInterrupt(rtxFn);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void runWithInterrupt(Function<String, E> errorFn)
			throws E, InterruptedException {
			super.applyWithInterrupt(OBJ, errorFn);
		}

		/**
		 * Awaits the call without signaling completion. Use when autoResponse is enabled.
		 */
		public void awaitAuto() throws InterruptedException {
			super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call and signals completion. Use when autoResponse is disabled.
		 */
		public void await() throws InterruptedException {
			await(() -> {});
		}

		/**
		 * Awaits the call, executes the action, and signals completion. Use when autoResponse is
		 * disabled.
		 */
		public <E extends Exception> void await(ExceptionRunnable<E> actionFn)
			throws InterruptedException, E {
			super.awaitCallWithResponse(t -> exec(actionFn));
		}

		private <E extends Exception> Object exec(ExceptionRunnable<E> responseFn) throws E {
			responseFn.run();
			return OBJ;
		}
	}

	protected CallSync(T valueDef) {
		this.valueDef = valueDef;
		reset();
	}

	/**
	 * Reset state. Does not change auto response.
	 */
	public void reset() {
		lastValue = valueDef;
		errorModeFn = (t, r) -> Mode.none;
		error.reset();
		callSync.clear();
		responseSync.clear();
	}

	/**
	 * Sets the error generator mode.
	 */
	public void error(Mode mode) {
		error(() -> mode);
	}

	/**
	 * Sets the error generator mode, based on call count.
	 */
	public void errorForIndex(Mode mode, int... indexes) {
		List<Integer> list = ArrayUtil.intList(indexes);
		Counter counter = Counter.of();
		error(() -> list.contains(counter.intInc() - 1) ? mode : Mode.none);
	}

	/**
	 * Sets the error generator mode, evaluated when a call is made.
	 */
	public void error(Supplier<Mode> modeSupplier) {
		errorFromFn((t, r) -> modeSupplier.get());
	}

	/**
	 * Asserts that no call was made.
	 */
	public void assertNoCall() {
		assertNull(callSync.value());
	}

	private void errorFromFn(BiFunction<T, R, Mode> errorModeFn) {
		this.errorModeFn = errorModeFn;
	}

	private void autoResponseFn(Function<T, R> autoResponseFn) {
		this.autoResponseFn = autoResponseFn;
	}

	private <E extends Exception> R apply(T value, Function<String, E> errorFn) throws E {
		R response = sync(value);
		error.mode(errorModeFn.apply(value, response));
		error.generate(errorFn);
		lastValue = value;
		return response;
	}

	private <E extends Exception> R applyWithInterrupt(T value, Function<String, E> errorFn)
		throws InterruptedException, E {
		R response = sync(value);
		error.mode(errorModeFn.apply(value, response));
		error.generateWithInterrupt(errorFn);
		lastValue = value;
		return response;
	}

	private T awaitCallWithAutoResponse() throws InterruptedException {
		return callSync.await();
	}

	private <E extends Exception> T awaitCallWithResponse(ExceptionFunction<E, T, R> responseFn)
		throws InterruptedException, E {
		T t = awaitCallWithAutoResponse();
		responseSync.signal(responseFn.apply(t));
		return t;
	}

	private void assertCallWithAutoResponse(T value) throws InterruptedException {
		assertEquals(callSync.await(), value);
	}

	private <E extends Exception> void assertCallWithResponse(T value,
		ExceptionFunction<E, T, R> responseFn) throws InterruptedException, E {
		assertCallWithAutoResponse(value);
		responseSync.signal(responseFn.apply(value));
	}

	private R sync(T t) {
		callSync.signal(t);
		var autoResponseFn = this.autoResponseFn;
		if (autoResponseFn != null) return autoResponseFn.apply(t);
		return ConcurrentUtil.executeGetInterruptible(responseSync::await);
	}

}
