package ceri.common.net;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestSocket;

public class TcpSocketOptionBehavior {

	@Test
	public void shouldDisableSocketOption() throws IOException {
		try (var s = TestSocket.of()) {
			TcpSocketOption.soLinger.disable(s);
			assertEquals(s.getSoLinger(), -1);
			TcpSocketOption.soSndBuf.disable(s); // does nothing, not supported
			assertNotEquals(s.getSendBufferSize(), 0);
		}
	}

}
