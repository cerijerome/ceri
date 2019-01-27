package ceri.common.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SafeReadWriteBehavior {

	@Test
	public void shouldProvideLockForConditions() {
		SafeReadWrite safe = SafeReadWrite.create();
		BooleanCondition.create(safe.conditionLock());
	}

	@Test
	public void shouldUseReadLockToRead() {
		SafeReadWrite safe = SafeReadWrite.create();
		assertTrue(safe.read(() -> {
			assertTrue(safe.lock.readLock().tryLock());
			safe.lock.readLock().unlock();
			assertFalse(safe.lock.writeLock().tryLock());
			return true;
		}));
	}

	@Test
	public void shouldUseWriteLockToWrite() {
		SafeReadWrite safe = SafeReadWrite.create();
		safe.write(() -> {
			assertTrue(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
		});
	}

	@Test
	public void shouldUseWriteLockToWriteWithReturn() {
		SafeReadWrite safe = SafeReadWrite.create();
		assertTrue(safe.writeWithReturn(() -> {
			assertTrue(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
			return true;
		}));
	}

}
