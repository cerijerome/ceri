package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;

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
	public void testCloseExecutor() {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.submit(() -> ConcurrentUtil.delay(60000));
		assertTrue(ConcurrentUtil.close(exec, 1000));
	}

	@Test
	public void testCloseExecutorWithException() throws InterruptedException {
		assertFalse(ConcurrentUtil.close(null, 0));
		ExecutorService exec = Mockito.mock(ExecutorService.class);
		when(exec.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException());
		assertFalse(ConcurrentUtil.close(exec, 0));
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
	public void testGetFutureInterruption()
		throws InterruptedException, ExecutionException, TimeoutException {
		Future<?> future = Mockito.mock(Future.class);
		when(future.get()).thenThrow(new InterruptedException());
		when(future.get(anyLong(), any())).thenThrow(new InterruptedException());
		assertThrown(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.get(future, RuntimeException::new));
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
	public void testCheckInterrupted() throws InterruptedException {
		ConcurrentUtil.checkInterrupted();
		Thread.currentThread().interrupt();
		assertThrown(InterruptedException.class, ConcurrentUtil::checkInterrupted);
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		Thread.currentThread().interrupt();
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
