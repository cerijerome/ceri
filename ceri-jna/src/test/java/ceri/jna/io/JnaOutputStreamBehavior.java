package ceri.jna.io;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;

public class JnaOutputStreamBehavior {

	private static class TestOutputStream extends JnaOutputStream {
		@Override
		protected int write(Memory buffer, int len) throws IOException {
			return 0;
		}
	}

	@Test
	public void shouldFailIfClosed() throws IOException {
		try (var out = new TestOutputStream()) {
			out.flush();
			out.close();
			assertThrown(out::flush);
		}
	}

}
