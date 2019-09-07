package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;

public class PollingInputStreamBehavior {
	private BlockingBufferStream stream = new BlockingBufferStream();

	@Test(expected = IoTimeoutException.class)
	public void shouldTimeoutIfNoData() throws IOException {
		try (PollingInputStream in = new PollingInputStream(stream.asInputStream(), 1, 1)) {
			in.read();
		}
	}

	@Test
	public void shouldReadData() throws IOException {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		try (PollingInputStream in = new PollingInputStream(new ByteArrayInputStream(data), 1)) {
			assertThat((byte) in.read(), is(Byte.MIN_VALUE));
			assertThat((byte) in.read(), is(Byte.MAX_VALUE));
		}
	}

	@Test
	public void shouldNotWaitIfClosed() throws Exception {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		PollingInputStream in0;
		try (PollingInputStream in = new PollingInputStream(new ByteArrayInputStream(data), 1, 1)) {
			in0 = in;
		}
		byte[] read = new byte[2];
		in0.read(read, 0, 0);
	}

}
