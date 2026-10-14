package ceri.common.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestConcurrent;

public class BinarySemaphoreBehavior {
	private BinarySemaphore bs;

	@BeforeEach
	public void before() {
		bs = BinarySemaphore.of();
	}

	@AfterEach
	public void after() {
		bs.close();
	}

	@Test
	public void shouldAcquireWithResource() {
		try (var _ = bs.acquirer()) {
			Assert.no(bs.available());
		}
		Assert.yes(bs.available());
	}

	@Test
	public void shouldFailToAcquireIfClosed() {
		bs.acquire();
		bs.close();
		Assert.no(bs.available());
		Assert.thrown(bs::acquire);
	}

	@Test
	public void shouldFailIfClosedDuringAcquisition() {
		bs.acquire();
		try (var x = TestConcurrent.threadRun(() -> {
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
		try (var _ = TestConcurrent.threadRun(() -> {
			Assert.thrown(bs::acquire);
		})) {
			while (bs.waitingThreads() == 0)
				Concurrent.delay(1);
		}
	}

}
