package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.threadCall;
import static ceri.common.test.TestUtil.threadRun;
import org.junit.Test;
import ceri.common.test.CallSync.Accept;
import ceri.common.test.CallSync.Apply;
import ceri.common.test.CallSync.Get;
import ceri.common.test.CallSync.Run;

public class CallSyncBehavior {

	@Test
	public void shouldApplyAndRespond() {
		Apply<String, Integer> call = CallSync.function(null);
		try (var exec = threadCall(() -> call.apply("test"))) {
			assertEquals(call.await(3), "test");
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyAndRespondWithInterrupt() {
		Apply<String, Integer> call = CallSync.function(null);
		try (var exec = threadCall(() -> call.applyWithInterrupt("test"))) {
			call.assertCall("test", 3);
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyWithAutoResponse() {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.assertNoCall();
		assertEquals(call.apply("test0"), 3);
		call.assertAuto("test0");
		assertEquals(call.apply("test1"), 3);
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldApplyWithDefaultValue() {
		Apply<String, Integer> call = CallSync.function("test", 3);
		assertEquals(call.value(), "test");
		call.valueDef("test0");
		assertEquals(call.value(), "test0");
		call.apply("test1");
		assertEquals(call.value(), "test1");
	}

	@Test
	public void shouldApplyValueWithoutSignal() {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.value("test");
		call.assertNoCall();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldApplyAndGetValues() {
		Apply<String, Integer> call = CallSync.function(null, 3);
		assertIterable(call.values());
		call.value("test0");
		call.value(null);
		call.apply("test2");
		call.apply(null);
		assertIterable(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldApplyAndAssertValues() {
		Apply<String, Integer> call = CallSync.function(null, 3);
		assertIterable(call.values());
		call.apply("test0");
		call.apply(null);
		call.apply("test1");
		call.assertValues("test0", null, "test1");
		assertIterable(call.values()); // cleared
	}

	@Test
	public void shouldApplyWithoutSavingValues() {
		Apply<String, Integer> call = CallSync.function(null, 3);
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
		Accept<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.accept("test"))) {
			assertEquals(call.await(), "test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptAndRespondWithInterrupt() {
		Accept<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.acceptWithInterrupt("test"))) {
			call.assertCall("test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptWithAutoResponse() {
		Accept<String> call = CallSync.consumer(null, true);
		call.assertNoCall();
		call.accept("test0");
		call.assertAuto("test0");
		call.accept("test1");
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldAcceptWithDefaultValue() {
		Accept<String> call = CallSync.consumer("test", true);
		assertEquals(call.value(), "test");
		call.valueDef("test0");
		assertEquals(call.value(), "test0");
		call.accept("test1");
		assertEquals(call.value(), "test1");
	}

	@Test
	public void shouldAcceptValueWithoutSignal() {
		Accept<String> call = CallSync.consumer(null, true);
		call.value("test");
		call.assertNoCall();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldAcceptAndGetValues() {
		Accept<String> call = CallSync.consumer(null, true);
		assertIterable(call.values());
		call.value("test0");
		call.value(null);
		call.accept("test2");
		call.accept(null);
		assertIterable(call.values(), "test0", null, "test2", null);
	}

	@Test
	public void shouldAcceptAndAssertValues() {
		Accept<String> call = CallSync.consumer(null, true);
		assertIterable(call.values());
		call.accept("test0");
		call.accept(null);
		call.accept("test1");
		call.assertValues("test0", null, "test1");
		assertIterable(call.values()); // cleared
	}

	@Test
	public void shouldGetAndRespond() {
		Get<String> call = CallSync.supplier();
		try (var exec = threadCall(() -> call.get())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetAndRespondWithInterrupt() {
		Get<String> call = CallSync.supplier();
		try (var exec = threadCall(() -> call.getWithInterrupt())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetWithAutoResponse() {
		Get<String> call = CallSync.supplier("test");
		call.assertNoCall();
		assertEquals(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithCallCount() {
		Get<String> call = CallSync.supplier("test");
		assertEquals(call.calls(), 0);
		call.get();
		call.get();
		call.get();
		assertEquals(call.calls(), 3);
	}

	@Test
	public void shouldRunAndRespond() {
		Run call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.run())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunAndRespondWithInterrupt() {
		Run call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.runWithInterrupt())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunWithAutoResponse() {
		Run call = CallSync.runnable(true);
		call.assertNoCall();
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldRunWithCallCount() {
		Run call = CallSync.runnable(true);
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
		CallSync.Get<String> tos = CallSync.supplier();
		try (var exec0 = threadRun(() -> tos.await(() -> {
			try (var exec1 = threadCall(() -> tos.toString())) { // locked
				return exec1.get();
			}
		}))) {
			assertTrue(tos.get().contains("locked"));
			exec0.get();
		}
	}

}
