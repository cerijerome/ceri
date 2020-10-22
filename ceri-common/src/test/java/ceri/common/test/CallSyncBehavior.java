package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.threadCall;
import static ceri.common.test.TestUtil.threadRun;
import org.junit.Test;
import ceri.common.test.CallSync.Accept;
import ceri.common.test.CallSync.Apply;
import ceri.common.test.CallSync.Get;
import ceri.common.test.CallSync.Run;
import ceri.common.test.ErrorGen.Mode;

public class CallSyncBehavior {

	@Test
	public void shouldApplyAndRespond() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, null);
		try (var exec = threadCall(() -> call.apply("test"))) {
			assertEquals(call.await(3), "test");
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyAndRespondWithInterrupt() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, null);
		try (var exec = threadCall(() -> call.applyWithInterrupt("test"))) {
			call.assertCall("test", 3);
			assertEquals(exec.get(), 3);
		}
	}

	@Test
	public void shouldApplyWithAutoResponse() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.assertNoCall();
		assertEquals(call.apply("test0"), 3);
		call.assertAuto("test0");
		assertEquals(call.apply("test1"), 3);
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldApplyWithExceptionMode() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.error(Mode.checked);
		assertThrown(() -> call.apply("test0"));
		call.assertAuto("test0");
		assertThrown(() -> call.apply("test1"));
		call.assertAuto("test1");
	}

	@Test
	public void shouldApplyWithExceptions() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.errors(Mode.checked, Mode.none, Mode.checked);
		assertThrown(() -> call.apply("test0"));
		call.assertAuto("test0");
		assertEquals(call.apply("test1"), 3);
		call.assertAuto("test1");
		assertThrown(() -> call.apply("test2"));
		call.assertAuto("test2");
		assertEquals(call.apply("test3"), 3);
		call.assertAuto("test3");
	}

	@Test
	public void shouldApplyWithExceptionFunction() throws InterruptedException {
		Apply<String, Integer> call = CallSync.function(null, 5);
		call.error((s, i) -> s.length() == i ? Mode.checked : Mode.none);
		assertEquals(call.apply("test"), 5);
		call.assertAuto("test");
		assertThrown(() -> call.apply("test0"));
		call.assertAuto("test0");
	}

	@Test
	public void shouldApplyWithDefaultValue() {
		Apply<String, Integer> call = CallSync.function("test", 3);
		assertEquals(call.value(), "test");
		call.apply("test0");
		assertEquals(call.value(), "test0");
		call.reset();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldApplyValueWithoutSignal() {
		Apply<String, Integer> call = CallSync.function(null, 3);
		call.value("test");
		call.assertNoCall();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldAcceptAndRespond() throws InterruptedException {
		Accept<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.accept("test"))) {
			assertEquals(call.await(), "test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptAndRespondWithInterrupt() throws InterruptedException {
		Accept<String> call = CallSync.consumer(null, false);
		try (var exec = threadRun(() -> call.acceptWithInterrupt("test"))) {
			call.assertCall("test");
			exec.get();
		}
	}

	@Test
	public void shouldAcceptWithAutoResponse() throws InterruptedException {
		Accept<String> call = CallSync.consumer(null, true);
		call.assertNoCall();
		call.accept("test0");
		call.assertAuto("test0");
		call.accept("test1");
		assertEquals(call.awaitAuto(), "test1");
	}

	@Test
	public void shouldAcceptWithExceptionFunction() throws InterruptedException {
		Accept<String> call = CallSync.consumer(null, true);
		call.error(s -> s.length() == 5 ? Mode.checked : Mode.none);
		call.accept("test");
		call.assertAuto("test");
		assertThrown(() -> call.accept("test0"));
		call.assertAuto("test0");
	}

	@Test
	public void shouldAcceptWithDefaultValue() {
		Accept<String> call = CallSync.consumer("test", true);
		assertEquals(call.value(), "test");
		call.accept("test0");
		assertEquals(call.value(), "test0");
		call.reset();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldAcceptValueWithoutSignal() {
		Accept<String> call = CallSync.consumer(null, true);
		call.value("test");
		call.assertNoCall();
		assertEquals(call.value(), "test");
	}

	@Test
	public void shouldGetAndRespond() throws InterruptedException {
		Get<String> call = CallSync.supplier(null);
		try (var exec = threadCall(() -> call.get())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetAndRespondWithInterrupt() throws InterruptedException {
		Get<String> call = CallSync.supplier(null);
		try (var exec = threadCall(() -> call.getWithInterrupt())) {
			call.await("test");
			assertEquals(exec.get(), "test");
		}
	}

	@Test
	public void shouldGetWithAutoResponse() throws InterruptedException {
		Get<String> call = CallSync.supplier("test");
		call.assertNoCall();
		assertEquals(call.get(), "test");
		call.awaitAuto();
	}

	@Test
	public void shouldGetWithExceptionFunction() throws InterruptedException {
		Get<String> call = CallSync.supplier("test");
		call.error(s -> s.length() == 5 ? Mode.checked : Mode.none);
		assertEquals(call.get(), "test");
		call.awaitAuto();
		call.autoResponse("test0");
		assertThrown(() -> call.get());
		call.awaitAuto();
	}

	@Test
	public void shouldRunAndRespond() throws InterruptedException {
		Run call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.run())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunAndRespondWithInterrupt() throws InterruptedException {
		Run call = CallSync.runnable(false);
		try (var exec = threadRun(() -> call.runWithInterrupt())) {
			call.await();
			exec.get();
		}
	}

	@Test
	public void shouldRunWithAutoResponse() throws InterruptedException {
		Run call = CallSync.runnable(true);
		call.assertNoCall();
		call.run();
		call.awaitAuto();
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var accept = CallSync.consumer("test", true);
		accept.accept("test0");
		assertFind(accept.toString(), "(?s)\\[test0\\].*\\[lambda\\].*test0;test");
		var run = CallSync.runnable(true);
		run.run();
		assertFind(run.toString(), "(?s)\\[OBJ\\].*OBJ;OBJ");
	}

}
