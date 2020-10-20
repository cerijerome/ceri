package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.FileNotFoundException;
import org.junit.Test;

public class IoTimeoutExceptionBehavior {

	@Test
	public void shouldWrapException() {
		Exception ex = new FileNotFoundException("test");
		IoTimeoutException e = new IoTimeoutException(ex);
		assertEquals(e.getCause(), ex);
	}

}
