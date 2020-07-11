package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.event.Listeners;

public class TestListenerBehavior {

	@Test
	public void shouldListenForNotifications() throws InterruptedException {
		Listeners<String> listeners = new Listeners<>();
		try (TestListener<String> listener = TestListener.of(listeners)) {
			listeners.accept("test");
			assertThat(listener.listen.await(), is("test"));
		}
	}

	@Test
	public void shouldAccessActors() {
		Listeners<String> listeners = new Listeners<>();
		try (TestListener<String> listener = TestListener.of(listeners)) {
			assertThat(listener.listenable(), is(listeners));
			assertThat(listener.listener(), is(listener.listener.listener));

		}
	}

}
