package ceri.jna.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;

public class JnaInputStreamBehavior {

	private static class TestInputStream extends JnaInputStream {
		@Override
		protected int read(Memory buffer, int len) throws IOException {
			return len;
		}
	}

	@Test
	public void shouldProvideAvailableBytesUnlessClosed() throws IOException {
		try (var in = new TestInputStream()) {
			assertEquals(in.available(), 0);
			in.close();
			assertThrown(in::available);
		}
	}

}
