package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class SafeReadWriteBehavior {

	@Test
	public void shouldProvideLockForConditions() {
		SafeReadWrite safe = SafeReadWrite.of();
		BoolCondition.of(safe.conditionLock());
	}

	@Test
	public void shouldUseReadLockToRead() {
		SafeReadWrite safe = SafeReadWrite.of();
		assertTrue(safe.read(() -> {
			assertTrue(safe.lock.readLock().tryLock());
			safe.lock.readLock().unlock();
			assertFalse(safe.lock.writeLock().tryLock());
			return true;
		}));
	}

	@Test
	public void shouldUseReadLockToReadWithNoReturn() {
		SafeReadWrite safe = SafeReadWrite.of();
		safe.readNoReturn(() -> {
			assertTrue(safe.lock.readLock().tryLock());
			safe.lock.readLock().unlock();
			assertFalse(safe.lock.writeLock().tryLock());
		});
	}

	@Test
	public void shouldUseWriteLockToWrite() {
		SafeReadWrite safe = SafeReadWrite.of();
		safe.write(() -> {
			assertTrue(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
		});
	}

	@Test
	public void shouldUseWriteLockToWriteWithReturn() {
		SafeReadWrite safe = SafeReadWrite.of();
		assertTrue(safe.writeWithReturn(() -> {
			assertTrue(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
			return true;
		}));
	}

}
