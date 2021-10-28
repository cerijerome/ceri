package ceri.common.test;

import static ceri.common.collection.CollectionUtil.getOrDefault;
import static ceri.common.concurrent.ConcurrentUtil.execute;
import static ceri.common.concurrent.ConcurrentUtil.executeGet;
import static ceri.common.concurrent.ConcurrentUtil.executeGetInterruptible;
import static ceri.common.concurrent.ConcurrentUtil.lockInfo;
import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.function.FunctionUtil.lambdaName;
import static ceri.common.function.FunctionUtil.sequentialSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ValueCondition;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.text.ToString;
import ceri.common.util.Counter;
import ceri.common.util.Holder;

/**
 * Thread-safe utility for synchronizing calls from a thread, and generating exceptions. If
 * autoResponse is set, the thread continues when the sync call is made. If not, the thread waits
 * for the response condition to be signaled; either by awaitCall or assertCall. This ensures no
 * calls are missed by the test class.
 */
public class CallSync<T, R> {
	private final Lock lock = new ReentrantLock();
	public final ErrorGen error = ErrorGen.of();
	private final ValueCondition<Holder<T>> callSync = ValueCondition.of(lock);
	private final ValueCondition<Holder<R>> responseSync = ValueCondition.of(lock);
	private final List<T> values = new ArrayList<>();
	private Function<T, R> autoResponseFn = null;
	private T valueDef;
	private boolean saveValues = true;

	/**
	 * Creates a synchronizing function.
	 */
	@SafeVarargs
	public static <T, R> Apply<T, R> function(T valueDef, R... autoResponses) {
		return new Apply<T, R>(valueDef).autoResponses(autoResponses);
	}

	/**
	 * Creates a synchronizing consumer.
	 */
	public static <T> Accept<T> consumer(T valueDef, boolean autoResponse) {
		return new Accept<>(valueDef).autoResponse(autoResponse);
	}

	/**
	 * Creates a synchronizing supplier.
	 */
	@SafeVarargs
	public static <R> Get<R> supplier(R... autoResponses) {
		return new Get<R>().autoResponses(autoResponses);
	}

	/**
	 * Creates a synchronizing runnable.
	 */
	public static Run runnable(boolean autoResponse) {
		return new Run().autoResponse(autoResponse);
	}

	/**
	 * Sub-class for a function call.
	 */
	public static class Apply<T, R> extends CallSync<T, R> implements Function<T, R> {
		protected Apply(T valueDef) {
			super(valueDef);
		}

		/**
		 * Set the default value.
		 */
		public Apply<T, R> valueDef(T value) {
			super.valueDef(value);
			return this;
		}

		/**
		 * Returns the last call value, or default if not set.
		 */
		public T value() {
			return super.getValue();
		}

		/**
		 * Sets the call value without signaling.
		 */
		public Apply<T, R> value(T value) {
			super.setValue(value);
			return this;
		}

		/**
		 * Returns call values set since creation or reset.
		 */
		public List<T> values() {
			return super.getValues();
		}

		/**
		 * Sets auto response values. The last value repeats.
		 */
		@SafeVarargs
		public final Apply<T, R> autoResponses(R... responses) {
			Supplier<R> supplier = sequentialSupplier(responses);
			return autoResponse(supplier != null ? x -> supplier.get() : null);
		}

		/**
		 * Sets the auto response function. Use null to disable.
		 */
		public Apply<T, R> autoResponse(Function<T, R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn);
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		@Override
		public R apply(T t) {
			return apply(t, RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R apply(T t, ExceptionAdapter<E> adapter) throws E {
			return super.apply(t, adapter);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException or InterruptedException if the generator is configured.
		 */
		public R applyWithInterrupt(T t) throws InterruptedException {
			return applyWithInterrupt(t, RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R applyWithInterrupt(T t, ExceptionAdapter<E> adapter)
			throws InterruptedException, E {
			return super.applyWithInterrupt(t, adapter);
		}

		/**
		 * Awaits the call, and responds without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public T awaitAuto() {
			return super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call, signals completion, and responds with the given value. Use when
		 * autoResponse is disabled.
		 */
		public T await(R response) {
			return await(t -> response);
		}

		/**
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> T await(ExceptionFunction<E, T, R> responseFn) throws E {
			return super.awaitCallWithResponse(responseFn);
		}

		/**
		 * Awaits and asserts the call, without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void assertAuto(T value) {
			super.assertCallWithAutoResponse(value);
		}

		/**
		 * Awaits and asserts the call, signals completion, and responds with the given value. Use
		 * when autoResponse is disabled.
		 */
		public void assertCall(T value, R response) {
			assertCall(value, t -> response);
		}

		/**
		 * Asserts and clears values. Can be used with or without autoResponse.
		 */
		@SafeVarargs
		public final void assertValues(T... values) {
			super.assertAndClearValues(values);
		}

		/**
		 * Awaits and asserts the call, evaluates the response action, and signals completion. Use
		 * when autoResponse is disabled.
		 */
		public <E extends Exception> void assertCall(T value, ExceptionFunction<E, T, R> responseFn)
			throws E {
			super.assertCallWithResponse(value, responseFn);
		}
	}

	/**
	 * Sub-class for a consumer call.
	 */
	public static class Accept<T> extends CallSync<T, Object> implements Consumer<T> {
		protected Accept(T valueDef) {
			super(valueDef);
		}

		/**
		 * Set the default value.
		 */
		public Accept<T> valueDef(T value) {
			super.valueDef(value);
			return this;
		}

		/**
		 * Returns the last call value, or default if not set.
		 */
		public T value() {
			return super.getValue();
		}

		/**
		 * Sets the call value without signaling.
		 */
		public Accept<T> value(T value) {
			super.setValue(value);
			return this;
		}

		/**
		 * Returns the call values set since creation or reset.
		 */
		public List<T> values() {
			return super.getValues();
		}

		/**
		 * Enables auto response.
		 */
		public Accept<T> autoResponse(boolean enabled) {
			super.autoResponseFn(enabled ? t -> null : null);
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		@Override
		public void accept(T t) {
			accept(t, RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void accept(T t, ExceptionAdapter<E> adapter) throws E {
			super.apply(t, adapter);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException or
		 * InterruptedException if the generator is configured.
		 */
		public void acceptWithInterrupt(T t) throws InterruptedException {
			acceptWithInterrupt(t, RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void acceptWithInterrupt(T t, ExceptionAdapter<E> adapter)
			throws InterruptedException, E {
			super.applyWithInterrupt(t, adapter);
		}

		/**
		 * Awaits the call without signaling completion. Use when autoResponse is enabled.
		 */
		public T awaitAuto() {
			return super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call and signals completion. Use when autoResponse is disabled.
		 */
		public T await() {
			return await(t -> {});
		}

		/**
		 * Awaits the call, executes the action, and signals completion. Use when autoResponse is
		 * disabled.
		 */
		public <E extends Exception> T await(ExceptionConsumer<E, T> actionFn) throws E {
			return super.awaitCallWithResponse(t -> exec(t, actionFn));
		}

		/**
		 * Awaits and asserts the call, without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void assertAuto(T value) {
			super.assertCallWithAutoResponse(value);
		}

		/**
		 * Awaits and asserts the call, and signals completion. Use when autoResponse is disabled.
		 */
		public void assertCall(T value) {
			assertCall(value, t -> {});
		}

		/**
		 * Awaits and asserts the call, executes the action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void assertCall(T value, ExceptionConsumer<E, T> actionFn)
			throws E {
			super.assertCallWithResponse(value, t -> exec(t, actionFn));
		}

		/**
		 * Asserts and clears values. Can be used with or without autoResponse.
		 */
		@SafeVarargs
		public final void assertValues(T... values) {
			super.assertAndClearValues(values);
		}

		private <E extends Exception> Object exec(T t, ExceptionConsumer<E, T> responseFn)
			throws E {
			responseFn.accept(t);
			return null;
		}
	}

	/**
	 * Sub-class for a supplier call.
	 */
	public static class Get<R> extends CallSync<Object, R> implements Supplier<R> {
		protected Get() {
			super(null);
		}

		/**
		 * Sets auto response values. The last value repeats.
		 */
		@SafeVarargs
		public final Get<R> autoResponses(R... responses) {
			if (responses.length == 0) return autoResponse(null);
			if (responses.length == 1) return autoResponse(() -> responses[0]);
			Counter counter = Counter.of();
			return autoResponse(() -> responses[Math.min(counter.intInc(), responses.length) - 1]);
		}

		/**
		 * Sets the auto response supplier. Use null to disable.
		 */
		public Get<R> autoResponse(Supplier<R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn != null ? t -> autoResponseFn.get() : null);
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		@Override
		public R get() {
			return get(RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R get(ExceptionAdapter<E> adapter) throws E {
			return super.apply(null, adapter);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException or InterruptedException if the generator is configured.
		 */
		public R getWithInterrupt() throws InterruptedException {
			return getWithInterrupt(RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws an
		 * exception if the generator is configured.
		 */
		public <E extends Exception> R getWithInterrupt(ExceptionAdapter<E> adapter)
			throws E, InterruptedException {
			return super.applyWithInterrupt(null, adapter);
		}

		/**
		 * Awaits the call, and responds without signaling completion. Use when autoResponse is
		 * enabled.
		 */
		public void awaitAuto() {
			super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call, signals completion, and responds with the given value. Use when
		 * autoResponse is disabled.
		 */
		public void await(R response) {
			await(() -> response);
		}

		/**
		 * Returns the number of calls made since creation or reset.
		 */
		public int calls() {
			return super.getValueCount();
		}

		/**
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void await(ExceptionSupplier<E, R> responseFn) throws E {
			super.awaitCallWithResponse(t -> responseFn.get());
		}
	}

	/**
	 * Sub-class for a runnable call.
	 */
	public static class Run extends CallSync<Object, Object> implements Runnable {
		protected Run() {
			super(null);
		}

		/**
		 * Enables/disables auto response.
		 */
		public Run autoResponse(boolean enabled) {
			super.autoResponseFn(enabled ? t -> null : null);
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		@Override
		public void run() {
			run(RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void run(ExceptionAdapter<E> adapter) throws E {
			super.apply(null, adapter);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException or
		 * InterruptedException if the generator is configured.
		 */
		public void runWithInterrupt() throws InterruptedException {
			runWithInterrupt(RUNTIME);
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws an exception if the
		 * generator is configured.
		 */
		public <E extends Exception> void runWithInterrupt(ExceptionAdapter<E> adapter)
			throws E, InterruptedException {
			super.applyWithInterrupt(null, adapter);
		}

		/**
		 * Awaits the call without signaling completion. Use when autoResponse is enabled.
		 */
		public void awaitAuto() {
			super.awaitCallWithAutoResponse();
		}

		/**
		 * Awaits the call and signals completion. Use when autoResponse is disabled.
		 */
		public void await() {
			await(() -> {});
		}

		/**
		 * Awaits the call, executes the action, and signals completion. Use when autoResponse is
		 * disabled.
		 */
		public <E extends Exception> void await(ExceptionRunnable<E> actionFn) throws E {
			super.awaitCallWithResponse(t -> exec(actionFn));
		}

		/**
		 * Returns the number of calls made since creation or reset.
		 */
		public int calls() {
			return super.getValueCount();
		}

		private <E extends Exception> Object exec(ExceptionRunnable<E> responseFn) throws E {
			responseFn.run();
			return null;
		}
	}

	protected CallSync(T valueDef) {
		valueDef(valueDef);
		reset();
	}

	/**
	 * Reset state. Does not change auto response or default value.
	 */
	public void reset() {
		execute(lock, () -> {
			values.clear();
			error.clear();
			callSync.clear();
			responseSync.clear();
		});
	}

	/**
	 * Asserts that no call was made.
	 */
	public void assertNoCall() {
		assertNull(callSync.value());
	}

	/**
	 * Enables/disables saving of values. Clears previous values if disabled. Enabled by default.
	 */
	public void saveValues(boolean enabled) {
		ConcurrentUtil.execute(lock, () -> {
			saveValues = enabled;
			if (!enabled && values.size() > 1) setValue(getValue());
		});
	}

	/**
	 * Prints internal state. Useful for debugging tests.
	 */
	@Override
	public String toString() {
		var callLock = lockInfo(callSync.lock);
		var responseLock = lockInfo(responseSync.lock);
		ToString s = ToString.ofClass(this);
		if (!ConcurrentUtil.tryExecute(lock, () -> s.children( //
			String.format("call=%s;%s", callSync.tryValue(), callLock),
			String.format("response=%s;%s;%s", responseSync.tryValue(), responseLock,
				lambdaName(autoResponseFn)),
			String.format("error=%s", error), String.format("values=%s;%s", values, valueDef)))) {
			// Unable to get lock
			s.children("call=[locked];" + callLock, "response=[locked];" + responseLock,
				"error=[locked]", "values=[locked]");
		}
		return s.toString();
	}

	/**
	 * Thread-safe; sets default value.
	 */
	private void valueDef(T value) {
		execute(lock, () -> valueDef = value);
	}

	/**
	 * Thread-safe; set current value.
	 */
	private void setValue(T value) {
		execute(lock, () -> {
			if (!saveValues) values.clear();
			values.add(value);
		});
	}

	/**
	 * Thread-safe; get current value or default.
	 */
	private T getValue() {
		return executeGet(lock, () -> getOrDefault(values, values.size() - 1, valueDef));
	}

	/**
	 * Thread-safe; get all values.
	 */
	private List<T> getValues() {
		return executeGet(lock, () -> new ArrayList<>(values));
	}

	/**
	 * Thread-safe; assert and clear values.
	 */
	@SafeVarargs
	private void assertAndClearValues(T... expecteds) {
		List<T> values = executeGet(lock, () -> {
			List<T> list = new ArrayList<>(this.values);
			this.values.clear();
			return list;
		});
		assertIterable(values, expecteds);
	}

	/**
	 * Thread-safe; returns number of values set.
	 */
	private int getValueCount() {
		return executeGet(lock, () -> values.size());
	}

	/**
	 * Thread-safe; sets auto-response function. Null to disable.
	 */
	private void autoResponseFn(Function<T, R> autoResponseFn) {
		execute(lock, () -> this.autoResponseFn = autoResponseFn);
	}

	/**
	 * Thread-safe; signals call, waits for completion / auto-response.
	 */
	private <E extends Exception> R apply(T value, ExceptionAdapter<E> adapter) throws E {
		lock.lock();
		try {
			R response = sync(value);
			error.call(adapter);
			setValue(value);
			return response;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread-safe; signals call, waits for completion / auto-response.
	 */
	private <E extends Exception> R applyWithInterrupt(T value, ExceptionAdapter<E> adapter)
		throws InterruptedException, E {
		lock.lock();
		try {
			R response = sync(value);
			error.callWithInterrupt(adapter);
			setValue(value);
			return response;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread-safe; checks auto-response is set, and awaits call.
	 */
	private T awaitCallWithAutoResponse() {
		return executeGet(lock, () -> {
			assertNotNull(autoResponseFn);
			return awaitCall();
		});
	}

	/**
	 * Thread-safe; awaits call, and signals completion. Auto-response is temporarily disabled.
	 */
	private <E extends Exception> T awaitCallWithResponse(ExceptionFunction<E, T, R> responseFn)
		throws E {
		return executeGet(lock, () -> {
			var autoResponseFn = this.autoResponseFn;
			try {
				// Temporarily removes auto response
				autoResponseFn(null);
				T t = awaitCall();
				responseSync.signal(Holder.of(responseFn.apply(t)));
				return t;
			} finally {
				autoResponseFn(autoResponseFn);
			}
		});
	}

	private void assertCallWithAutoResponse(T value) {
		assertEquals(awaitCallWithAutoResponse(), value);
	}

	private <E extends Exception> void assertCallWithResponse(T value,
		ExceptionFunction<E, T, R> responseFn) throws E {
		assertEquals(awaitCallWithResponse(responseFn), value);
	}

	private T awaitCall() {
		return executeGetInterruptible(callSync::await).value();
	}

	private R sync(T t) {
		callSync.signal(Holder.of(t));
		var autoResponseFn = this.autoResponseFn;
		if (autoResponseFn != null) return autoResponseFn.apply(t);
		return executeGetInterruptible(responseSync::await).value();
	}

}
