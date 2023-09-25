package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.threadCall;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.AfterClass;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil.LockInfo;
import ceri.common.test.TestExecutorService;
import ceri.common.test.TestFuture;
import ceri.common.util.Holder;

public class ConcurrentUtilTest {
	private static final ExecutorService exec = Executors.newCachedThreadPool();

	@AfterClass
	public static void init() throws InterruptedException {
		exec.shutdownNow();
		exec.awaitTermination(1, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ConcurrentUtil.class);
	}

	@Test
	public void testLockInfo() {
		assertEquals(ConcurrentUtil.lockInfo(new ReentrantReadWriteLock().readLock()),
			LockInfo.NULL);
		Lock lock = new ReentrantLock();
		ConcurrentUtil.execute(lock, () -> {
			LockInfo info = ConcurrentUtil.lockInfo(lock);
			assertEquals(info.holdCount, 1);
			assertEquals(info.queueLength, 0);
		});
	}

	@Test
	public void testLocker() {
		var lock = new ReentrantLock();
		try (var x = ConcurrentUtil.locker(lock)) {
			assertTrue(lock.isLocked());
		}
		assertFalse(lock.isLocked());
	}

	@Test
	public void testDelay() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		try (var exec = SimpleExecutor.run(() -> {
			ConcurrentUtil.delay(0);
			ConcurrentUtil.delay(1);
			sync.signal();
			ConcurrentUtil.delay(100000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testDelayMicros() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		try (var exec = SimpleExecutor.run(() -> {
			ConcurrentUtil.delayMicros(0);
			ConcurrentUtil.delayMicros(10);
			sync.signal();
			ConcurrentUtil.delay(100000000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testDelayNanos() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		try (var exec = SimpleExecutor.run(() -> {
			ConcurrentUtil.delayNanos(0);
			ConcurrentUtil.delayNanos(10);
			sync.signal();
			ConcurrentUtil.delayNanos(100000000);
		})) {
			sync.await();
		}
	}

	@Test
	public void testCloseExecutor() {
		try (ExecutorService exec = Executors.newSingleThreadExecutor()) {
			exec.submit(() -> ConcurrentUtil.delay(60000));
			assertTrue(ConcurrentUtil.close(exec, 1000));
		}
	}

	@Test
	public void testCloseExecutorWithException() {
		assertFalse(ConcurrentUtil.close(null, 0));
		try (TestExecutorService exec = TestExecutorService.of()) {
			exec.awaitTermination.error.setFrom(INX);
			assertFalse(ConcurrentUtil.close(exec, 0));
		}
	}

	@Test
	public void testExecuteAndGet() throws IOException {
		ValueCondition<String> signal = ValueCondition.of();
		signal.signal("test");
		assertEquals(ConcurrentUtil.executeAndGet(exec, signal::await, IOException::new), "test");
		signal.signal("test2");
		assertEquals(ConcurrentUtil.executeAndGet(exec, signal::await, IOException::new, 10000),
			"test2");
	}

	@Test
	public void testExecuteAndWait() throws IOException {
		BooleanCondition signal = BooleanCondition.of();
		ConcurrentUtil.executeAndWait(exec, signal::signal, IOException::new);
		assertTrue(signal.isSet());
		signal.clear();
		ConcurrentUtil.executeAndWait(exec, signal::signal, IOException::new, 10000);
		assertTrue(signal.isSet());
	}

	@Test
	public void testExecuteAndWaitThrowExceptionsOfGivenType() {
		assertThrown(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new));
		assertThrown(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new, 10000));
	}

	@Test
	public void testExecuteAndWaitTimeoutException() {
		assertThrown(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, //
			() -> Thread.sleep(1000), IOException::new, 1));
	}

	@Test
	public void testGetFutureInterruption() {
		TestFuture<?> future = TestFuture.of("test");
		future.get.error.setFrom(INX);
		assertThrown(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.get(future, RuntimeException::new));
		future.get.error.setFrom(INX);
		assertThrown(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.get(future, RuntimeException::new, 1000));
	}

	@Test
	public void testExecuteRunnable() throws InterruptedException {
		Lock lock = new ReentrantLock();
		final boolean[] exec = { false };
		ConcurrentUtil.execute(lock, () -> exec[0] = true);
		assertTrue(exec[0]);
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteRunnableUnlocksOnException() throws InterruptedException {
		Lock lock = new ReentrantLock();
		assertThrown(() -> ConcurrentUtil.execute(lock, () -> {
			throw new RuntimeInterruptedException("test");
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testTryExecuteRunnable() {
		Lock lock = new ReentrantLock();
		var holder = Holder.mutable();
		assertTrue(ConcurrentUtil.tryExecute(lock, () -> {
			holder.set("test0");
			// Cannot get lock in new thread => return false
			try (var exec =
				threadCall(() -> ConcurrentUtil.tryExecute(lock, () -> holder.set("test1")))) {
				assertFalse(exec.get());
			}
		}));
		assertEquals(holder.value(), "test0");
	}

	@Test
	public void testTryExecuteGet() {
		Lock lock = new ReentrantLock();
		var holder0 = ConcurrentUtil.tryExecuteGet(lock, () -> {
			try (var exec = threadCall(() -> ConcurrentUtil.tryExecuteGet(lock, () -> "test1"))) {
				var holder1 = exec.get();
				assertTrue(holder1.isEmpty());
			}
			return "test0";
		});
		assertTrue(holder0.holds("test0"));
	}

	@Test
	public void testExecuteSupplier() throws InterruptedException {
		Lock lock = new ReentrantLock();
		String result = ConcurrentUtil.executeGet(lock, () -> "test");
		assertEquals(result, "test");
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteSupplierUnlocksOnException() throws InterruptedException {
		Lock lock = new ReentrantLock();
		boolean throwEx = true;
		assertThrown(() -> ConcurrentUtil.executeGet(lock, () -> {
			if (throwEx) throw new RuntimeInterruptedException("test");
			return "test";
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testInterruptFromException() {
		assertFalse(Thread.interrupted());
		assertFalse(ConcurrentUtil.interrupt(new RuntimeException()));
		assertFalse(Thread.interrupted());
		assertTrue(ConcurrentUtil.interrupt(new RuntimeInterruptedException("test")));
		assertTrue(Thread.interrupted());
		assertTrue(ConcurrentUtil.interrupt(new InterruptedException("test")));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testCheckInterrupted() throws InterruptedException {
		ConcurrentUtil.checkInterrupted();
		ConcurrentUtil.interrupt();
		assertThrown(InterruptedException.class, ConcurrentUtil::checkInterrupted);
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		ConcurrentUtil.interrupt();
		assertThrown(RuntimeInterruptedException.class, ConcurrentUtil::checkRuntimeInterrupted);
	}

	@Test
	public void testExecuteInterruptible() {
		ConcurrentUtil.executeInterruptible(() -> {});
		assertThrown(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.executeInterruptible(() -> {
				throw new InterruptedException();
			}));
	}

	@Test
	public void testExecuteGetInterruptible() {
		assertEquals(ConcurrentUtil.executeGetInterruptible(() -> "x"), "x");
		assertThrown(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.executeGetInterruptible(() -> {
				throw new InterruptedException();
			}));
	}

	@Test
	public void testInvokeMultipleThreads() throws InterruptedException, IOException {
		Set<String> msgs = new HashSet<>();
		ConcurrentUtil.invoke(exec, IOException::new, () -> msgs.add("1"), () -> msgs.add("2"));
		assertCollection(msgs, "1", "2");
		ConcurrentUtil.invoke(exec, IOException::new, 1000, () -> msgs.add("3"));
		assertCollection(msgs, "1", "2", "3");
	}

	@Test
	public void testInvokeWithException() {
		Set<String> msgs = new HashSet<>();
		assertThrown(IOException.class,
			() -> ConcurrentUtil.invoke(exec, IOException::new, () -> msgs.add("1"), () -> {
				throw new Exception("test");
			}));
		assertCollection(msgs, "1");
	}

	@Test
	public void testInvokeWithTimeout() {
		Set<String> msgs = new HashSet<>();
		assertThrown(CancellationException.class,
			() -> ConcurrentUtil.invoke(exec, IOException::new, 1, () -> {
				Thread.sleep(10000);
				msgs.add("1");
			}, () -> {
				Thread.sleep(10000);
				msgs.add("2");
			}));
		assertCollection(msgs);
	}

}
