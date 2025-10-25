package ceri.common.concurrent;

import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class BinarySemaphoreBehavior {
	private BinarySemaphore bs;

	@Before
	public void before() {
		bs = BinarySemaphore.of();
	}

	@After
	public void after() {
		bs.close();
	}

	@Test
	public void shouldAcquireWithResource() {
		try (var _ = bs.acquirer()) {
			assertFalse(bs.available());
		}
		assertTrue(bs.available());
	}

	@Test
	public void shouldFailToAcquireIfClosed() {
		bs.acquire();
		bs.close();
		assertFalse(bs.available());
		Assert.thrown(bs::acquire);
	}

	@Test
	public void shouldFailIfClosedDuringAcquisition() {
		bs.acquire();
		try (var x = TestUtil.threadRun(() -> {
			Assert.thrown(bs::acquire);
		})) {
			while (bs.waitingThreads() == 0)
				Concurrent.delay(1);
			bs.close();
			x.get();
		}
	}

	@Test
	public void shouldFailToAcquireIfInterrupted() {
		bs.acquire();
		try (var _ = TestUtil.threadRun(() -> {
			Assert.thrown(bs::acquire);
		})) {
			while (bs.waitingThreads() == 0)
				Concurrent.delay(1);
		}
	}

}
