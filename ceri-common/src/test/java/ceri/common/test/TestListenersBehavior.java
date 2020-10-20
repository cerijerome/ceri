package ceri.common.test;

import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.SimpleExecutor;

public class TestListenersBehavior {

	@Test
	public void shouldClearSync() {
		TestListeners<String> l = TestListeners.of();
		l.sync.set(1);
		l.clear();
		assertNull(l.sync.value());
	}

	@Test
	public void shouldNotifyOnListen() throws InterruptedException {
		TestListeners<String> l = TestListeners.of();
		l.listen(s -> {});
		l.await(false);
	}

	@Test
	public void shouldNotifyOnUnlisten() throws InterruptedException {
		TestListeners<String> l = TestListeners.of();
		l.unlisten(s -> {});
		l.await(false);
	}

	@Test
	public void shouldResetSync() throws InterruptedException {
		TestListeners<String> l = TestListeners.of();
		try (SimpleExecutor<?, ?> exec = SimpleExecutor.run(() -> unlisten(l))) {
			l.await(true);
		}
	}

	/**
	 * Continually unlistens to generate a signal.
	 */
	private static void unlisten(TestListeners<String> listeners) {
		while (true) {
			listeners.unlisten(s -> {});
			ConcurrentUtil.delayMicros(10);
		}
	}

}
