package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.jna.clib.FileDescriptor.Open;

public class PipeBehavior {

	@Test
	public void shouldSetBlocking() throws IOException {
		try (var pipe = Pipe.of()) {
			pipe.blocking(false);
			assertTrue(pipe.read.flags().has(Open.NONBLOCK));
			assertTrue(pipe.write.flags().has(Open.NONBLOCK));
			pipe.blocking(true);
			assertFalse(pipe.read.flags().has(Open.NONBLOCK));
			assertFalse(pipe.write.flags().has(Open.NONBLOCK));
		}
	}

}
