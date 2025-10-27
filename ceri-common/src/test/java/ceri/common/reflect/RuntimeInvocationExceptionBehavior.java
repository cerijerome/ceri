package ceri.common.reflect;

import org.junit.Test;
import ceri.common.test.Assert;

public class RuntimeInvocationExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		var e = new RuntimeInvocationException("test");
		Assert.equal(e.getMessage(), "test");
		Assert.isNull(e.getCause());
		var e2 = new RuntimeInvocationException(e);
		Assert.equal(e2.getCause(), e);
		Assert.isNull(e2.getMessage());
	}
}
