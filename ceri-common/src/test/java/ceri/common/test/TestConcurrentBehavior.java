package ceri.common.test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.ValueCondition;
import ceri.common.time.Timeout;

public class TestConcurrentBehavior {
	private TestConcurrent.Exec exec;
	private SimpleExecutor<RuntimeException, ?> simple;

	@After
	public void after() {
		exec = Testing.close(exec);
		simple = Testing.close(simple);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(TestConcurrent.class);
	}

	@Test
	public void shouldRunFuture() {
		var future = TestConcurrent.futureOf();
		future.run();
		Assert.yes(future.isDone());
	}

	@Test
	public void shouldGetFutureResult() throws InterruptedException, ExecutionException {
		var future = TestConcurrent.futureOf("test");
		Assert.equal(future.get(), "test");
		future.get.assertAuto(Timeout.NULL);
	}

	@Test
	public void shouldGetFutureTimeoutResult()
		throws InterruptedException, ExecutionException, TimeoutException {
		var future = TestConcurrent.futureOf("test");
		Assert.equal(future.get(1, TimeUnit.MILLISECONDS), "test");
		future.get.assertAuto(Timeout.millis(1));
	}

	@Test
	public void shouldGetFutureWithException() {
		var future = TestConcurrent.futureOf("test");
		future.get.error.setFrom(ErrorGen.IOX);
		Assert.thrownCause(ExecutionException.class, IOException.class, future::get);
	}

	@Test
	public void shouldGetFutureTimeoutWithException() {
		var future = TestConcurrent.futureOf("test");
		future.get.error.setFrom(ErrorGen.IOX);
		Assert.thrownCause(ExecutionException.class, IOException.class,
			() -> future.get(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void shouldAwaitExecTermination() throws InterruptedException {
		exec = TestConcurrent.exec();
		Assert.yes(exec.awaitTermination(1, TimeUnit.MILLISECONDS));
		exec.awaitTermination.assertAuto(Timeout.millis(1));
	}

	@Test
	public void shouldExecuteExec() throws InterruptedException, ExecutionException {
		exec = TestConcurrent.exec();
		Assert.equal(exec.submit(() -> "test").get(), "test");
		exec.execute.awaitAuto();
	}

	@Test
	public void shouldShutdownExec() {
		exec = TestConcurrent.exec();
		exec.shutdown();
		exec.shutdown.assertAuto(true);
		Assert.yes(exec.isShutdown());
		Assert.yes(exec.isTerminated());
	}

	@Test
	public void testRunRepeat() throws InterruptedException {
		var sync = BoolCondition.of();
		simple = TestConcurrent.runRepeat(sync::signal);
		sync.await();
		sync.await();
	}

	@Test
	public void testRunRepeatWithIndex() throws InterruptedException {
		var sync = ValueCondition.<Integer>of();
		simple = TestConcurrent.runRepeat(i -> sync.signal(i));
		sync.await(i -> i > 1);
	}

	@Test
	public void testThreadCall() {
		var sync = ValueCondition.<String>of();
		simple = TestConcurrent.threadCall(sync::await);
		sync.signal("test");
		Assert.equal(simple.get(), "test");
	}

	@Test
	public void testThreadRun() {
		var sync = BoolCondition.of();
		simple = TestConcurrent.threadRun(sync::await);
		sync.signal();
		simple.get();
	}
}
