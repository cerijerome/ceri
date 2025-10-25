package ceri.jna.clib;

import static ceri.common.test.Assert.assertEquals;
import static ceri.jna.clib.FileDescriptor.FLAGS;
import java.io.IOException;
import org.junit.Test;
import ceri.jna.clib.FileDescriptor.Open;

public class PipeBehavior {

	@Test
	public void shouldSetBlocking() throws IOException {
		try (var pipe = Pipe.of()) {
			pipe.blocking(false);
			assertEquals(FLAGS.has(pipe.read, Open.NONBLOCK), true);
			assertEquals(FLAGS.has(pipe.write, Open.NONBLOCK), true);
			pipe.blocking(true);
			assertEquals(FLAGS.has(pipe.read, Open.NONBLOCK), false);
			assertEquals(FLAGS.has(pipe.write, Open.NONBLOCK), false);
		}
	}

}
