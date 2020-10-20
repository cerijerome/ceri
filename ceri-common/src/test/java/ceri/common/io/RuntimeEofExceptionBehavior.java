package ceri.common.io;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;

public class RuntimeEofExceptionBehavior {

	@Test
	public void shouldCreateExceptions() {
		RuntimeEofException e0 = RuntimeEofException.of();
		RuntimeEofException e1 = RuntimeEofException.of("test");
		RuntimeEofException e2 = RuntimeEofException.of("%s", "test");
		RuntimeEofException e3 = RuntimeEofException.of(new IOException(), "%s", "test");
		assertAllNotEqual(e0, e1, e2, e3);
		assertEquals(e1.getMessage(), e2.getMessage());
		assertEquals(e1.getMessage(), e3.getMessage());
	}

}
