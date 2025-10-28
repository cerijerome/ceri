package ceri.common.test;

import org.junit.Test;

public class CallSyncBehavior {

	@Test
	public void shouldSetSaveValues() {
		var call0 = CallSync.function("test", 1, 2, 3);
		var call1 = CallSync.supplier("test");
		CallSync.saveValuesAll(true, call0, call1);
		call0.apply("1");
		call0.apply("2");
		Assert.ordered(call0.values(), "1", "2");
		CallSync.saveValuesAll(false, call0, call1);
		Assert.ordered(call0.values(), "2");
	}

	@Test
	public void shouldProvideFunctionLastValue() throws InterruptedException {
		var call = CallSync.function("test", 1, 2, 3);
		Assert.equal(call.lastValue(), "test");
		Assert.equal(call.lastValueWithInterrupt(), "test");
		call.apply("abc");
		Assert.equal(call.lastValue(), "abc");
		Assert.equal(call.lastValueWithInterrupt(), "abc");
	}

	@Test
	public void shouldProvideConsumerLastValue() throws InterruptedException {
		var call = CallSync.consumer("test", true);
		Assert.equal(call.lastValue(), "test");
		Assert.equal(call.lastValueWithInterrupt(), "test");
		call.accept("abc");
		Assert.equal(call.lastValue(), "abc");
		Assert.equal(call.lastValueWithInterrupt(), "abc");
	}

	@Test
	public void shouldResetFunctionToOriginalState() {
		var call = CallSync.function("", 1, 2, 3);
		Assert.equal(call.value(), "");
		Assert.equal(call.apply("1"), 1);
		Assert.equal(call.apply("2"), 2);
		Assert.equal(call.value(), "2");
		call.reset();
		Assert.equal(call.value(), "");
		Assert.equal(call.apply("1"), 1);
	}

	@Test
	public void shouldResetConsumerToOriginalState() {
		var call = CallSync.consumer("", true);
		Assert.equal(call.value(), "");
		Assert.yes(call.autoResponseEnabled());
		call.accept("1");
		call.autoResponse(false);
		Assert.equal(call.value(), "1");
		Assert.no(call.autoResponseEnabled());
		call.reset();
		Assert.equal(call.value(), "");
		Assert.yes(call.autoResponseEnabled());
	}

	@Test
	public void shouldResetSupplierToOriginalState() {
		var call = CallSync.supplier(1, 2, 3);
		Assert.yes(call.autoResponseEnabled());
		Assert.equal(call.get(), 1);
		Assert.equal(call.get(), 2);
		call.autoResponse(null);
		Assert.no(call.autoResponseEnabled());
		call.reset();
		Assert.equal(call.get(), 1);
		Assert.yes(call.autoResponseEnabled());
	}

	@Test
	public void shouldResetRunnableToOriginalState() {
		var call = CallSync.runnable(true);
		Assert.yes(call.autoResponseEnabled());
		call.run();
		call.autoResponse(false);
		Assert.no(call.autoResponseEnabled());
		call.reset();
		Assert.yes(call.autoResponseEnabled());
	}

	@Test
	public void shouldApplyAndRespond() {
		var call = CallSync.<String, Integer>function(null);
		try (var exec = Testing.threadCall(() -> call.apply("test"))) {
			Assert.equal(call.await(3), "test");
			Assert.equal(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyAndRespondWithInterrupt() {
		var call = CallSync.<String, Integer>function(null);
		try (var exec = Testing.threadCall(() -> call.applyWithInterrupt("test"))) {
			call.assertCall("test", 3);
			Assert.equal(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyWithAutoResponse() {
		var call = CallSync.<String, Integer>function(null, 3);
		call.assertNoCall();
		Assert.equal(call.apply("test0"), 3);
		call.assertAuto("test0");
		Assert.equal(call.apply("test1"), 3);
		Assert.equal(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldApplyWithAutoResponseFunction() {
		var call = CallSync.<String, Integer>function(null);
		call.autoResponse(_ -> {}, 3);
		Assert.equal(call.apply("test0"), 3);
		call.assertAuto("test0");
	}

	@Test
	public void shouldApplyWithDefaultValue() {
		var call = CallSync.function("test", 3);
		Assert.equal(call.value(), "test");
		call.valueDef("test0");
		Assert.equal(call.value(), "test0");
		call.apply("test1");
		Assert.equal(call.value(), "test1");
	}

	@Test
	public void shouldApplyValueWithoutSignal() {
		var call = CallSync.<String, Integer>function(null, 3);
		call.value("test");
		call.assertCalls(0);
		Assert.equal(call.value(), "test");
	}

	@Test
	public void shouldApplyAndGetValues() {
		var call = CallSync.<String, Integer>function(null, 3);
		Assert.ordered(call.values());
		call.value("test0");
		call.value(null);
		call.apply("test2");
		call.apply(null);
		Assert.ordered(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldApplyAndAssertValues() {
		var call = CallSync.<String, Integer>function(null, 3);
		Assert.ordered(call.values());
		call.apply("test0");
		call.apply(null);
		call.apply("test1");
		call.assertValues("test0", null, "test1");
		Assert.ordered(call.values()); // cleared
	}

	@Test
	public void shouldApplyWithoutSavingValues() {
		var call = CallSync.<String, Integer>function(null, 3);
		call.saveValues(false);
		Assert.ordered(call.values());
		call.saveValues(true);
		call.apply("1");
		call.apply("2");
		call.saveValues(false);
		Assert.ordered(call.values(), "2");
		call.apply("3");
		Assert.ordered(call.values(), "3");
		call.saveValues(false);
		Assert.ordered(call.values(), "3");
	}

	@Test
	public void shouldAcceptAndRespond() {
		var call = CallSync.<String>consumer(null, false);
		try (var exec = Testing.threadRun(() -> call.accept("test"))) {
			Assert.equal(call.await(), "test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptAndRespondWithInterrupt() {
		var call = CallSync.<String>consumer(null, false);
		try (var exec = Testing.threadRun(() -> call.acceptWithInterrupt("test"))) {
			call.assertCall("test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptWithAutoResponse() {
		var call = CallSync.<String>consumer(null, true);
		call.assertNoCall();
		call.accept("test0");
		call.assertAuto("test0");
		call.accept("test1");
		Assert.equal(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldAcceptWithAutoResponseFunction() {
		var call = CallSync.<String>consumer(null, false);
		call.autoResponse(_ -> {});
		call.accept("test");
		call.assertAuto("test");
	}

	@Test
	public void shouldAcceptWithDefaultValue() {
		var call = CallSync.consumer("test", true);
		Assert.equal(call.value(), "test");
		call.valueDef("test0");
		Assert.equal(call.value(), "test0");
		call.accept("test1");
		Assert.equal(call.value(), "test1");
	}

	@Test
	public void shouldAcceptValueWithoutSignal() {
		var call = CallSync.<String>consumer(null, true);
		call.value("test");
		call.assertCalls(0);
		Assert.equal(call.value(), "test");
	}

	@Test
	public void shouldAcceptAndGetValues() {
		var call = CallSync.<String>consumer(null, true);
		Assert.ordered(call.values());
		call.value("test0");
		call.value(null);
		call.accept("test2");
		call.accept(null);
		Assert.ordered(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldAcceptAndAssertValues() {
		var call = CallSync.<String>consumer(null, true);
		Assert.ordered(call.values());
		call.accept("test0");
		call.accept(null);
		call.accept("test1");
		call.assertValues("test0", null, "test1");
		Assert.ordered(call.values()); // cleared
	}

	@Test
	public void shouldGetAndRespond() {
		var call = CallSync.<String>supplier();
		try (var exec = Testing.threadCall(() -> call.get())) {
			call.await("test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetAndRespondWithInterrupt() {
		var call = CallSync.<String>supplier();
		try (var exec = Testing.threadCall(() -> call.getWithInterrupt())) {
			call.await("test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetWithAutoResponse() {
		var call = CallSync.supplier("test");
		call.assertCalls(0);
		Assert.equal(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithAutoResponseFunction() {
		var call = CallSync.supplier("");
		call.autoResponse(() -> {}, "test");
		Assert.equal(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithCallCount() {
		var call = CallSync.supplier("test");
		Assert.equal(call.calls(), 0);
		call.get();
		call.get();
		call.get();
		Assert.equal(call.calls(), 3);
	}

	@Test
	public void shouldRunAndRespond() {
		var call = CallSync.runnable(false);
		try (var exec = Testing.threadRun(() -> call.run())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunAndRespondWithInterrupt() {
		var call = CallSync.runnable(false);
		try (var exec = Testing.threadRun(() -> call.runWithInterrupt())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunWithAutoResponse() {
		var call = CallSync.runnable(true);
		call.assertCalls(0);
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldRunWithAutoResponseFunction() {
		var call = CallSync.runnable(false);
		call.autoResponse(() -> {});
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldRunWithCallCount() {
		var call = CallSync.runnable(true);
		Assert.equal(call.calls(), 0);
		call.run();
		call.run();
		call.run();
		Assert.equal(call.calls(), 3);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.yes(CallSync.consumer("test", true).toString().length() > 0);
		Assert.yes(CallSync.runnable(true).toString().length() > 0);
	}

	@Test
	public void shouldProvideStringRepresentationEvenIfLocked() {
		var tos = CallSync.<String>supplier();
		try (var exec0 = Testing.threadRun(() -> tos.await(() -> {
			try (var exec1 = Testing.threadCall(() -> tos.toString())) { // locked
				return exec1.get();
			}
		}))) {
			Assert.yes(tos.get().contains("locked"));
			exec0.get();
		}
	}

	@Test
	public void shouldProvideCompactStringRepresentation() {
		Assert.yes(CallSync.consumer("test", false).compactString().length() > 0);
		Assert.yes(CallSync.runnable(true).compactString().length() > 0);
	}

	@Test
	public void shouldProvideCompactStringRepresentationEvenIfLocked() {
		var tos = CallSync.<String>supplier();
		try (var exec0 = Testing.threadRun(() -> tos.await(() -> {
			try (var exec1 = Testing.threadCall(() -> tos.compactString())) { // locked
				return exec1.get();
			}
		}))) {
			Assert.yes(tos.get().contains("locked"));
			exec0.get();
		}
	}
}
