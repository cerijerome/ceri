package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class ConcurrentUtilTest {
	private final ExecutorService exec = Executors.newSingleThreadExecutor();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ConcurrentUtil.class);
	}

	@Test
	public void testExecuteAndWait() throws IOException {
		BooleanCondition signal = BooleanCondition.create();
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
		AsyncRunner<?> runner1 = AsyncRunner.create(() -> ConcurrentUtil.executeAndWait(exec,
			() -> BasicUtil.delay(10000), IOException::new, 5000)).start();
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
		assertException(InterruptedException.class, () -> ConcurrentUtil.checkInterrupted());
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		Thread.currentThread().interrupt();
		assertException(RuntimeInterruptedException.class,
			() -> ConcurrentUtil.checkRuntimeInterrupted());
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

}
