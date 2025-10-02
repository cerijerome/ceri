package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.test.TestUtil;

public class LockerBehavior {

	@Test
	public void shouldLockAndUnlock() {
		Locker locker = Locker.of();
		try (var _ = locker.lock()) {
			assertEquals(isLocked(locker.lock), true);
			throw new RuntimeException();
		} catch (RuntimeException e) {
			assertEquals(isLocked(locker.lock), false);
		}
		assertEquals(isLocked(locker.lock), false);
	}

	@Test
	public void shouldLockAndExecuteFunctions() {
		var captor = Captor.ofInt();
		Locker locker = Locker.of();
		try (var _ = locker.lock(() -> captor.accept(1), () -> captor.accept(2))) {
			captor.verifyInt(1);
		}
		captor.verifyInt(1, 2);
	}

	@Test
	public void shouldExecuteFunctions() {
		Locker locker = Locker.of();
		assertEquals(locker.get(() -> assertLocked(locker, "test")), "test");
		assertEquals(locker.getAsInt(() -> assertLocked(locker, 3)), 3);
		assertEquals(locker.getAsLong(() -> assertLocked(locker, 5L)), 5L);
		locker.run(() -> assertLocked(locker, ""));
	}

	@Test
	public void shouldTryToExecuteUnlockedFunctions() {
		Locker locker = Locker.of();
		assertEquals(locker.tryGet(() -> assertLocked(locker, "test")).value(), "test");
		assertEquals(locker.tryGetAsInt(() -> assertLocked(locker, 3)).getAsInt(), 3);
		assertEquals(locker.tryGetAsLong(() -> assertLocked(locker, 5L)).getAsLong(), 5L);
		assertTrue(locker.tryRun(() -> assertLocked(locker, "")));
	}

	@Test
	public void shouldTryToExecuteLockedFunctions() {
		Locker locker = Locker.of();
		try (var _ = locker.lock(); var exec = TestUtil.threadRun(() -> {
			assertTrue(locker.tryGet(() -> assertLocked(locker, "test")).isEmpty());
			assertTrue(locker.tryGetAsInt(() -> assertLocked(locker, 3)).isEmpty());
			assertTrue(locker.tryGetAsLong(() -> assertLocked(locker, 5L)).isEmpty());
			assertFalse(locker.tryRun(() -> assertLocked(locker, "")));
		})) {
			exec.get();
		}
	}

	@Test
	public void shouldCreateCondition() throws InterruptedException {
		Locker locker = Locker.of();
		Condition condition = locker.condition();
		try (var _ = TestUtil.threadRun(() -> signalLoop(locker, condition))) {
			try (var _ = locker.lock()) {
				condition.await();
			}
		}
	}

	private static void signalLoop(Locker locker, Condition condition) throws InterruptedException {
		while (true) {
			Concurrent.checkInterrupted();
			try (var _ = locker.lock()) {
				condition.signal();
			}
		}
	}

	private static <T> T assertLocked(Locker locker, T response) {
		assertEquals(isLocked(locker.lock), true);
		return response;
	}

	private static boolean isLocked(Lock lock) {
		return ((ReentrantLock) lock).isLocked();
	}
}
