package ceri.common.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.TestConcurrent;

public class LockerBehavior {

	@Test
	public void shouldLockAndUnlock() {
		Locker locker = Locker.of();
		try (var _ = locker.lock()) {
			Assert.equal(isLocked(locker.lock), true);
			throw new RuntimeException();
		} catch (RuntimeException e) {
			Assert.equal(isLocked(locker.lock), false);
		}
		Assert.equal(isLocked(locker.lock), false);
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
		Assert.equal(locker.get(() -> assertLocked(locker, "test")), "test");
		Assert.equal(locker.getAsBool(() -> assertLocked(locker, false)), false);
		Assert.equal(locker.getAsInt(() -> assertLocked(locker, 3)), 3);
		Assert.equal(locker.getAsLong(() -> assertLocked(locker, 5L)), 5L);
		Assert.equal(locker.getAsDouble(() -> assertLocked(locker, 0.1)), 0.1);
		locker.run(() -> assertLocked(locker, ""));
	}

	@Test
	public void shouldTryToExecuteUnlockedFunctions() {
		Locker locker = Locker.of();
		Assert.equal(locker.tryGet(() -> assertLocked(locker, "test")).value(), "test");
		Assert.equal(locker.tryGetAsBool(() -> assertLocked(locker, true)), true);
		Assert.equal(locker.tryGetAsInt(() -> assertLocked(locker, 3)), 3);
		Assert.equal(locker.tryGetAsLong(() -> assertLocked(locker, 5L)), 5L);
		Assert.equal(locker.tryGetAsDouble(() -> assertLocked(locker, 0.1)), 0.1);
		Assert.yes(locker.tryRun(() -> assertLocked(locker, "")));
	}

	@Test
	public void shouldTryToExecuteLockedFunctions() {
		Locker locker = Locker.of();
		try (var _ = locker.lock(); var exec = TestConcurrent.threadRun(() -> {
			Assert.yes(locker.tryGet(() -> assertLocked(locker, "test")).isEmpty());
			Assert.equal(locker.tryGetAsBool(() -> assertLocked(locker, true)), null);
			Assert.equal(locker.tryGetAsInt(() -> assertLocked(locker, 3)), null);
			Assert.equal(locker.tryGetAsLong(() -> assertLocked(locker, 5L)), null);
			Assert.equal(locker.tryGetAsDouble(() -> assertLocked(locker, 0.1)), null);
			Assert.no(locker.tryRun(() -> assertLocked(locker, "")));
		})) {
			exec.get();
		}
	}

	@Test
	public void shouldCreateCondition() throws InterruptedException {
		Locker locker = Locker.of();
		Condition condition = locker.condition();
		try (var _ = TestConcurrent.threadRun(() -> signalLoop(locker, condition))) {
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
		Assert.equal(isLocked(locker.lock), true);
		return response;
	}

	private static boolean isLocked(Lock lock) {
		return ((ReentrantLock) lock).isLocked();
	}
}
