package ceri.common.event;

import static ceri.common.util.BasicUtil.uncheckedCast;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.Mockito;

public class NullListenableBehavior {

	@Test
	public void should() {
		NullListenable<String> nl = NullListenable.of();
		Consumer<String> consumer = uncheckedCast(Mockito.mock(Consumer.class));
		assertThat(nl.listen(consumer), is(false));
		assertThat(nl.listeners().listen(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		assertThat(nl.unlisten(consumer), is(false));
		verifyNoMoreInteractions(consumer);
	}

}
