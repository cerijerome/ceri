package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;

public class ConcurrentUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ConcurrentUtil.class);
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
		assertException(RuntimeInterruptedException.class, () -> ConcurrentUtil
			.checkRuntimeInterrupted());
	}

}
