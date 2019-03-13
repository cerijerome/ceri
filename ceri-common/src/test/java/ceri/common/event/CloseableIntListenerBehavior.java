package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.function.IntConsumer;
import org.junit.Test;
import org.mockito.Mockito;

public class CloseableIntListenerBehavior {

	@Test
	public void shouldUnlistenOnClose() {
		IntListeners listeners = new IntListeners();
		IntConsumer consumer = Mockito.mock(IntConsumer.class);
		try (CloseableIntListener cl =
			CloseableIntListener.of(IntListenable.Indirect.from(listeners), consumer)) {
			assertThat(listeners.size(), is(1));
			listeners.accept(-1);
		}
		verify(consumer).accept(-1);
		assertThat(listeners.isEmpty(), is(true));
	}

	@Test
	public void shouldIgnoreNullListenable() {
		IntConsumer consumer = Mockito.mock(IntConsumer.class);
		IntListenable listenable = null;
		IntListenable.Indirect indirect = null;
		// Make sure no errors
		try (CloseableIntListener cl = CloseableIntListener.of(listenable, consumer)) {}
		try (CloseableIntListener cl = CloseableIntListener.of(indirect, consumer)) {}
		verifyNoMoreInteractions(consumer);
	}

	@Test
	public void shouldIgnoreNullListener() {
		IntListeners listeners = new IntListeners();
		try (CloseableIntListener cl = CloseableIntListener.of(listeners, null)) {
			assertThat(listeners.size(), is(0));
			listeners.accept(-1);
		}
	}

}
