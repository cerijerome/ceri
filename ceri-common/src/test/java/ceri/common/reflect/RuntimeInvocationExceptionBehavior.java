package ceri.common.reflect;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class RuntimeInvocationExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		var e = new RuntimeInvocationException("test");
		assertEquals(e.getMessage(), "test");
		Assert.isNull(e.getCause());
		var e2 = new RuntimeInvocationException(e);
		assertEquals(e2.getCause(), e);
		Assert.isNull(e2.getMessage());
	}
}
