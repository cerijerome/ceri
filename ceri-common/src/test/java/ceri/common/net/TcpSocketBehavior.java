package ceri.common.net;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestTcpSocket;

public class TcpSocketBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		assertEquals(TcpSocket.NULL.hostPort(), HostPort.NULL);
		assertEquals(TcpSocket.NULL.localPort(), 0);
		TcpSocket.NULL.option(TcpSocketOption.soLinger, 100);
		assertEquals(TcpSocket.NULL.option(TcpSocketOption.soLinger), -1);
		assertEquals(TcpSocket.NULL.option(TcpSocketOption.soSndBuf), 0);
	}

	@Test
	public void shouldFailToWrapBadSocket() throws IOException {
		try (Socket s = new Socket()) {
			Assert.thrown(() -> TcpSocket.wrap(s)); // not connected
		}
		Assert.thrown(() -> TcpSocket.connect(HostPort.LOCALHOST)); // port out of range -1
	}

	@Test
	public void shouldDetermineIfSocketIsBroken() {
		assertFalse(TcpSocket.isBroken(null));
		assertFalse(TcpSocket.isBroken(new IOException()));
		assertTrue(TcpSocket.isBroken(new SocketException()));
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		try (var s = TestTcpSocket.of()) {
			s.options(TcpSocketOptions.of().set(TcpSocketOption.tcpNoDelay, true));
			assertEquals(s.option(TcpSocketOption.tcpNoDelay), true);
		}
	}

}
