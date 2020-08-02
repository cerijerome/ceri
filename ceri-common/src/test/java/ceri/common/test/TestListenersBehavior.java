package ceri.common.test;

import static org.junit.Assert.assertNull;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.util.BasicUtil;

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
			BasicUtil.delay(1);
		}
	}

}
