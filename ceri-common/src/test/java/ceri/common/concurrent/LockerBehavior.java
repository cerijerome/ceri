package ceri.common.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.Testing;

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
		Assert.equal(locker.getAsInt(() -> assertLocked(locker, 3)), 3);
		Assert.equal(locker.getAsLong(() -> assertLocked(locker, 5L)), 5L);
		locker.run(() -> assertLocked(locker, ""));
	}

	@Test
	public void shouldTryToExecuteUnlockedFunctions() {
		Locker locker = Locker.of();
		Assert.equal(locker.tryGet(() -> assertLocked(locker, "test")).value(), "test");
		Assert.equal(locker.tryGetAsInt(() -> assertLocked(locker, 3)).getAsInt(), 3);
		Assert.equal(locker.tryGetAsLong(() -> assertLocked(locker, 5L)).getAsLong(), 5L);
		Assert.yes(locker.tryRun(() -> assertLocked(locker, "")));
	}

	@Test
	public void shouldTryToExecuteLockedFunctions() {
		Locker locker = Locker.of();
		try (var _ = locker.lock(); var exec = Testing.threadRun(() -> {
			Assert.yes(locker.tryGet(() -> assertLocked(locker, "test")).isEmpty());
			Assert.yes(locker.tryGetAsInt(() -> assertLocked(locker, 3)).isEmpty());
			Assert.yes(locker.tryGetAsLong(() -> assertLocked(locker, 5L)).isEmpty());
			Assert.no(locker.tryRun(() -> assertLocked(locker, "")));
		})) {
			exec.get();
		}
	}

	@Test
	public void shouldCreateCondition() throws InterruptedException {
		Locker locker = Locker.of();
		Condition condition = locker.condition();
		try (var _ = Testing.threadRun(() -> signalLoop(locker, condition))) {
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
