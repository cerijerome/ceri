package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.test.Assert;

public class RuntimeInterruptedExceptionBehavior {

	@Test
	public void shouldHaveInterruptedExceptionAsCause() {
		InterruptedException ex = new InterruptedException();
		RuntimeInterruptedException e = new RuntimeInterruptedException(ex);
		Assert.equal(e.getCause(), ex);
	}

}
