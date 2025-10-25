package ceri.jna.io;

import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.test.Assert;

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
			Assert.thrown(out::flush);
		}
	}
}
