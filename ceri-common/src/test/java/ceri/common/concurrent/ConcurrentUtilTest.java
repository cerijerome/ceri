package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import org.junit.Test;
import ceri.common.test.TestRunnable;

public class ConcurrentUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ConcurrentUtil.class);
	}

	@Test
	public void testCheckInterrupted() throws InterruptedException {
		ConcurrentUtil.checkInterrupted();
		Thread.currentThread().interrupt();
		assertException(InterruptedException.class, new TestRunnable() {
			@Override
			public void run() throws InterruptedException {
				ConcurrentUtil.checkInterrupted();
			}
		});
	}

	@Test
	public void testCheckRuntimeInterrupted() {
		ConcurrentUtil.checkRuntimeInterrupted();
		Thread.currentThread().interrupt();
		assertException(RuntimeInterruptedException.class, new TestRunnable() {
			@Override
			public void run() {
				ConcurrentUtil.checkRuntimeInterrupted();
			}
		});
	}

}
