package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.function.IntConsumer;
import org.junit.Test;
import org.mockito.Mockito;

public class NullIntListenableBehavior {

	@Test
	public void should() {
		NullIntListenable nl = NullIntListenable.of();
		IntConsumer consumer = Mockito.mock(IntConsumer.class);
		assertThat(nl.listen(consumer), is(false));
		assertThat(nl.listeners().listen(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		verifyNoMoreInteractions(consumer);
	}

}
