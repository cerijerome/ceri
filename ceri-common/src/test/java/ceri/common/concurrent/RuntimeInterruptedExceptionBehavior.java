package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class RuntimeInterruptedExceptionBehavior {

	@Test
	public void shouldHaveInterruptedExceptionAsCause() {
		InterruptedException ex = new InterruptedException();
		RuntimeInterruptedException e = new RuntimeInterruptedException(ex);
		assertThat(e.getCause(), is(ex));
	}

}
