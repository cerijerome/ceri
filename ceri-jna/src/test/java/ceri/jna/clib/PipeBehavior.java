package ceri.jna.clib;

import static ceri.jna.clib.FileDescriptor.FLAGS;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.clib.FileDescriptor.Open;

public class PipeBehavior {

	@Test
	public void shouldSetBlocking() throws IOException {
		try (var pipe = Pipe.of()) {
			pipe.blocking(false);
			Assert.equal(FLAGS.has(pipe.read, Open.NONBLOCK), true);
			Assert.equal(FLAGS.has(pipe.write, Open.NONBLOCK), true);
			pipe.blocking(true);
			Assert.equal(FLAGS.has(pipe.read, Open.NONBLOCK), false);
			Assert.equal(FLAGS.has(pipe.write, Open.NONBLOCK), false);
		}
	}

}
