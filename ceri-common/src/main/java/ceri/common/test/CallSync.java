package ceri.common.test;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.collect.Collectable;
import ceri.common.collect.Lists;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.ValueCondition;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functional;
import ceri.common.function.Functions;
import ceri.common.text.ToString;
import ceri.common.util.Holder;

/**
 * Thread-safe utility for synchronizing calls from a thread, and generating exceptions. If
 * autoResponse is set, the thread continues when the sync call is made. If not, the thread waits
 * for the response condition to be signaled; either by awaitCall or assertCall. This ensures no
 * calls are missed by the test class.
 */
public abstract class CallSync<T, R> {
	private static final boolean SAVE_VALUES_DEF = true;
	private final Lock lock = new ReentrantLock();
	public final ErrorGen error = ErrorGen.of();
	private final ValueCondition<Holder<T>> callSync = ValueCondition.of(lock);
	private final ValueCondition<Holder<R>> responseSync = ValueCondition.of(lock);
	private final List<T> values = Lists.of();
	private final T originalValueDef;
	private final Functions.Supplier<Excepts.Function<?, T, R>> origAutoResponseSupplier;
	private T valueDef;
	private Excepts.Function<?, T, R> autoResponseFn = null; // can be stateful
	private int calls = 0;
	private boolean saveValues = SAVE_VALUES_DEF;

	/**
	 * Resets all the given instances.
	 */
	public static void resetAll(CallSync<?, ?>... callSyncs) {
		for (var callSync : callSyncs)
			callSync.reset();
	}

	/**
	 * Disable/enable saving of values for all the given instances.
	 */
	public static void saveValuesAll(boolean enabled, CallSync<?, ?>... callSyncs) {
		for (var callSync : callSyncs)
			callSync.saveValues(enabled);
	}

	/**
	 * Creates a synchronizing function.
	 */
	@SafeVarargs
	public static <T, R> Function<T, R> function(T valueDef, R... autoResponses) {
		return new Function<>(valueDef, autoResponses);
	}

	/**
	 * Creates a synchronizing consumer.
	 */
	public static <T> Consumer<T> consumer(T valueDef, boolean autoResponse) {
		return new Consumer<>(valueDef, autoResponse);
	}

	/**
	 * Creates a synchronizing supplier.
	 */
	@SafeVarargs
	public static <R> Supplier<R> supplier(R... autoResponses) {
		return new Supplier<>(autoResponses);
	}

	/**
	 * Creates a synchronizing runnable.
	 */
	public static Runnable runnable(boolean autoResponse) {
		return new Runnable(autoResponse);
	}

	/**
	 * Sub-class for a function call.
	 */
	public static class Function<T, R> extends CallSync<T, R> implements Functions.Function<T, R> {

		protected Function(T valueDef, R[] autoResponses) {
			super(valueDef, () -> toAutoResponseFn(autoResponses));
		}

		/**
		 * Set the default value.
		 */
		public Function<T, R> valueDef(T value) {
			super.valueDef(value);
			return this;
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public T lastValue() {
			return lastValue(ExceptionAdapter.runtime);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public <E extends Exception> T lastValue(ExceptionAdapter<E> adapter) throws E {
			return super.lastValue(adapter);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public T lastValueWithInterrupt() throws InterruptedException {
			return lastValueWithInterrupt(ExceptionAdapter.runtime);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public <E extends Exception> T lastValueWithInterrupt(ExceptionAdapter<E> adapter)
			throws InterruptedException, E {
			return super.lastValueWithInterrupt(adapter);
		}

		/**
		 * Returns the last call value, or default if not set. Does not check for exceptions.
		 */
		public T value() {
			return super.getValue();
		}

		/**
		 * Sets the call value without signaling.
		 */
		public Function<T, R> value(T value) {
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
		public final Function<T, R> autoResponses(R... responses) {
			return autoResponse(toAutoResponseFn(responses));
		}

		/**
		 * Sets the auto response function. Use null to disable.
		 */
		public Function<T, R> autoResponse(Excepts.Function<?, T, R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn);
			return this;
		}

		/**
		 * Enables auto response with value consumer and fixed response.
		 */
		public Function<T, R> autoResponse(Excepts.Consumer<?, T> consumer, R response) {
			return autoResponse(toAutoResponseFn(consumer, response));
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		@Override
		public R apply(T t) {
			return apply(t, ExceptionAdapter.runtime);
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
			return applyWithInterrupt(t, ExceptionAdapter.runtime);
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
			return await(_ -> response);
		}

		/**
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> T await(Excepts.Function<E, T, R> responseFn) throws E {
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
			assertCall(value, _ -> response);
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
		public <E extends Exception> void assertCall(T value, Excepts.Function<E, T, R> responseFn)
			throws E {
			super.assertCallWithResponse(value, responseFn);
		}
	}

	/**
	 * Sub-class for a consumer call.
	 */
	public static class Consumer<T> extends CallSync<T, Object> implements Functions.Consumer<T> {

		protected Consumer(T valueDef, boolean autoResponse) {
			super(valueDef, () -> toAutoResponseFn(autoResponse));
		}

		/**
		 * Set the default value.
		 */
		public Consumer<T> valueDef(T value) {
			super.valueDef(value);
			return this;
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public T lastValue() {
			return lastValue(ExceptionAdapter.runtime);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public <E extends Exception> T lastValue(ExceptionAdapter<E> adapter) throws E {
			return super.lastValue(adapter);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public T lastValueWithInterrupt() throws InterruptedException {
			return lastValueWithInterrupt(ExceptionAdapter.runtime);
		}

		/**
		 * Returns the last call value, or default if not set. Checks for exceptions.
		 */
		public <E extends Exception> T lastValueWithInterrupt(ExceptionAdapter<E> adapter)
			throws InterruptedException, E {
			return super.lastValueWithInterrupt(adapter);
		}

		/**
		 * Returns the last call value, or default if not set. Does not check for exceptions.
		 */
		public T value() {
			return super.getValue();
		}

		/**
		 * Sets the call value without signaling. Does not check for exceptions.
		 */
		public Consumer<T> value(T value) {
			super.setValue(value);
			return this;
		}

		/**
		 * Returns the call values set since creation or reset. Does not check for exceptions.
		 */
		public List<T> values() {
			return super.getValues();
		}

		/**
		 * Enables/disables auto response.
		 */
		public Consumer<T> autoResponse(boolean enabled) {
			super.autoResponseFn(toAutoResponseFn(enabled));
			return this;
		}

		/**
		 * Enables auto response with value consumer when called.
		 */
		public Consumer<T> autoResponse(Excepts.Consumer<?, T> consumer) {
			super.autoResponseFn(toAutoResponseFn(consumer, null));
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		@Override
		public void accept(T t) {
			accept(t, ExceptionAdapter.runtime);
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
			acceptWithInterrupt(t, ExceptionAdapter.runtime);
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
			return await(_ -> {});
		}

		/**
		 * Awaits the call, executes the action, and signals completion. Use when autoResponse is
		 * disabled.
		 */
		public <E extends Exception> T await(Excepts.Consumer<E, T> actionFn) throws E {
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
			assertCall(value, _ -> {});
		}

		/**
		 * Awaits and asserts the call, executes the action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void assertCall(T value, Excepts.Consumer<E, T> actionFn)
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

		private <E extends Exception> Object exec(T t, Excepts.Consumer<E, T> responseFn) throws E {
			responseFn.accept(t);
			return null;
		}
	}

	/**
	 * Sub-class for a supplier call.
	 */
	public static class Supplier<R> extends CallSync<Object, R> implements Functions.Supplier<R> {

		protected Supplier(R[] autoResponses) {
			super(null, () -> toAutoResponseFn(autoResponses));
		}

		/**
		 * Sets auto response values. The last value repeats.
		 */
		@SafeVarargs
		public final Supplier<R> autoResponses(R... responses) {
			super.autoResponseFn(toAutoResponseFn(responses));
			return this;
		}

		/**
		 * Sets the auto response supplier. Use null to disable.
		 */
		public Supplier<R> autoResponse(Functions.Supplier<R> autoResponseFn) {
			super.autoResponseFn(autoResponseFn != null ? _ -> autoResponseFn.get() : null);
			return this;
		}

		/**
		 * Enables auto response with runnable and fixed response.
		 */
		public Supplier<R> autoResponse(java.lang.Runnable runnable, R response) {
			super.autoResponseFn(toAutoResponseFn(runnable, response));
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for the completion response. Throws a
		 * RuntimeException if the generator is configured.
		 */
		@Override
		public R get() {
			return get(ExceptionAdapter.runtime);
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
			return getWithInterrupt(ExceptionAdapter.runtime);
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
		 * Awaits the call, evaluates the response action, and signals completion. Use when
		 * autoResponse is disabled.
		 */
		public <E extends Exception> void await(Excepts.Supplier<E, R> responseFn) throws E {
			super.awaitCallWithResponse(_ -> responseFn.get());
		}
	}

	/**
	 * Sub-class for a runnable call.
	 */
	public static class Runnable extends CallSync<Object, Object> implements java.lang.Runnable {

		protected Runnable(boolean autoResponse) {
			super(null, () -> toAutoResponseFn(autoResponse));
		}

		/**
		 * Enables/disables auto response.
		 */
		public Runnable autoResponse(boolean enabled) {
			super.autoResponseFn(toAutoResponseFn(enabled));
			return this;
		}

		/**
		 * Enables auto response with runnable to execute when called.
		 */
		public Runnable autoResponse(java.lang.Runnable runnable) {
			super.autoResponseFn(toAutoResponseFn(runnable, null));
			return this;
		}

		/**
		 * Signals that a call has been made, and waits for completion. Throws a RuntimeException if
		 * the generator is configured.
		 */
		@Override
		public void run() {
			run(ExceptionAdapter.runtime);
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
			runWithInterrupt(ExceptionAdapter.runtime);
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
		public <E extends Exception> void await(Excepts.Runnable<E> actionFn) throws E {
			super.awaitCallWithResponse(_ -> exec(actionFn));
		}

		private <E extends Exception> Object exec(Excepts.Runnable<E> responseFn) throws E {
			responseFn.run();
			return null;
		}
	}

	protected CallSync(T valueDef,
		Functions.Supplier<Excepts.Function<?, T, R>> autoResponseSupplier) {
		originalValueDef = valueDef;
		this.valueDef = valueDef;
		origAutoResponseSupplier = autoResponseSupplier;
		this.autoResponseFn = autoResponseSupplier.get();
	}

	/**
	 * Reset state, which includes setting the original default value and auto-response.
	 */
	public void reset() {
		Concurrent.lockedRun(lock, () -> {
			error.clear();
			callSync.clear();
			responseSync.clear();
			autoResponseFn = origAutoResponseSupplier.get();
			saveValues = SAVE_VALUES_DEF;
			clearCalls();
		});
	}

	/**
	 * Clears values and call count.
	 */
	public void clearCalls() {
		Concurrent.lockedRun(lock, () -> {
			values.clear();
			valueDef = originalValueDef;
			calls = 0;
		});
	}

	/**
	 * Returns true if auto-response is enabled.
	 */
	public boolean autoResponseEnabled() {
		return Concurrent.lockedGet(lock, () -> autoResponseFn) != null;
	}

	/**
	 * Thread-safe; get call count since creation or reset.
	 */
	public int calls() {
		return Concurrent.lockedGet(lock, () -> calls);
	}

	/**
	 * Asserts the number of calls made since construction/reset.
	 */
	public void assertCalls(int calls) {
		assertEquals(calls(), calls);
	}

	/**
	 * Asserts that no call was made.
	 */
	public void assertNoCall() {
		assertEquals(callSync.value(), null);
	}

	/**
	 * Enables/disables saving of values. Clears previous values if disabled. Enabled by default.
	 */
	public void saveValues(boolean enabled) {
		Concurrent.lockedRun(lock, () -> {
			saveValues = enabled;
			if (!enabled && values.size() > 1) setValue(getValue());
		});
	}

	/**
	 * Prints internal state. Useful for debugging tests.
	 */
	@Override
	public String toString() {
		var callLock = Concurrent.lockInfo(callSync.lock);
		var responseLock = Concurrent.lockInfo(responseSync.lock);
		ToString s = ToString.ofClass(this).field("error", error);
		if (!Concurrent.tryLockedRun(lock,
			() -> s.children(String.format("call=%s;%s;%d", callSync.tryValue(), callLock, calls),
				String.format("response=%s;%s;%s", responseSync.tryValue(), responseLock,
					responseString()),
				String.format("values=%s;%s", values, valueDef)))) {
			// Lock failed; unable to safely read fields
			s.children("call=[locked];" + callLock, "response=[locked];" + responseLock,
				"values=[locked]");
		}
		return s.toString();
	}

	/**
	 * Prints compact internal state.
	 */
	public String compactString() {
		ToString s = ToString.ofClass(this).field("error", error);
		if (!Concurrent.tryLockedRun(lock,
			() -> s.fields("calls", calls, "response", responseString(), "values",
				values + ";" + valueDef)))
			s.fields("calls", "[locked]", "response", "[locked]", "values", "[locked]");
		return s.field("error", error).toString();
	}

	private String responseString() {
		return autoResponseFn == null ? "manual" : "auto";
	}

	/**
	 * Thread-safe; sets default value.
	 */
	private void valueDef(T value) {
		Concurrent.lockedRun(lock, () -> valueDef = value);
	}

	/**
	 * Thread-safe; set current value.
	 */
	private void setValue(T value) {
		Concurrent.lockedRun(lock, () -> {
			if (!saveValues) values.clear();
			values.add(value);
		});
	}

	/**
	 * Thread-safe; get current value or default. Checks for exceptions.
	 */
	private <E extends Exception> T lastValue(ExceptionAdapter<E> adapter) throws E {
		error.call(adapter);
		return getValue();
	}

	/**
	 * Thread-safe; get current value or default. Checks for exceptions.
	 */
	private <E extends Exception> T lastValueWithInterrupt(ExceptionAdapter<E> adapter)
		throws InterruptedException, E {
		error.callWithInterrupt(adapter);
		return getValue();
	}

	/**
	 * Thread-safe; get current value or default. Does not check for exceptions.
	 */
	private T getValue() {
		return Concurrent.lockedGet(lock, () -> Lists.last(values, valueDef));
	}

	/**
	 * Thread-safe; get all values. Does not check for exceptions.
	 */
	private List<T> getValues() {
		return Concurrent.lockedGet(lock, () -> Collectable.add(Lists.of(), values));
	}

	/**
	 * Thread-safe; assert and clear values.
	 */
	@SafeVarargs
	private void assertAndClearValues(T... expecteds) {
		List<T> values = Concurrent.lockedGet(lock, () -> {
			var list = Lists.of(this.values);
			this.values.clear();
			return list;
		});
		assertOrdered(values, expecteds);
	}

	/**
	 * Thread-safe; sets auto-response function. Null to disable.
	 */
	private void autoResponseFn(Excepts.Function<?, T, R> autoResponseFn) {
		Concurrent.lockedRun(lock, () -> this.autoResponseFn = autoResponseFn);
	}

	/**
	 * Thread-safe; signals call, waits for completion / auto-response.
	 */
	private <E extends Exception> R apply(T value, ExceptionAdapter<E> adapter) throws E {
		lock.lock();
		try {
			calls++;
			R response = adapter.get(() -> sync(value));
			error.call(adapter);
			setValue(value);
			return response;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw adapter.apply(e);
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
			calls++;
			R response = adapter.get(() -> sync(value));
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
		return Concurrent.lockedGet(lock, () -> {
			Assert.notNull(autoResponseFn);
			return awaitCall();
		});
	}

	/**
	 * Thread-safe; awaits call, and signals completion. Auto-response is temporarily disabled.
	 */
	private <E extends Exception> T awaitCallWithResponse(Excepts.Function<E, T, R> responseFn)
		throws E {
		return Concurrent.lockedGet(lock, () -> {
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
		Excepts.Function<E, T, R> responseFn) throws E {
		assertEquals(awaitCallWithResponse(responseFn), value);
	}

	private T awaitCall() {
		return Concurrent.getInterruptible(callSync::await).value();
	}

	private R sync(T t) throws Exception {
		callSync.signal(Holder.of(t));
		var autoResponseFn = this.autoResponseFn;
		if (autoResponseFn != null) return autoResponseFn.apply(t);
		return Concurrent.getInterruptible(responseSync::await).value();
	}

	private static <T, R> Excepts.Function<?, T, R>
		toAutoResponseFn(Excepts.Consumer<?, T> consumer, R response) {
		return t -> {
			if (consumer != null) consumer.accept(t);
			return response;
		};
	}

	private static <T, R> Excepts.Function<?, T, R> toAutoResponseFn(java.lang.Runnable runnable,
		R response) {
		return _ -> {
			if (runnable != null) runnable.run();
			return response;
		};
	}

	private static <T, R> Excepts.Function<?, T, R> toAutoResponseFn(boolean enabled) {
		return enabled ? _ -> null : null;
	}

	private static <T, R> Excepts.Function<?, T, R> toAutoResponseFn(R[] responses) {
		if (responses.length == 0) return null;
		var supplier = Functional.sequentialSupplier(responses);
		return _ -> supplier.get();
	}
}
