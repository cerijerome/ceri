package ceri.common.test;

import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.util.BasicUtil;

public class TestListenersBehavior {

	@Test
	public void shouldNotifyOnListen() throws InterruptedException {
		TestListeners<String> l = new TestListeners<>();
		l.listen(s -> {});
		l.await(false);
	}

	@Test
	public void shouldNotifyOnUnlisten() throws InterruptedException {
		TestListeners<String> l = new TestListeners<>();
		l.unlisten(s -> {});
		l.await(false);
	}

	@Test
	public void shouldResetSync() throws InterruptedException {
		TestListeners<String> l = new TestListeners<>();
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
