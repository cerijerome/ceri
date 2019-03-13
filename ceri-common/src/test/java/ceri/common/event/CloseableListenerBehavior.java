package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.util.BasicUtil;

public class CloseableListenerBehavior {

	@Test
	public void shouldUnlistenOnClose() {
		Listeners<Integer> listeners = new Listeners<>();
		Consumer<Integer> consumer = BasicUtil.uncheckedCast(Mockito.mock(Consumer.class));
		try (CloseableListener<Integer> cl =
			CloseableListener.of(Listenable.Indirect.from(listeners), consumer)) {
			assertThat(listeners.size(), is(1));
			listeners.accept(-1);
		}
		verify(consumer).accept(-1);
		assertThat(listeners.isEmpty(), is(true));
	}

	@Test
	public void shouldIgnoreNullListenable() {
		Consumer<Integer> consumer = BasicUtil.uncheckedCast(Mockito.mock(Consumer.class));
		Listenable<Integer> listenable = null;
		Listenable.Indirect<Integer> indirect = null;
		// Make sure no errors
		try (CloseableListener<Integer> cl = CloseableListener.of(listenable, consumer)) {}
		try (CloseableListener<Integer> cl = CloseableListener.of(indirect, consumer)) {}
		verifyNoMoreInteractions(consumer);
	}

	@Test
	public void shouldIgnoreNullListener() {
		Listeners<Integer> listeners = new Listeners<>();
		try (CloseableListener<Integer> cl = CloseableListener.of(listeners, null)) {
			assertThat(listeners.size(), is(0));
			listeners.accept(-1);
		}
	}

}
