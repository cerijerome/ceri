package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class PollingInputStreamBehavior {

	@Test
	public void shouldTimeoutIfNoData() throws IOException {
		try (InputStream in0 = IoStreamUtil.in((_, _, _) -> 0)) {
			try (PollingInputStream in = new PollingInputStream(in0, 1, 1)) {
				assertThrown(IoExceptions.Timeout.class, in::read);
			}
		}
	}

	@Test
	public void shouldReadData() throws IOException {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		try (PollingInputStream in = new PollingInputStream(new ByteArrayInputStream(data), 1)) {
			assertEquals((byte) in.read(), Byte.MIN_VALUE);
			assertEquals((byte) in.read(), Byte.MAX_VALUE);
		}
	}

	@Test
	public void shouldNotWaitIfClosed() throws Exception {
		byte[] data = { Byte.MIN_VALUE, Byte.MAX_VALUE };
		try (PollingInputStream in = new PollingInputStream(new ByteArrayInputStream(data), 1, 1)) {
			in.close();
			byte[] read = new byte[2];
			in.read(read, 0, 0);
		}
	}

}
