package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.test.Assert;

public class SafeReadWriteBehavior {

	@Test
	public void shouldProvideLockForConditions() {
		SafeReadWrite safe = SafeReadWrite.of();
		BoolCondition.of(safe.conditionLock());
	}

	@Test
	public void shouldUseReadLockToRead() {
		SafeReadWrite safe = SafeReadWrite.of();
		Assert.yes(safe.read(() -> {
			Assert.yes(safe.lock.readLock().tryLock());
			safe.lock.readLock().unlock();
			Assert.no(safe.lock.writeLock().tryLock());
			return true;
		}));
	}

	@Test
	public void shouldUseReadLockToReadWithNoReturn() {
		SafeReadWrite safe = SafeReadWrite.of();
		safe.readNoReturn(() -> {
			Assert.yes(safe.lock.readLock().tryLock());
			safe.lock.readLock().unlock();
			Assert.no(safe.lock.writeLock().tryLock());
		});
	}

	@Test
	public void shouldUseWriteLockToWrite() {
		SafeReadWrite safe = SafeReadWrite.of();
		safe.write(() -> {
			Assert.yes(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
		});
	}

	@Test
	public void shouldUseWriteLockToWriteWithReturn() {
		SafeReadWrite safe = SafeReadWrite.of();
		Assert.yes(safe.writeWithReturn(() -> {
			Assert.yes(safe.lock.writeLock().tryLock());
			safe.lock.writeLock().unlock();
			return true;
		}));
	}

}
