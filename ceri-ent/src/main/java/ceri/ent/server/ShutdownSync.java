package ceri.ent.server;

import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;

public class ShutdownSync {
	private final BooleanCondition sync = BooleanCondition.create();

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
