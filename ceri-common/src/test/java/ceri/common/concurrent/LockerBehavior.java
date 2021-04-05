package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;

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

	private static boolean isLocked(Lock lock) {
		return ((ReentrantLock) lock).isLocked();
	}
}
