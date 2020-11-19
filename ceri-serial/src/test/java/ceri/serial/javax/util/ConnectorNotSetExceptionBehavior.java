package ceri.serial.javax.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ConnectorNotSetExceptionBehavior {

	@Test
	public void shouldCreateWithMessage() {
		assertEquals(new ConnectorNotSetException("test").getMessage(), "test");
	}

}
