package ceri.common.net;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestSocket;

public class TcpSocketOptionBehavior {

	@Test
	public void shouldDisableSocketOption() throws IOException {
		try (var s = TestSocket.of()) {
			TcpSocketOption.soLinger.disable(s);
			Assert.equal(s.getSoLinger(), -1);
			TcpSocketOption.soSndBuf.disable(s); // does nothing, not supported
			Assert.notEqual(s.getSendBufferSize(), 0);
		}
	}

}
