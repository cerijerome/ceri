package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.util.BasicUtil;

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
		assertException(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new));
		assertException(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, () -> {
			throw new ParseException("hello", 0);
		}, IOException::new, 10000));
	}

	@Test
	public void testExecuteAndWaitTimeoutException() {
		assertException(IOException.class, () -> ConcurrentUtil.executeAndWait(exec, //
			() -> Thread.sleep(1000), IOException::new, 1));
	}

	@Test
	public void testExecuteAndWaitInterruptions() throws Exception {
		AsyncRunner<?> runner1 = AsyncRunner.create(() -> ConcurrentUtil
			.executeAndWait(exec, () -> BasicUtil.delay(10000), IOException::new, 5000)).start();
		AsyncRunner<?> runner2 = AsyncRunner.create(
			() -> ConcurrentUtil.executeAndWait(exec, () -> runner1.join(10000), IOException::new))
			.start();
		AsyncRunner<?> runner3 = AsyncRunner.create(() -> {
			runner1.interrupt();
			runner2.interrupt();
		}).start();
		runner3.join(10000);
		assertException(() -> runner2.join(10000));
		assertException(() -> runner1.join(10000));
	}

	@Test
	public void testExecuteRunnable() throws InterruptedException {
		Lock lock = new ReentrantLock();
		final boolean[] exec = { false };
		ConcurrentUtil.execute(lock, () -> exec[0] = true);
		assertThat(exec[0], is(true));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteRunnableUnlocksOnException() throws InterruptedException {
		Lock lock = new ReentrantLock();
		assertException(() -> ConcurrentUtil.execute(lock, () -> {
			throw new RuntimeInterruptedException("test");
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteSupplier() throws InterruptedException {
		Lock lock = new ReentrantLock();
		String result = ConcurrentUtil.executeGet(lock, () -> "test");
		assertThat(result, is("test"));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testExecuteSupplierUnlocksOnException() throws InterruptedException {
		Lock lock = new ReentrantLock();
		boolean throwEx = true;
		assertException(() -> ConcurrentUtil.executeGet(lock, () -> {
			if (throwEx) throw new RuntimeInterruptedException("test");
			return "test";
		}));
		assertTrue(lock.tryLock(1, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testCheckInterrupted() throws InterruptedException {
		ConcurrentUtil.checkInterrupted();
		Thread.currentThread().interrupt();
		assertException(InterruptedException.class, ConcurrentUtil::checkInterrupted);
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		Thread.currentThread().interrupt();
		assertException(RuntimeInterruptedException.class,
			ConcurrentUtil::checkRuntimeInterrupted);
	}

	@Test
	public void testExecuteInterruptible() {
		ConcurrentUtil.executeInterruptible(() -> {});
		assertException(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.executeInterruptible(() -> {
				throw new InterruptedException();
			}));
	}

	@Test
	public void testExecuteGetInterruptible() {
		assertThat(ConcurrentUtil.executeGetInterruptible(() -> "x"), is("x"));
		assertException(RuntimeInterruptedException.class,
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
		assertException(IOException.class,
			() -> ConcurrentUtil.invoke(exec, IOException::new, () -> msgs.add("1"), () -> {
				throw new Exception("test");
			}));
		assertCollection(msgs, "1");
	}

	@Test
	public void testInvokeWithTimeout() throws Exception {
		Set<String> msgs = new HashSet<>();
		assertException(CancellationException.class,
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
