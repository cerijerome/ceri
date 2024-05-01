package ceri.common.reflect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;

public class CreateExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		RuntimeInvocationException e = new RuntimeInvocationException("test");
		assertEquals(e.getMessage(), "test");
		assertNull(e.getCause());
		RuntimeInvocationException e2 = new RuntimeInvocationException(e);
		assertEquals(e2.getCause(), e);
		assertNull(e2.getMessage());
	}

}
