package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.AssertUtil.throwIo;
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
import ceri.common.collect.Sets;
import ceri.common.function.Closeables;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.test.TestUtil;
import ceri.common.util.Holder;

public class ConcurrentTest {
	private ExecutorService exec = Executors.newCachedThreadPool();

	@After
	public void after() {
		Closeables.close(exec);
		exec = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Concurrent.class);
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
		assertEquals(Concurrent.lockInfo(new ReentrantReadWriteLock().readLock()),
			Concurrent.LockInfo.NULL);
		var lock = new ReentrantLock();
		Concurrent.lockedRun(lock, () -> {
			var info = Concurrent.lockInfo(lock);
			assertEquals(info.holdCount, 1);
			assertEquals(info.queueLength, 0);
		});
	}

	@Test
	public void testLocker() {
		var lock = new ReentrantLock();
		try (var _ = Concurrent.locker(lock)) {
			assertTrue(lock.isLocked());
		}
		assertFalse(lock.isLocked());
	}

	@Test
	public void testLockerWithException() {
		var lock = new ReentrantLock();
		try (var _ = Concurrent.locker(lock, () -> throwIo(), () -> {})) {
			fail();
		} catch (IOException e) {
			assertFalse(lock.isLocked());
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
		assertEquals(
			Concurrent.getWhileInterrupted((t, _) -> sync.apply(t), 100L, TimeUnit.MILLISECONDS),
			true);
		assertEquals(Thread.interrupted(), true);
	}

	@Test
	public void testClosedExecutorSubmit() {
		try (var exec = TestExecutorService.of()) {
			exec.execute.error.set(new RejectedExecutionException("test"));
			assertThrown(RejectedExecutionException.class, () -> Concurrent.submit(exec, () -> {}));
			exec.close();
			Concurrent.submit(exec, () -> {}); // no exception if closed
		}
	}

	@Test
	public void testExecuteAndGet() throws IOException {
		initExec();
		var signal = ValueCondition.of();
		signal.signal("test");
		assertEquals(Concurrent.submitAndGet(exec, signal::await, IOException::new), "test");
		signal.signal("test2");
		assertEquals(Concurrent.submitAndGet(exec, signal::await, IOException::new, 10000),
			"test2");
	}

	@Test
	public void testExecuteAndWait() throws IOException {
		initExec();
		var signal = BoolCondition.of();
		Concurrent.submitAndWait(exec, signal::signal, IOException::new);
		assertTrue(signal.isSet());
		signal.clear();
		Concurrent.submitAndWait(exec, signal::signal, IOException::new, 10000);
		assertTrue(signal.isSet());
	}

	@Test
	public void testExecuteAndWaitThrowExceptionsOfGivenType() {
		initExec();
		assertIoe(() -> Concurrent.submitAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new));
		assertIoe(() -> Concurrent.submitAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new, 10000));
	}

	@Test
	public void testExecuteAndWaitTimeoutException() {
		assertIoe(
			() -> Concurrent.submitAndWait(exec, () -> Thread.sleep(1000), IOException::new, 1));
	}

	@Test
	public void testGetFutureInterruption() {
		var future = TestFuture.of("test");
		future.get.error.setFrom(ErrorGen.INX);
		assertThrown(RuntimeInterruptedException.class,
			() -> Concurrent.get(future, RuntimeException::new));
		future.get.error.setFrom(ErrorGen.INX);
		assertThrown(RuntimeInterruptedException.class,
			() -> Concurrent.get(future, RuntimeException::new, 1000));
	}

	@Test
	public void testExecuteRunnable() throws InterruptedException {
		var lock = new ReentrantLock();
		final boolean[] exec = { false };
		Concurrent.lockedRun(lock, () -> exec[0] = true);
		assertTrue(exec[0]);
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteRunnableUnlocksOnException() throws InterruptedException {
		var lock = new ReentrantLock();
		assertRte(() -> Concurrent.lockedRun(lock, () -> {
			throw new RuntimeInterruptedException("test");
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testTryExecuteRunnable() {
		var lock = new ReentrantLock();
		var holder = Holder.mutable();
		assertTrue(Concurrent.tryLockedRun(lock, () -> {
			holder.set("test0");
			// Cannot get lock in new thread => return false
			try (var exec = TestUtil
				.threadCall(() -> Concurrent.tryLockedRun(lock, () -> holder.set("test1")))) {
				assertFalse(exec.get());
			}
		}));
		assertEquals(holder.value(), "test0");
	}

	@Test
	public void testTryExecuteGet() {
		var lock = new ReentrantLock();
		var holder0 = Concurrent.tryLockedGet(lock, () -> {
			try (var exec =
				TestUtil.threadCall(() -> Concurrent.tryLockedGet(lock, () -> "test1"))) {
				var holder1 = exec.get();
				assertTrue(holder1.isEmpty());
			}
			return "test0";
		});
		assertTrue(holder0.holds("test0"));
	}

	@Test
	public void testExecuteSupplier() throws InterruptedException {
		var lock = new ReentrantLock();
		var result = Concurrent.lockedGet(lock, () -> "test");
		assertEquals(result, "test");
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteSupplierUnlocksOnException() throws InterruptedException {
		var lock = new ReentrantLock();
		boolean throwEx = true;
		assertRte(() -> Concurrent.lockedGet(lock, () -> {
			if (throwEx) throw new RuntimeInterruptedException("test");
			return "test";
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testInterruptFromException() {
		assertFalse(Thread.interrupted());
		assertFalse(Concurrent.interrupt(new RuntimeException()));
		assertFalse(Thread.interrupted());
		assertTrue(Concurrent.interrupt(new RuntimeInterruptedException("test")));
		assertTrue(Thread.interrupted());
		assertTrue(Concurrent.interrupt(new InterruptedException("test")));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testInterrupted() {
		Concurrent.interrupt();
		assertEquals(Concurrent.interrupted(), true); // not cleared
		assertEquals(Concurrent.interrupted(), true);
		assertEquals(Thread.interrupted(), true);
	}

	@Test
	public void testCheckInterrupted() throws InterruptedException {
		Concurrent.checkInterrupted();
		Concurrent.interrupt();
		assertThrown(InterruptedException.class, Concurrent::checkInterrupted);
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		Concurrent.checkRuntimeInterrupted();
		Concurrent.interrupt();
		assertThrown(RuntimeInterruptedException.class, Concurrent::checkRuntimeInterrupted);
	}

	@Test
	public void testExecuteInterruptible() {
		Concurrent.runInterruptible(() -> {});
		assertThrown(RuntimeInterruptedException.class, () -> Concurrent.runInterruptible(() -> {
			throw new InterruptedException();
		}));
	}

	@Test
	public void testExecuteGetInterruptible() {
		assertEquals(Concurrent.getInterruptible(() -> "x"), "x");
		assertThrown(RuntimeInterruptedException.class, () -> Concurrent.getInterruptible(() -> {
			throw new InterruptedException();
		}));
	}

	@Test
	public void testInvokeMultipleThreads() throws InterruptedException, IOException {
		initExec();
		var msgs = Sets.<String>of();
		Concurrent.invoke(exec, IOException::new, () -> msgs.add("1"), () -> msgs.add("2"));
		assertUnordered(msgs, "1", "2");
		Concurrent.invoke(exec, IOException::new, 1000, () -> msgs.add("3"));
		assertUnordered(msgs, "1", "2", "3");
	}

	@Test
	public void testInvokeWithException() {
		initExec();
		var msgs = Sets.<String>of();
		assertIoe(() -> Concurrent.invoke(exec, IOException::new, () -> msgs.add("1"), () -> {
			throw new Exception("test");
		}));
		assertUnordered(msgs, "1");
	}

	@Test
	public void testInvokeWithTimeout() {
		initExec();
		var msgs = Sets.<String>of();
		assertThrown(CancellationException.class,
			() -> Concurrent.invoke(exec, IOException::new, 1, () -> {
				Thread.sleep(10000);
				msgs.add("1");
			}, () -> {
				Thread.sleep(10000);
				msgs.add("2");
			}));
		assertUnordered(msgs);
	}

	@Test
	public void testInvokeClosedWithTimeout() throws Exception {
		try (var exec = TestExecutorService.of()) {
			exec.execute.error.set(new RejectedExecutionException("test"));
			assertThrown(RejectedExecutionException.class,
				() -> Concurrent.invoke(exec, IOException::new, () -> {}));
			exec.close();
			Concurrent.invoke(exec, IOException::new, () -> {}); // no exception if closed
		}
	}

	private void initExec() {
		exec = Executors.newCachedThreadPool();
	}
}
