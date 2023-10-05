package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;

public class IncompleteIoExceptionBehavior {

	@Test
	public void testVerifyMinimumBytesTransferred() throws IOException {
		IncompleteIoException.verify(100, 100);
		IncompleteIoException.verify(100, 99);
		assertThrown(() -> IncompleteIoException.verify(99, 100));
	}

	@Test
	public void shouldCreateWithCause() {
		var e = IncompleteIoException.of(99, 100, new IOException("test"));
		assertEquals(e.actual, 99);
		assertEquals(e.expected, 100);
	}

}
