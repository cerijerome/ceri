package ceri.common.net;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestIo;
import ceri.common.test.Testing;

public class TcpSocketOptionBehavior {
	private TestIo.Socket socket;

	@After
	public void after() {
		socket = Testing.close(socket);
	}

	@Test
	public void shouldDisableSocketOption() throws IOException {
		socket = TestIo.socket();
		TcpSocketOption.soLinger.disable(socket);
		Assert.equal(socket.getSoLinger(), -1);
		TcpSocketOption.soSndBuf.disable(socket); // does nothing, not supported
		Assert.notEqual(socket.getSendBufferSize(), 0);
	}

}
