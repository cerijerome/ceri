package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class RuntimeInterruptedExceptionBehavior {

	@Test
	public void shouldHaveInterruptedExceptionAsCause() {
		InterruptedException ex = new InterruptedException();
		RuntimeInterruptedException e = new RuntimeInterruptedException(ex);
		assertEquals(e.getCause(), ex);
	}

}
