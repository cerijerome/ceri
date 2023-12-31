package ceri.common.test;

import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.RuntimeCloseable;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Named;

/**
 * Stops and starts cycles in a separate thread. Primarily used for manual testing.
 */
public class CycleRunner implements RuntimeCloseable {
	private final SimpleExecutor<RuntimeException, ?> exec;
	private final Locker locker;
	private final ValueCondition<Action> sync;
	private Cycle cycle;

	public static interface Cycle extends Named {
		/**
		 * Process the sequence number, and return milliseconds to delay before the next sequence
		 * number. Return -1 to finish. Each call should return as quickly as possible, no sleep or
		 * delay within the call itself.
		 */
		int cycle(int sequence);
	}

	private static enum Action {
		none,
		start,
		stop;
	}

	public static Cycle activeCycle(CycleRunner runner) {
		return runner == null ? null : runner.cycle();
	}

	public static CycleRunner of(Locker locker) {
		return new CycleRunner(locker);
	}

	private CycleRunner(Locker locker) {
		this.locker = locker;
		sync = ValueCondition.of(locker.lock);
		exec = SimpleExecutor.run(this::loops);
	}

	/**
	 * Starts the given cycle.
	 */
	public void start(Cycle cycle) {
		try (var locked = locker.lock()) {
			this.cycle = cycle;
			sync.signal(Action.start);
		}
	}

	/**
	 * Stops the current cycle.
	 */
	public void stop() {
		try (var locked = locker.lock()) {
			this.cycle = null;
			sync.signal(Action.stop);
		}
	}

	/**
	 * Returns the current cycle, or null if none is running.
	 */
	public Cycle cycle() {
		return locker.get(() -> cycle);
	}

	@Override
	public void close() {
		CloseableUtil.close(exec);
	}

	private void loop() throws InterruptedException {
		try (var locked = locker.lock()) {
			sync.await(Action.start);
			var cycle = this.cycle;
			if (cycle == null) return;
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				int delayMs = cycle.cycle(i);
				if (delayMs < 0) break;
				if (sync.awaitPeek(delayMs) != null) break;
			}
		}
	}

	private void loops() {
		try {
			while (true) {
				ConcurrentUtil.checkInterrupted();
				loop();
			}
		} catch (InterruptedException | RuntimeInterruptedException e) {
			// exit
		}
	}
}
