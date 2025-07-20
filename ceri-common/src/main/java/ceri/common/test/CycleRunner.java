package ceri.common.test;

import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.Functions;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Named;

/**
 * Stops and starts cycles in a separate thread. Primarily used for manual testing.
 */
public class CycleRunner implements Functions.Closeable {
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
		return of(locker, Integer.MAX_VALUE);
	}

	public static CycleRunner of(Locker locker, int max) {
		return new CycleRunner(locker, max);
	}

	private CycleRunner(Locker locker, int max) {
		this.locker = locker;
		sync = ValueCondition.of(locker.lock);
		exec = SimpleExecutor.run(() -> loops(max));
	}

	/**
	 * Starts the given cycle.
	 */
	public void start(Cycle cycle) {
		try (var _ = locker.lock()) {
			this.cycle = cycle;
			sync.signal(Action.start);
		}
	}

	/**
	 * Stops the current cycle.
	 */
	public void stop() {
		try (var _ = locker.lock()) {
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

	private void loop(int max) throws InterruptedException {
		try (var _ = locker.lock()) {
			sync.await(Action.start);
			var cycle = this.cycle;
			if (cycle == null) return;
			for (int i = 0; i < max; i++) {
				int delayMs = cycle.cycle(i);
				if (delayMs < 0) break;
				if (sync.awaitPeek(delayMs) != null) break;
			}
		}
	}

	private void loops(int max) {
		try {
			while (true) {
				ConcurrentUtil.checkInterrupted();
				loop(max);
			}
		} catch (InterruptedException | RuntimeInterruptedException e) {
			// exit
		}
	}
}
