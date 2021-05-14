package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class LockerBehavior {

	@Test
	public void shouldLockAndUnlock() {
		Locker locker = Locker.of();
		try (var locked = locker.lock()) {
			assertEquals(isLocked(locker.lock), true);
			throw new RuntimeException();
		} catch (RuntimeException e) {
			assertEquals(isLocked(locker.lock), false);
		}
		assertEquals(isLocked(locker.lock), false);
	}

	@Test
	public void shouldCreateCondition() throws InterruptedException {
		Locker locker = Locker.of();
		Condition condition = locker.condition();
		try (var exec = TestUtil.threadRun(() -> signalLoop(locker, condition))) {
			try (var locked = locker.lock()) {
				condition.await();
			}
		}
	}

	private static void signalLoop(Locker locker, Condition condition) throws InterruptedException {
		while (true) {
			ConcurrentUtil.checkInterrupted();
			try (var locked = locker.lock()) {
				condition.signal();
			}
		}
	}
	
	private static boolean isLocked(Lock lock) {
		return ((ReentrantLock) lock).isLocked();
	}
}
