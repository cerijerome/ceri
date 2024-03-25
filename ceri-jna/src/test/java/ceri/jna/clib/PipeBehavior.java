package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;

public class PipeBehavior {

	@Test
	public void shouldSetBlocking() throws IOException {
		try (var pipe = Pipe.of()) {
			pipe.blocking(false);
			assertTrue(pipe.read.flags().has(OpenFlag.O_NONBLOCK));
			assertTrue(pipe.write.flags().has(OpenFlag.O_NONBLOCK));
			pipe.blocking(true);
			assertFalse(pipe.read.flags().has(OpenFlag.O_NONBLOCK));
			assertFalse(pipe.write.flags().has(OpenFlag.O_NONBLOCK));
		}
	}

}
