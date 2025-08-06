package ceri.ent.server;

import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.RuntimeInterruptedException;

public class ShutdownSync {
	private final BoolCondition sync = BoolCondition.of();

	public void signal() {
		sync.signal();
	}

	public void await() {
		try {
			sync.await();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

}
