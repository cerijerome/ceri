package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.runRepeat;
import org.junit.Test;
import ceri.common.event.Listeners;

public class TestListenerBehavior {

	@Test
	public void shouldListenForNotifications() throws InterruptedException {
		Listeners<String> listeners = Listeners.of();
		try (TestListener<String> listener = TestListener.of(listeners)) {
			listeners.accept("test");
			assertEquals(listener.await(), "test");
		}
	}

	@Test
	public void shouldNotHang() throws InterruptedException {
		Listeners<String> listeners = Listeners.of();
		try (TestListener<String> listener = TestListener.of(listeners)) {
			try (var exec = runRepeat(() -> listeners.accept("test"))) {
				assertEquals(listener.awaitClear(), "test");
			}
		}
	}

	@Test
	public void shouldReturnListener() throws InterruptedException {
		Listeners<String> listeners = Listeners.of();
		try (TestListener<String> listener = TestListener.of(listeners)) {
			listener.listener().accept("test");
			assertEquals(listener.await(), "test");
		}
	}

}
