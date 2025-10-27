package ceri.common.concurrent;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestUtil;
import ceri.common.util.Holder;

public class ConcurrentTest {
	private CallSync.Consumer<Object> captor;
	private ExecutorService exec = Executors.newCachedThreadPool();

	@After
	public void after() {
		exec = TestUtil.close(exec);
		captor = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Concurrent.class);
	}

	@Test
	public void testNullLock() throws InterruptedException {
		Concurrent.NULL_LOCK.lock();
		Concurrent.NULL_LOCK.lockInterruptibly();
		Concurrent.NULL_LOCK.tryLock();
		Concurrent.NULL_LOCK.tryLock(1, TimeUnit.MICROSECONDS);
		Concurrent.NULL_LOCK.unlock();
		var condition = Concurrent.NULL_LOCK.newCondition();
		condition.signal();
		condition.signalAll();
		condition.await();
		condition.await(1, TimeUnit.MICROSECONDS);
		condition.awaitNanos(1);
		condition.awaitUntil(new Date());
		condition.awaitUninterruptibly();
	}

	@Test
	public void testLockInfo() {
		Assert.equal(Concurrent.lockInfo(new ReentrantReadWriteLock().readLock()),
			Concurrent.LockInfo.NULL);
		var lock = new ReentrantLock();
		Concurrent.lockedRun(lock, () -> {
			var info = Concurrent.lockInfo(lock);
			Assert.equal(info.holdCount, 1);
			Assert.equal(info.queueLength, 0);
		});
	}

	@Test
	public void testLocker() {
		var lock = new ReentrantLock();
		try (var _ = Concurrent.locker(lock)) {
			Assert.equal(lock.isLocked(), true);
		}
		Assert.equal(lock.isLocked(), false);
	}

	@Test
	public void testLockerWithException() {
		var lock = new ReentrantLock();
		try (var _ = Concurrent.locker(lock, () -> Assert.throwIo(), () -> {})) {
			Assert.fail();
		} catch (IOException e) {
			Assert.equal(lock.isLocked(), false);
		}
	}

	@Test
	public void testDelay() throws InterruptedException {
		var sync = BoolCondition.of();
		try (var _ = SimpleExecutor.run(() -> {
			Concurrent.delay(0);
			Concurrent.delay(1);
			sync.signal();
			Concurrent.delay(100000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testDelayMicros() throws InterruptedException {
		var sync = BoolCondition.of();
		try (var _ = SimpleExecutor.run(() -> {
			Concurrent.delayMicros(0);
			Concurrent.delayMicros(10);
			sync.signal();
			Concurrent.delay(100000000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testDelayNanos() throws InterruptedException {
		var sync = BoolCondition.of();
		try (var _ = SimpleExecutor.run(() -> {
			Concurrent.delayNanos(0);
			Concurrent.delayNanos(10);
			sync.signal();
			Concurrent.delayNanos(100000000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testGetWhileInterrupted() {
		var sync = CallSync.function(0L, true);
		sync.error.setFrom(ErrorGen.INX, null);
		Assert.equal(
			Concurrent.getWhileInterrupted((t, _) -> sync.apply(t), 100L, TimeUnit.MILLISECONDS),
			true);
		Assert.equal(Thread.interrupted(), true);
	}

	@Test
	public void testClosedExecutorSubmit() {
		try (var exec = TestExecutorService.of()) {
			exec.execute.error.set(new RejectedExecutionException("test"));
			Assert.thrown(RejectedExecutionException.class,
				() -> Concurrent.submit(exec, () -> {}));
			exec.close();
			Concurrent.submit(exec, () -> {}); // no exception if closed
		}
	}

	@Test
	public void testExecuteAndGet() throws IOException {
		initExec(true);
		captor.accept("test");
		Assert.equal(Concurrent.submitAndGet(exec, captor::await, IOException::new), "test");
		captor.accept("test2");
		Assert.equal(Concurrent.submitAndGet(exec, captor::await, IOException::new, 10000),
			"test2");
	}

	@Test
	public void testExecuteAndWait() throws IOException {
		initExec(true);
		Concurrent.submitAndWait(exec, () -> captor.accept("1"), IOException::new);
		captor.assertAuto("1");
		Concurrent.submitAndWait(exec, () -> captor.accept("2"), IOException::new, 10000);
		captor.assertAuto("2");
	}

	@Test
	public void testExecuteAndWaitThrowExceptionsOfGivenType() {
		initExec(null);
		Assert.io(() -> Concurrent.submitAndWait(exec,
			() -> Assert.throwIt(new ParseException("hello", 0)), IOException::new));
		Assert.io(() -> Concurrent.submitAndWait(exec,
			() -> Assert.throwIt(new ParseException("hello", 0)), IOException::new, 10000));
	}

	@Test
	public void testExecuteAndWaitTimeoutException() {
		initExec(null);
		Assert.io(
			() -> Concurrent.submitAndWait(exec, () -> Thread.sleep(10000), IOException::new, 1));
	}

	@Test
	public void testGetFutureInterruption() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(ErrorGen.INX);
		Assert.thrown(RuntimeInterruptedException.class,
			() -> Concurrent.get(future, RuntimeException::new));
		future.get.error.setFrom(ErrorGen.INX);
		Assert.thrown(RuntimeInterruptedException.class,
			() -> Concurrent.get(future, RuntimeException::new, 1000));
	}

	@Test
	public void testExecuteRunnable() throws InterruptedException {
		var lock = new ReentrantLock();
		final boolean[] exec = { false };
		Concurrent.lockedRun(lock, () -> exec[0] = true);
		Assert.equal(exec[0], true);
		Assert.equal(lock.tryLock(1, TimeUnit.MILLISECONDS), true);
	}

	@Test
	public void testExecuteRunnableUnlocksOnException() throws InterruptedException {
		var lock = new ReentrantLock();
		Assert.runtime(() -> Concurrent.lockedRun(lock,
			() -> Assert.throwIt(new RuntimeInterruptedException("test"))));
		Assert.equal(lock.tryLock(1, TimeUnit.MILLISECONDS), true);
	}

	@Test
	public void testTryExecuteRunnable() {
		var lock = new ReentrantLock();
		var holder = Holder.mutable();
		Assert.equal(Concurrent.tryLockedRun(lock, () -> {
			holder.set("test0");
			// Cannot get lock in new thread => return false
			try (var exec = TestUtil
				.threadCall(() -> Concurrent.tryLockedRun(lock, () -> holder.set("test1")))) {
				Assert.equal(exec.get(), false);
			}
		}), true);
		Assert.equal(holder.value(), "test0");
	}

	@Test
	public void testTryExecuteGet() {
		var lock = new ReentrantLock();
		var holder0 = Concurrent.tryLockedGet(lock, () -> {
			try (var exec =
				TestUtil.threadCall(() -> Concurrent.tryLockedGet(lock, () -> "test1"))) {
				var holder1 = exec.get();
				Assert.equal(holder1.isEmpty(), true);
			}
			return "test0";
		});
		Assert.equal(holder0.holds("test0"), true);
	}

	@Test
	public void testExecuteSupplier() throws InterruptedException {
		var lock = new ReentrantLock();
		var result = Concurrent.lockedGet(lock, () -> "test");
		Assert.equal(result, "test");
		Assert.equal(lock.tryLock(1, TimeUnit.MILLISECONDS), true);
	}

	@Test
	public void testExecuteSupplierUnlocksOnException() throws InterruptedException {
		var lock = new ReentrantLock();
		boolean throwEx = true;
		Assert.runtime(() -> Concurrent.lockedGet(lock, () -> {
			if (throwEx) throw new RuntimeInterruptedException("test");
			return "test";
		}));
		Assert.equal(lock.tryLock(1, TimeUnit.MILLISECONDS), true);
	}

	@Test
	public void testInterruptFromException() {
		Assert.equal(Thread.interrupted(), false);
		Assert.equal(Concurrent.interrupt(new RuntimeException()), false);
		Assert.equal(Thread.interrupted(), false);
		Assert.equal(Concurrent.interrupt(new RuntimeInterruptedException("test")), true);
		Assert.equal(Thread.interrupted(), true);
		Assert.equal(Concurrent.interrupt(new InterruptedException("test")), true);
		Assert.equal(Thread.interrupted(), true);
	}

	@Test
	public void testInterrupted() {
		Concurrent.interrupt();
		Assert.equal(Concurrent.interrupted(), true); // not cleared
		Assert.equal(Concurrent.interrupted(), true);
		Assert.equal(Thread.interrupted(), true);
	}

	@Test
	public void testCheckInterrupted() throws InterruptedException {
		Concurrent.checkInterrupted();
		Concurrent.interrupt();
		Assert.thrown(InterruptedException.class, Concurrent::checkInterrupted);
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		Concurrent.checkRuntimeInterrupted();
		Concurrent.interrupt();
		Assert.thrown(RuntimeInterruptedException.class, Concurrent::checkRuntimeInterrupted);
	}

	@Test
	public void testExecuteInterruptible() {
		Concurrent.runInterruptible(() -> {});
		Assert.thrown(RuntimeInterruptedException.class,
			() -> Concurrent.runInterruptible(() -> Assert.throwInterrupted()));
	}

	@Test
	public void testExecuteGetInterruptible() {
		Assert.equal(Concurrent.getInterruptible(() -> "x"), "x");
		Assert.thrown(RuntimeInterruptedException.class,
			() -> Concurrent.getInterruptible(() -> Assert.throwInterrupted()));
	}

	@Test
	public void testInvokeMultipleThreads() throws InterruptedException, IOException {
		initExec(true);
		Concurrent.invoke(exec, IOException::new, () -> captor.accept("1"),
			() -> captor.accept("2"));
		assertCaptor("1", "2");
		Concurrent.invoke(exec, IOException::new, 1000, () -> captor.accept("3"));
		assertCaptor("1", "2", "3");
	}

	@Test
	public void testInvokeWithException() {
		initExec(true);
		Assert.io(() -> Concurrent.invoke(exec, IOException::new, () -> captor.accept("1"),
			() -> Assert.throwIt(new Exception("test"))));
		assertCaptor("1");
	}

	@Test
	public void testInvokeWithTimeout() {
		initExec(true);
		Assert.thrown(CancellationException.class,
			() -> Concurrent.invoke(exec, IOException::new, 1, () -> {
				Thread.sleep(10000);
				captor.accept("1");
			}, () -> {
				Thread.sleep(10000);
				captor.accept("2");
			}));
		assertCaptor();
	}

	@Test
	public void testInvokeClosedWithTimeout() throws Exception {
		try (var exec = TestExecutorService.of()) {
			exec.execute.error.set(new RejectedExecutionException("test"));
			Assert.thrown(RejectedExecutionException.class,
				() -> Concurrent.invoke(exec, IOException::new, () -> {}));
			exec.close();
			Concurrent.invoke(exec, IOException::new, () -> {}); // no exception if closed
		}
	}

	private void initExec(Boolean autoResponse) {
		exec = Executors.newCachedThreadPool();
		if (autoResponse != null) initCaptor(autoResponse);
	}

	private void initCaptor(boolean autoResponse) {
		captor = CallSync.consumer(null, autoResponse);
	}

	private void assertCaptor(Object... values) {
		Assert.unordered(captor.values(), values);
	}
}
