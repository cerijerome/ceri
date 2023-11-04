package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.threadCall;
import static ceri.common.test.TestUtil.threadRun;
import org.junit.Test;
import ceri.common.test.CallSync.Consumer;
import ceri.common.test.CallSync.Function;
import ceri.common.test.CallSync.Runnable;
import ceri.common.test.CallSync.Supplier;

public class CallSyncBehavior {

	@Test
	public void shouldProvideFunctionLastValue() throws InterruptedException {
		var call = CallSync.function("test", 1, 2, 3);
		assertEquals(call.lastValue(), "test");
		assertEquals(call.lastValueWithInterrupt(), "test");
		call.apply("abc");
		assertEquals(call.lastValue(), "abc");
		assertEquals(call.lastValueWithInterrupt(), "abc");
	}

	@Test
	public void shouldProvideConsumerLastValue() throws InterruptedException {
		var call = CallSync.consumer("test", true);
		assertEquals(call.lastValue(), "test");
		assertEquals(call.lastValueWithInterrupt(), "test");
		call.accept("abc");
		assertEquals(call.lastValue(), "abc");
		assertEquals(call.lastValueWithInterrupt(), "abc");
	}

	@Test
	public void shouldResetFunctionToOriginalState() {
		var call = CallSync.function("", 1, 2, 3);
		assertEquals(call.value(), "");
		assertEquals(call.apply("1"), 1);
		assertEquals(call.apply("2"), 2);
		assertEquals(call.value(), "2");
		call.reset();
		assertEquals(call.value(), "");
		assertEquals(call.apply("1"), 1);
	}

	@Test
	public void shouldResetConsumerToOriginalState() {
		var call = CallSync.consumer("", true);
		assertEquals(call.value(), "");
		assertTrue(call.autoResponseEnabled());
		call.accept("1");
		call.autoResponse(false);
		assertEquals(call.value(), "1");
		assertFalse(call.autoResponseEnabled());
		call.reset();
		assertEquals(call.value(), "");
		assertTrue(call.autoResponseEnabled());
	}

	@Test
	public void shouldResetSupplierToOriginalState() {
		var call = CallSync.supplier(1, 2, 3);
		assertTrue(call.autoResponseEnabled());
		assertEquals(call.get(), 1);
		assertEquals(call.get(), 2);
		call.autoResponse(null);
		assertFalse(call.autoResponseEnabled());
		call.reset();
		assertEquals(call.get(), 1);
		assertTrue(call.autoResponseEnabled());
	}

	@Test
	public void shouldResetRunnableToOriginalState() {
		var call = CallSync.runnable(true);
		assertTrue(call.autoResponseEnabled());
		call.run();
		call.autoResponse(false);
		assertFalse(call.autoResponseEnabled());
		call.reset();
		assertTrue(call.autoResponseEnabled());
	}

	@Test
	public void shouldApplyAndRespond() {
		Function<String, Integer> call = CallSync.function(null);
		try (var exec = threadCall(() -> call.apply("test"))) {
			assertEquals(call.await(3), "test");
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyAndRespondWithInterrupt() {
		Function<String, Integer> call = CallSync.function(null);
		try (var exec = threadCall(() -> call.applyWithInterrupt("test"))) {
			call.assertCall("test", 3);
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyWithAutoResponse() {
		Function<String, Integer> call = CallSync.function(null, 3);
		call.assertNoCall();
		assertEquals(call.apply("test0"), 3);
		call.assertAuto("test0");
		assertEquals(call.apply("test1"), 3);
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldApplyWithAutoResponseFunction() {
		Function<String, Integer> call = CallSync.function(null);
		call.autoResponse(s -> {}, 3);
		assertEquals(call.apply("test0"), 3);
		call.assertAuto("test0");
	}

	@Test
	public void shouldApplyWithDefaultValue() {
		Function<String, Integer> call = CallSync.function("test", 3);
		assertEquals(call.value(), "test");
		call.valueDef("test0");
		assertEquals(call.value(), "test0");
		call.apply("test1");
		assertEquals(call.value(), "test1");
	}

	@Test
	public void shouldApplyValueWithoutSignal() {
		Function<String, Integer> call = CallSync.function(null, 3);
		call.value("test");
		call.assertCalls(0);
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldApplyAndGetValues() {
		Function<String, Integer> call = CallSync.function(null, 3);
		assertIterable(call.values());
		call.value("test0");
		call.value(null);
		call.apply("test2");
		call.apply(null);
		assertIterable(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldApplyAndAssertValues() {
		Function<String, Integer> call = CallSync.function(null, 3);
		assertIterable(call.values());
		call.apply("test0");
		call.apply(null);
		call.apply("test1");
		call.assertValues("test0", null, "test1");
		assertIterable(call.values()); // cleared
	}

	@Test
	public void shouldApplyWithoutSavingValues() {
		Function<String, Integer> call = CallSync.function(null, 3);
		call.saveValues(false);
		assertIterable(call.values());
		call.saveValues(true);
		call.apply("1");
		call.apply("2");
		call.saveValues(false);
		assertIterable(call.values(), "2");
		call.apply("3");
		assertIterable(call.values(), "3");
		call.saveValues(false);
		assertIterable(call.values(), "3");
	}

	@Test
	public void shouldAcceptAndRespond() {
		Consumer<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.accept("test"))) {
			assertEquals(call.await(), "test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptAndRespondWithInterrupt() {
		Consumer<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.acceptWithInterrupt("test"))) {
			call.assertCall("test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptWithAutoResponse() {
		Consumer<String> call = CallSync.consumer(null, true);
		call.assertNoCall();
		call.accept("test0");
		call.assertAuto("test0");
		call.accept("test1");
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldAcceptWithAutoResponseFunction() {
		Consumer<String> call = CallSync.consumer(null, false);
		call.autoResponse(s -> {});
		call.accept("test");
		call.assertAuto("test");
	}

	@Test
	public void shouldAcceptWithDefaultValue() {
		Consumer<String> call = CallSync.consumer("test", true);
		assertEquals(call.value(), "test");
		call.valueDef("test0");
		assertEquals(call.value(), "test0");
		call.accept("test1");
		assertEquals(call.value(), "test1");
	}

	@Test
	public void shouldAcceptValueWithoutSignal() {
		Consumer<String> call = CallSync.consumer(null, true);
		call.value("test");
		call.assertCalls(0);
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldAcceptAndGetValues() {
		Consumer<String> call = CallSync.consumer(null, true);
		assertIterable(call.values());
		call.value("test0");
		call.value(null);
		call.accept("test2");
		call.accept(null);
		assertIterable(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldAcceptAndAssertValues() {
		Consumer<String> call = CallSync.consumer(null, true);
		assertIterable(call.values());
		call.accept("test0");
		call.accept(null);
		call.accept("test1");
		call.assertValues("test0", null, "test1");
		assertIterable(call.values()); // cleared
	}

	@Test
	public void shouldGetAndRespond() {
		Supplier<String> call = CallSync.supplier();
		try (var exec = threadCall(() -> call.get())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetAndRespondWithInterrupt() {
		Supplier<String> call = CallSync.supplier();
		try (var exec = threadCall(() -> call.getWithInterrupt())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetWithAutoResponse() {
		Supplier<String> call = CallSync.supplier("test");
		call.assertCalls(0);
		assertEquals(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithAutoResponseFunction() {
		Supplier<String> call = CallSync.supplier("");
		call.autoResponse(() -> {}, "test");
		assertEquals(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithCallCount() {
		Supplier<String> call = CallSync.supplier("test");
		assertEquals(call.calls(), 0);
		call.get();
		call.get();
		call.get();
		assertEquals(call.calls(), 3);
	}

	@Test
	public void shouldRunAndRespond() {
		Runnable call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.run())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunAndRespondWithInterrupt() {
		Runnable call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.runWithInterrupt())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunWithAutoResponse() {
		Runnable call = CallSync.runnable(true);
		call.assertCalls(0);
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldRunWithAutoResponseFunction() {
		Runnable call = CallSync.runnable(false);
		call.autoResponse(() -> {});
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldRunWithCallCount() {
		Runnable call = CallSync.runnable(true);
		assertEquals(call.calls(), 0);
		call.run();
		call.run();
		call.run();
		assertEquals(call.calls(), 3);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertTrue(CallSync.consumer("test", true).toString().length() > 0);
		assertTrue(CallSync.runnable(true).toString().length() > 0);
	}

	@Test
	public void shouldProvideStringRepresentationEvenIfLocked() {
		CallSync.Supplier<String> tos = CallSync.supplier();
		try (var exec0 = threadRun(() -> tos.await(() -> {
			try (var exec1 = threadCall(() -> tos.toString())) { // locked
				return exec1.get();
			}
		}))) {
			assertTrue(tos.get().contains("locked"));
			exec0.get();
		}
	}

	@Test
	public void shouldProvideCompactStringRepresentation() {
		assertTrue(CallSync.consumer("test", false).compactString().length() > 0);
		assertTrue(CallSync.runnable(true).compactString().length() > 0);
	}

	@Test
	public void shouldProvideCompactStringRepresentationEvenIfLocked() {
		CallSync.Supplier<String> tos = CallSync.supplier();
		try (var exec0 = threadRun(() -> tos.await(() -> {
			try (var exec1 = threadCall(() -> tos.compactString())) { // locked
				return exec1.get();
			}
		}))) {
			assertTrue(tos.get().contains("locked"));
			exec0.get();
		}
	}

}
