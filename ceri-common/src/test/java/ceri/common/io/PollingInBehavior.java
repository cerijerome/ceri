package ceri.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class PollingInBehavior {

	@Test
	public void shouldTimeoutIfNoData() throws IOException {
		try (InputStream in0 = IoStream.in((_, _, _) -> 0)) {
			try (PollingIn in = new PollingIn(in0, 1, 1)) {
				Assert.thrown(IoExceptions.Timeout.class, in::read);
			}
		}
	}

	@Test
	public void shouldReadData() throws IOException {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		try (PollingIn in = new PollingIn(new ByteArrayInputStream(data), 1)) {
			Assert.equal((byte) in.read(), Byte.MIN_VALUE);
			Assert.equal((byte) in.read(), Byte.MAX_VALUE);
		}
	}

	@Test
	public void shouldNotWaitIfClosed() throws Exception {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		try (PollingIn in = new PollingIn(new ByteArrayInputStream(data), 1, 1)) {
			in.close();
			byte[] read = new byte[2];
			in.read(read, 0, 0);
		}
	}
}
