package ceri.common.net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestTcpSocket;

public class TcpSocketBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Assert.equal(TcpSocket.NULL.hostPort(), HostPort.NULL);
		Assert.equal(TcpSocket.NULL.localPort(), 0);
		TcpSocket.NULL.option(TcpSocketOption.soLinger, 100);
		Assert.equal(TcpSocket.NULL.option(TcpSocketOption.soLinger), -1);
		Assert.equal(TcpSocket.NULL.option(TcpSocketOption.soSndBuf), 0);
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
		Assert.no(TcpSocket.isBroken(null));
		Assert.no(TcpSocket.isBroken(new IOException()));
		Assert.yes(TcpSocket.isBroken(new SocketException()));
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		try (var s = TestTcpSocket.of()) {
			s.options(TcpSocketOptions.of().set(TcpSocketOption.tcpNoDelay, true));
			Assert.equal(s.option(TcpSocketOption.tcpNoDelay), true);
		}
	}

}
