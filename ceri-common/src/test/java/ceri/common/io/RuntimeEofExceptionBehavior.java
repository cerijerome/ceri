package ceri.common.io;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(e1.getMessage(), is(e2.getMessage()));
		assertThat(e1.getMessage(), is(e3.getMessage()));
	}

}
